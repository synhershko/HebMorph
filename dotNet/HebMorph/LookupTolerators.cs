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

        // Tolerate all standard Em Kriya appearances (Yud [incl. doubling], Vav, and double consonant Vav)
        public static byte? TolerateEmKryiaAll(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            byte? ret = TolerateNonDoubledConsonantVav(key, ref keyPos, word, ref score, curChar);
            if (ret != null)
                return ret;
            ret = TolerateEmKryiaVav(key, ref keyPos, word, ref score, curChar);
            if (ret != null)
                return ret;
            ret = TolerateEmKryiaYud(key, ref keyPos, word, ref score, curChar);
            if (ret != null)
                return ret;

            return null;
        }

        public static byte? TolerateEmKryiaYud(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            if (curChar != 'י' || keyPos == 0 || keyPos + 1 == key.Length)
                return null;

            // We already have consumed a Yud recently
            if (word[word.Length - 1] == 'י')
            {
                // We allow adding another Yud only if there was one in the key originally
                if (key[keyPos] == 'י')
                {
                    // There are two Yud letters in the original key - try omitting one instead
                    if (key[keyPos + 1] == 'י')
                    {
                        keyPos += 2;
                        score *= 0.9f;
                        return 1;
                    }

                    score *= 0.8f;
                    return 0;
                }
            }
            else // No Yud existed before in the key, so we tolerate normally
            {
                score *= 0.8f;
                return 1;
            }

            return null;
        }

        public static byte? TolerateEmKryiaVav(char[] key, ref byte keyPos, string word, ref float score, char curChar)
        {
            if (curChar != 'ו' || keyPos == 0 || keyPos + 1 == key.Length)
                return null;

            if (word[word.Length - 1] != 'ו')
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

            if (keyPos == 0 || keyPos + 1 == key.Length)
                return null;

            if (key[keyPos] == 'ו' && curChar != 'ו')
            {
                if (word[word.Length - 1] == 'ו')
                {
                    keyPos++;
                }
                else
                {
                    score *= 0.8f;
                }
                return 0;
            }

            return null;
        }
    }
}
