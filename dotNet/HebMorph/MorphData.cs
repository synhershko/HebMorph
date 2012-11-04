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

using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph
{
    public class MorphData
    {
        public HSpell.DMask[] DescFlags;
        public string[] Lemmas;
        public byte Prefixes;

        public override bool Equals(object obj)
        {
            MorphData o = obj as MorphData;
            if (o == null) return false;

            if (DescFlags.Length != o.DescFlags.Length)
                return false;

            for (int i = 0; i < DescFlags.Length; i++)
            {
                if (DescFlags[i] != o.DescFlags[i] || !Lemmas[i].Equals(o.Lemmas[i]))
                    return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return DescFlags.GetHashCode() * Lemmas.GetHashCode();
        }
    }
}
