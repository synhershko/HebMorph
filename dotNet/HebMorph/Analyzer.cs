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
    public class Result : IComparable
    {
        public Result(string _word, byte _prefixLength, HSpell.DMask _mask, string _stem, float _score)
        {
            this.Word = _word;
            this.PrefixLength = _prefixLength;
            this.Mask = _mask;
            if (_stem == null)
                Stem = _word.Substring(PrefixLength); // Support null stems while still taking into account prefixes
            else
                this.Stem = _stem;
            this.Score = _score;
        }

        public float Score = 1.0f;
        public byte PrefixLength;
        public HSpell.DMask Mask;
        public string Word, Stem;

        public override bool Equals(object obj)
        {
            Result o = obj as Result;
            if (o == null) return false;

            // TODO: In places where Equals returns true while being called from the sorted results list,
            // but this.Score < o.Score, we probably should somehow update the score for this object...

            return (this.PrefixLength == o.PrefixLength
                && this.Mask == o.Mask
                && this.Word.Equals(o.Word)
                && Stem == o.Stem);
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();//TODO
        }

        public override string ToString()
        {
            return string.Format("\t{0} ({1})", Stem, HSpell.LingInfo.DMask2EnglishString(Mask));
        }

        #region IComparable Members

        public int CompareTo(object obj)
        {
            Result o = obj as Result;
            if (o == null) return -1;

            if (this.Score == o.Score)
                return 0;
            else if (this.Score > o.Score)
                return 1;
            return -1;
        }

        #endregion
    }

    public class Analyzer
    {
        private DictRadix<HebMorph.MorphData> m_dict;
        private DictRadix<int> m_prefixes;
        private bool m_IsInitialized = false;

        public bool IsInitialized { get { return m_IsInitialized; } }

        public void InitFromHSpellFolder(string path, bool loadMorpholicData, bool allowHeHasheela)
        {
            m_dict = HSpell.Loader.LoadDictionaryFromHSpellFolder(path, loadMorpholicData);
            m_prefixes = HebMorph.HSpell.LingInfo.BuildPrefixTree(allowHeHasheela);
            m_IsInitialized = true;
        }

        public bool IsLegalPrefix(string word)
        {
            if (m_prefixes.Lookup(word) > 0)
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

        public List<Result> CheckWordExact(string word)
        {
            // TODO: Verify word to be non-empty and contain Hebrew characters?

            RealSortedList<Result> ret = new RealSortedList<Result>();

            MorphData md = m_dict.Lookup(word);
            if (md != null)
            {
                for (int result = 0; result < md.Stems.Length; result++)
                {
                    ret.AddUnique(new Result(word, 0, md.DescFlags[result], md.Stems[result], 1.0f));
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
                    for (int result = 0; result < md.Stems.Length; result++)
                    {
                        if (((int)HSpell.LingInfo.dmask2ps(md.DescFlags[result]) & prefixMask) > 0)
                            ret.AddUnique(new Result(word, prefLen, md.DescFlags[result], md.Stems[result], 0.9f));
                    }
                }
            }

            // TODO: Support Gimatria

            if (ret.Count > 0)
                return ret;
            return null;
        }

        public List<Result> CheckWordTolerant(string word)
        {
            // TODO: Verify word to be non-empty and contain Hebrew characters?

            RealSortedList<Result> ret = new RealSortedList<Result>();

            byte prefLen = 0;
            int prefixMask;

            List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.LookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
            if (tolerated != null)
            {
                foreach (DictRadix<MorphData>.LookupResult lr in tolerated)
                {
                    for (int result = 0; result < lr.Data.Stems.Length; result++)
                    {
                        ret.AddUnique(new Result(lr.Word, 0, lr.Data.DescFlags[result], lr.Data.Stems[result], lr.Score));
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
                        for (int result = 0; result < lr.Data.Stems.Length; result++)
                        {
                            if (((int)HSpell.LingInfo.dmask2ps(lr.Data.DescFlags[result]) & prefixMask) > 0)
                                ret.AddUnique(new Result(word.Substring(0, prefLen) + lr.Word, prefLen, lr.Data.DescFlags[result], lr.Data.Stems[result], lr.Score * 0.9f));
                        }
                    }
                }
            }

            // TODO: Support Gimatria

            if (ret.Count > 0)
                return ret;
            return null;
        }
    }
}
