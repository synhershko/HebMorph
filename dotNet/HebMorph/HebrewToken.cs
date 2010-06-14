using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph
{
    public class HebrewToken : IComparable
    {
        public HebrewToken(string _word, byte _prefixLength, HSpell.DMask _mask, string _lemma, float _score)
        {
            this.Word = _word;
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
        public string Word, Lemma;

        public override bool Equals(object obj)
        {
            HebrewToken o = obj as HebrewToken;
            if (o == null) return false;

            // TODO: In places where Equals returns true while being called from the sorted results list,
            // but this.Score < o.Score, we probably should somehow update the score for this object...

            return (this.PrefixLength == o.PrefixLength
                && this.Mask == o.Mask
                && this.Word.Equals(o.Word)
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
