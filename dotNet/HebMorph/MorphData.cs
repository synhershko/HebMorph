/***************************************************************************
 * HebMorph - making Hebrew properly searchable
 * 
 *   Copyright (C) 2010-2012                                               
 *      Itamar Syn-Hershko <itamar at code972 dot com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

using System.Collections.Generic;
using HebMorph.HSpell;

namespace HebMorph
{
    public class MorphData
    {
        public List<Entry> Lemmas { get; set; }
        public byte Prefixes { get; set; }

        public void AddLemma(Entry lemma)
        {
            Lemmas.Add(lemma);
        }

        public void ClearLemmas()
        {
            Lemmas.Clear();
        }

        public override bool Equals(object obj)
        {
            MorphData o = obj as MorphData;
            if (o == null) return false;

            if (Lemmas.Count != o.Lemmas.Count)
                return false;

            for (int i = 0; i < Lemmas.Count; i++)
            {
                if (Lemmas[i] != o.Lemmas[i] || !Lemmas[i].Equals(o.Lemmas[i]))
                    return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return Lemmas.GetHashCode();
        }

        public enum DescFlag
        {
            D_EMPTY = 0,
            D_NOUN = 1,
            D_VERB = 2,
            D_ADJ = 3,
            D_PROPER = 4,
            D_ACRONYM = 5,
        }

        public class Entry
        {
            public Entry(string lem, DescFlag descFlag, PrefixType prefixType)
            {
                Lemma = lem;
                DescFlag = descFlag;
                Prefix = prefixType;
            }

            public string Lemma { get; set; }
            public PrefixType Prefix { get; set; }
            public DescFlag DescFlag { get; set; }
        }
    }
}
