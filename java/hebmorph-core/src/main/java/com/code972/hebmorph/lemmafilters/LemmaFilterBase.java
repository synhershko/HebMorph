/***************************************************************************
 *   Copyright (C) 2010-2013 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com> (initial Java port)           *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package com.code972.hebmorph.lemmafilters;

import com.code972.hebmorph.Token;
import java.util.ArrayList;
import java.util.List;


public abstract class LemmaFilterBase
{
	public List<Token> filterCollection(List<Token> collection)
	{
		return filterCollection(collection, null);
	}

	public List<Token> filterCollection(List<Token> collection, List<Token> preallocatedOut)
	{
		if (preallocatedOut == null)
		{
			preallocatedOut = new ArrayList<Token>();
		}
		else
		{
			preallocatedOut.clear();
		}

		for (Token t : collection)
		{
			if (isValidToken(t))
			{
				preallocatedOut.add(t);
			}
		}

		return preallocatedOut;
	}

	public abstract boolean isValidToken(Token t);
}