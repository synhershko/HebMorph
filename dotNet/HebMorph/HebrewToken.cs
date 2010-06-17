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

namespace HebMorph
{
    public class HebrewToken : Token, IComparable
    {
        public HebrewToken(string _word, byte _prefixLength, HSpell.DMask _mask, string _lemma, float _score)
            : base(_word)
        {
            this.PrefixLength = _prefixLength;
            this.Mask = _mask;
            if (_lemma == null)
                this.Lemma = _word.Substring(PrefixLength); // Support null lemmas while still taking into account prefixes
            else
                this.Lemma = _lemma;
            this.Score = _score;
        }

        public float Score = 1.0f;
        public byte PrefixLength;
        public HSpell.DMask Mask;
        public string Lemma;

        public override bool Equals(object obj)
        {
            HebrewToken o = obj as HebrewToken;
            if (o == null) return false;

            // TODO: In places where Equals returns true while being called from the sorted results list,
            // but this.Score < o.Score, we probably should somehow update the score for this object...

            return (this.PrefixLength == o.PrefixLength
                && this.Mask == o.Mask
                && this.Text.Equals(o.Text)
                && Lemma == o.Lemma);
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();//TODO
        }

        public override string ToString()
        {
            return string.Format("\t{0} ({1})", Lemma, HSpell.LingInfo.DMask2EnglishString(Mask));
        }

        #region IComparable Members

        public int CompareTo(object obj)
        {
            HebrewToken o = obj as HebrewToken;
            if (o == null) return -1;

            if (this.Score == o.Score)
                return 0;
            else if (this.Score > o.Score)
                return 1;
            return -1;
        }

        #endregion
    }
}
