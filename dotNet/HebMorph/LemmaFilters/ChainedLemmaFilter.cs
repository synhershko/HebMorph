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

namespace HebMorph.LemmaFilters
{
    using System;
    using System.Collections.Generic;

    public class ChainedLemmaFilter : LemmaFilterBase
    {
        public LinkedList<LemmaFilterBase> Filters { get { return _filters; } }
        protected LinkedList<LemmaFilterBase> _filters = new LinkedList<LemmaFilterBase>();

        public override IList<Token> FilterCollection(IList<Token> collection, IList<Token> preallocatedOut)
        {
            if (preallocatedOut == null)
                preallocatedOut = new List<Token>();
            else
                preallocatedOut.Clear();

            bool filteringWasRequired = false;
            LinkedList<LemmaFilterBase>.Enumerator en = _filters.GetEnumerator();
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
