/***************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   Distributed under the GNU General Public License, Version 2.0.        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation (v2).                                    *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Steet, Fifth Floor, Boston, MA  02111-1307, USA.          *
 ***************************************************************************/

using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph
{
    /// <summary>
    /// Contains static tolerator function driving the tolerant lookup in HebMorph's dictionary radix
    /// </summary>
    public static class LookupTolerators
    {
        /// <summary>
        /// Toleration function used in HebMorph's tolerant dictionary lookups. Returns positive number (or zero)
        /// if tolerance was made, null if not.
        /// A tolerator function may also increment keyPos, which is provided by reference. For cases where a tolerator
        /// function returns 0, it *MUST* increment keyPos, or an endless recursion will be initiated.
        /// </summary>
        public delegate byte? ToleranceFuncDelegate(char[] key, ref byte keyPos, string word, ref float score, char curChar);

        /// <summary>
        /// Tolerate all standard Em Kriya appearances (Yud [incl. doubling], Vav, and double consonant Vav)
        /// </summary>
        public static readonly ToleranceFuncDelegate[] TolerateEmKryiaAll = { TolerateNonDoubledConsonantVav, TolerateEmKryiaVav, TolerateEmKryiaYud };

        /// <summary>
        /// Current trie position is Yud, while the requested key does not contain Yud at this position, or 
        /// contains Yud but not twice.
        /// </summary>
        public static byte? TolerateEmKryiaYud(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            if (keyPos == 0) // check this isn't the beginning of a word (no one misses Yud there)
                return null;

            // Yud shouldn't be tolerated before a Vav
            if (key[keyPos] == 'ו')
                return null;

            if (curChar != 'י')
            {
                // Support keys with doubled Yud, where the actual word in the dictionary isn't
                if (key[keyPos] == 'י' && key[keyPos - 1] == 'י')
                {
                    score *= 0.9f;
                    keyPos++;
                    return 0;
                }

                // Support Hirik-Haser (Niqqudless missing Yud), although it would suffer a high penalty
                // to low rate false positives when there are better options
                if (key[keyPos] == 'י')
                {
                    score *= 0.6f;
                    keyPos++;
                    return 0;
                }
               
                return null;
            }

            // Don't initiate a toleration process if current key position has Yud already. If the correct
            // spelling requires double-Yud, we will arrive here again very soon
            if (key[keyPos] == 'י')
                return null;

            // We already have consumed a Yud very recently
            if (word[word.Length - 1] == 'י')
            {
                // We allow adding another Yud only if there was one in the key originally, and the key is longer
                // than 3 letters (otherwise חיה becomes חייה, and בית becomes ביית).
                if (key[keyPos - 1] != 'י' || (keyPos + 1 == key.Length && key.Length <= 3))
                    return null;

                score *= 0.8f;
                return 1;
            }
            // No Yud existed before in the key, so we tolerate normally unless we consumed a Vav recently
            else if (word[word.Length - 1] != 'ו')
            {
                score *= 0.8f;
                return 1;
            }

            return null;
        }

        /// <summary>
        /// Accepts Vav as the next in key if absent from the original key, only if:
        ///     * Current original key position isn't the beginning or end of a word
        ///     * Tolerating Vav doesn't create a sequence of Vav and Yud
        ///     * Previous key position isn't another Vav, so is the next in the original key (handled by TolerateNonDoubledConsonantVav)
        ///     * Previous key position isn't Yud (too intrusive a tolerance)
        /// </summary>
        public static byte? TolerateEmKryiaVav(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            if (curChar != 'ו' || // check current trie position
                keyPos == 0 || keyPos + 1 == key.Length || // check this isn't the end or the beginning of a word (no one misses Vav there)
                key[keyPos] == 'י' || key[keyPos] == 'ה' || // Vav shouldn't be tolerated before a Yud or a Heh
                key[keyPos] == 'ו' // Don't low-rank exact matches
                )
                return null;

            char prevChar = word[word.Length - 1];
            if (key[keyPos + 1] != 'ו' && prevChar != 'ו' && // This case is handled by TolerateNonDoubledConsonantVav
                prevChar != 'י' // This is an edit too intrusive to be a possible niqqud-less spelling
                )
            {
                score *= 0.8f;
                return 1;
            }

            return null;
        }

        public static byte? TolerateNonDoubledConsonantVav(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            // TODO: Here we apply the Academia's "ha-ktiv hasar ha-niqqud" rule of doubling
            // a consonant waw in the middle a word, unless it's already next to a waw

            if (curChar == 'ו' || keyPos == 0 || keyPos + 1 == key.Length)
                return null;

            if (key[keyPos] == 'ו' && word[word.Length - 1] == 'ו')
            {
                keyPos++;
                score *= 0.8f;
                return 0;
            }

            return null;
        }
    }
}
