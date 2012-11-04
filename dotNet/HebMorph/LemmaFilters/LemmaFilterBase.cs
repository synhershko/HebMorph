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

namespace HebMorph.LemmaFilters
{
    public abstract class LemmaFilterBase
    {
        public IList<Token> FilterCollection(IList<Token> collection)
        {
            return FilterCollection(collection, null);
        }

        public virtual IList<Token> FilterCollection(IList<Token> collection, IList<Token> preallocatedOut)
        {
            if (!NeedsFiltering(collection))
                return null;

            if (preallocatedOut == null)
                preallocatedOut = new List<Token>();
            else
                preallocatedOut.Clear();

            foreach (Token t in collection)
            {
                if (IsValidToken(t))
                    preallocatedOut.Add(t);
            }

            return preallocatedOut;
        }

        abstract public bool NeedsFiltering(IList<Token> collection);
        abstract public bool IsValidToken(Token t);
    }
}
