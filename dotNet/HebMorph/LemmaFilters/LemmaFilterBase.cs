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
