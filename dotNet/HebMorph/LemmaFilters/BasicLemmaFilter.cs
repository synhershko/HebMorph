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
using HebMorph.HSpell;

namespace HebMorph.LemmaFilters
{
    /// <summary>
    /// BasicLemmaFilter will only filter collections with more than one lemma. For them, any lemma
    /// scored below 0.7 is probably a result of some heavy toleration, and will be ignored.
    /// </summary>
    public class BasicLemmaFilter : LemmaFilterBase
    {
        public override bool NeedsFiltering(IList<Token> collection)
        {
            return collection.Count > 1;
        }

        public override bool IsValidToken(Token t)
        {
            if (t is HebrewToken)
            {
                HebrewToken ht = t as HebrewToken;

                // Pose a minimum score limit for words
                if (ht.Score < 0.7f)
                    return false;

                // Pose a higher threshold to verbs (easier to get irrelevant verbs from toleration)
                if ((ht.Mask & DMask.D_TYPEMASK) == DMask.D_VERB && ht.Score < 0.85f)
                    return false;
            }
            return true;
        }
    }
}
