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

using HebMorph.DataStructures;

namespace HebMorph
{
    public class Lemmatizer
    {
        private DictRadix<HebMorph.MorphData> m_dict;
        private DictRadix<int> m_prefixes;
        private bool m_IsInitialized = false;
        public bool IsInitialized { get { return m_IsInitialized; } }

        public Lemmatizer()
        {
        }

        public Lemmatizer(string hspellPath, bool loadMorpholicalData, bool allowHeHasheela)
        {
            InitFromHSpellFolder(hspellPath, loadMorpholicalData, allowHeHasheela);
        }

        public void InitFromHSpellFolder(string path, bool loadMorpholicalData, bool allowHeHasheela)
        {
            m_dict = HSpell.Loader.LoadDictionaryFromHSpellFolder(path, loadMorpholicalData);
            m_prefixes = HebMorph.HSpell.LingInfo.BuildPrefixTree(allowHeHasheela);
            m_IsInitialized = true;
        }

        public bool IsLegalPrefix(string str)
        {
            if (m_prefixes.Lookup(str) > 0)
                return true;

            return false;
        }

        // See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
        public string TryStrippingPrefix(string word)
        {
            // TODO: Make sure we conform to the academy rules as closely as possible

            int firstQuote = word.IndexOf('"');
            
            if (firstQuote > -1)
            {
                if (IsLegalPrefix(word.Substring(0, firstQuote)))
                    return word.Substring(firstQuote + 1, word.Length - firstQuote - 1);
            }

            int firstSingleQuote = word.IndexOf('\'');
            if (firstSingleQuote == -1)
                return word;

            if (firstQuote > -1 && firstSingleQuote > firstQuote)
                return word;

            if (IsLegalPrefix(word.Substring(0, firstSingleQuote)))
                return word.Substring(firstSingleQuote + 1, word.Length - firstSingleQuote - 1);
            
            return word;
        }

        /// <summary>
        /// Removes all Niqqud character from a word
        /// </summary>
        /// <param name="word">A string to remove Niqqud from</param>
        /// <returns>A new word "clean" of Niqqud chars</returns>
        public string RemoveNiqqud(string word)
        {
            int length = word.Length;
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++)
            {
                if (word[i] < 1455 || word[i] > 1476) // current position is not a Niqqud character
                    sb.Append(word[i]);
            }
            return sb.ToString();
        }

        public IList<HebrewToken> Lemmatize(string word)
        {
            // TODO: Verify word to be non-empty and contain Hebrew characters?

            RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

            MorphData md = m_dict.Lookup(word);
            if (md != null)
            {
                for (int result = 0; result < md.Lemmas.Length; result++)
                {
                    ret.AddUnique(new HebrewToken(word, 0, md.DescFlags[result], md.Lemmas[result], 1.0f));
                }
            }

            byte prefLen = 0;
            int prefixMask;
            while (true)
            {
                // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                if (word.Length - prefLen < 2)
                    break;

                prefixMask = m_prefixes.Lookup(word.Substring(0, ++prefLen));
                if (prefixMask == 0) // no such prefix
                    break;

                md = m_dict.Lookup(word.Substring(prefLen));
                if (md != null && (md.Prefixes & prefixMask) > 0)
                {
                    for (int result = 0; result < md.Lemmas.Length; result++)
                    {
                        if (((int)HSpell.LingInfo.dmask2ps(md.DescFlags[result]) & prefixMask) > 0)
                            ret.AddUnique(new HebrewToken(word, prefLen, md.DescFlags[result], md.Lemmas[result], 0.9f));
                    }
                }
            }

            if (ret.Count > 0)
                return ret;
            return null;
        }

        public IList<HebrewToken> LemmatizeTolerant(string word)
        {
            // TODO: Verify word to be non-empty and contain Hebrew characters?

            RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

            byte prefLen = 0;
            int prefixMask;

            List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.LookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
            if (tolerated != null)
            {
                foreach (DictRadix<MorphData>.LookupResult lr in tolerated)
                {
                    for (int result = 0; result < lr.Data.Lemmas.Length; result++)
                    {
                        ret.AddUnique(new HebrewToken(lr.Word, 0, lr.Data.DescFlags[result], lr.Data.Lemmas[result], lr.Score));
                    }
                }
            }

            prefLen = 0;
            while (true)
            {
                // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                if (word.Length - prefLen < 2)
                    break;

                prefixMask = m_prefixes.Lookup(word.Substring(0, ++prefLen));
                if (prefixMask == 0) // no such prefix
                    break;

                tolerated = m_dict.LookupTolerant(word.Substring(prefLen), LookupTolerators.TolerateEmKryiaAll);
                if (tolerated != null)
                {
                    foreach (DictRadix<MorphData>.LookupResult lr in tolerated)
                    {
                        for (int result = 0; result < lr.Data.Lemmas.Length; result++)
                        {
                            if (((int)HSpell.LingInfo.dmask2ps(lr.Data.DescFlags[result]) & prefixMask) > 0)
                                ret.AddUnique(new HebrewToken(word.Substring(0, prefLen) + lr.Word, prefLen, lr.Data.DescFlags[result], lr.Data.Lemmas[result], lr.Score * 0.9f));
                        }
                    }
                }
            }

            if (ret.Count > 0)
                return ret;
            return null;
        }
    }
}
