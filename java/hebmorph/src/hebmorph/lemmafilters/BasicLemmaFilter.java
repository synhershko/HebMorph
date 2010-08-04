/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
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
 **************************************************************************/

package hebmorph.lemmafilters;

import hebmorph.HebrewToken;
import hebmorph.Token;
import hebmorph.hspell.Constants.DMask;
import java.util.List;


/**
 BasicLemmaFilter will only filter collections with more than one lemma. For them, any lemma
 scored below 0.7 is probably a result of some heavy toleration, and will be ignored.

*/
public class BasicLemmaFilter extends LemmaFilterBase
{
	@Override
	public List<Token> filterCollection(List<Token> collection, List<Token> preallocatedOut)
	{
		if (collection.size() > 1)
		{
			return super.filterCollection(collection, preallocatedOut);
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isValidToken(Token t)
	{
		if (t instanceof HebrewToken)
		{
			HebrewToken ht = (HebrewToken)((t instanceof HebrewToken) ? t : null);

			// Pose a minimum score limit for words
			if (ht.getScore() < 0.7f)
			{
				return false;
			}

			// Pose a higher threshold to verbs (easier to get irrelevant verbs from toleration)
			if (((ht.getMask() & DMask.D_TYPEMASK) == DMask.D_VERB) && (ht.getScore() < 0.85f))
			{
				return false;
			}
		}
		return true;
	}
}