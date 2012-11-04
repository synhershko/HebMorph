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

namespace HebMorph.LemmaFilters
{
    using System;
    using System.Collections.Generic;

    public class ChainedLemmaFilter : LemmaFilterBase
    {
        public LinkedList<LemmaFilterBase> Filters { get { return filtersList; } }
        protected LinkedList<LemmaFilterBase> filtersList = new LinkedList<LemmaFilterBase>();

        public override IList<Token> FilterCollection(IList<Token> collection, IList<Token> preallocatedOut)
        {
            if (preallocatedOut == null)
                preallocatedOut = new List<Token>();
            else
                preallocatedOut.Clear();

            bool filteringWasRequired = false;
            LinkedList<LemmaFilterBase>.Enumerator en = filtersList.GetEnumerator();
            while (en.MoveNext())
            {
                LemmaFilterBase filter = en.Current;

                if (!filter.NeedsFiltering(collection))
                    continue;

                filteringWasRequired = true;

                foreach (Token t in collection)
                {
                    if (filter.IsValidToken(t))
                        preallocatedOut.Add(t);
                }
            }

            if (filteringWasRequired) return preallocatedOut;

            return null;
        }

        public override bool IsValidToken(Token t)
        {
            throw new NotImplementedException("The method or operation is not implemented.");
        }

        public override bool NeedsFiltering(IList<Token> collection)
        {
            throw new NotImplementedException("The method or operation is not implemented.");
        }
    }
}
