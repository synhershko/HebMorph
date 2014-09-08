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
package com.code972.hebmorph.hspell;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Constants.DMask;
import com.code972.hebmorph.hspell.Constants.PrefixType;

import java.util.HashMap;

public class LingInfo
{
	public static HashMap<String, Integer> buildPrefixTree(boolean allowHeHasheela)
	{
		String[] prefixes;
		int[] masks;
		if (allowHeHasheela)
		{
			prefixes = Constants.prefixes_H;
			masks = Constants.masks_H;
		}
		else
		{
			prefixes = Constants.prefixes_noH;
			masks = Constants.masks_noH;
		}

        HashMap<String, Integer> ret = new HashMap<>();
		for (int i = 0; prefixes[i] != null; i++)
		{
			ret.put(prefixes[i], masks[i]);
		}

		return ret;
	}

	// find the prefixes required by a word according to its details
	public static Integer DMask2ps(Integer dmask)
	{
		Integer specifier;
		if ((dmask & DMask.D_TYPEMASK) == DMask.D_VERB)
		{
			if ((dmask & DMask.D_TENSEMASK) == DMask.D_IMPERATIVE)
			{
				specifier = PrefixType.PS_IMPER;
			}
			else if ((dmask & DMask.D_TENSEMASK) != DMask.D_PRESENT)
			{
				specifier = PrefixType.PS_VERB;
			}
			else if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0))
			{
				specifier = PrefixType.PS_NONDEF;
			}
			else
			{
				specifier = PrefixType.PS_ALL;
			}
			/*TODO I feel that this may lead to a bug with ליפול and other infinitives that
             * did not loose their initial lamed.  I should correct this all the way from
             * woo.pl*/
			if ((dmask & DMask.D_TENSEMASK) == DMask.D_INFINITIVE)
			{
				specifier = PrefixType.PS_L;
			}
			else if ((dmask & DMask.D_TENSEMASK) == DMask.D_BINFINITIVE)
			{
				specifier = PrefixType.PS_B;
			}
		}
		else if (((dmask & DMask.D_TYPEMASK) == DMask.D_NOUN) || ((dmask & DMask.D_TYPEMASK) == DMask.D_ADJ))
		{
			if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0) || ((dmask& DMask.D_SPECNOUN) > 0))
			{
				specifier = PrefixType.PS_NONDEF;
			}
			else
			{
				specifier = PrefixType.PS_ALL;
			}
		}
		else
		{
			specifier = PrefixType.PS_ALL;
		}
		return specifier;
	}

	public static String DMask2HebrewString(Integer dmask)
	{
		String ret = "";

		switch (dmask & DMask.D_TYPEMASK)
		{
			case DMask.D_NOUN:
				ret = "ע";
				break;
			case DMask.D_VERB:
				ret = "פ";
				break;
			case DMask.D_ADJ:
				ret = "ת";
				break;
			case 0:
				ret = "x";
				break;
			//default: ret = "";
		}

		// In few cases, both masculine and faminine are possible
		if (((dmask & DMask.D_GENDERMASK) & DMask.D_MASCULINE) > 0)
		{
			ret += ",ז";
		}
		if (((dmask & DMask.D_GENDERMASK) & DMask.D_FEMININE) > 0)
		{
			ret += ",נ";
		}

		switch (dmask & DMask.D_GUFMASK)
		{
			case DMask.D_FIRST:
				ret += ",1";
				break;
			case DMask.D_SECOND:
				ret += ",2";
				break;
			case DMask.D_THIRD:
				ret += ",3";
				break;
			//default: ret += "";
		}
		switch (dmask & DMask.D_NUMMASK)
		{
			case DMask.D_SINGULAR:
				ret += ",יחיד";
				break;
			case DMask.D_DOUBLE:
				ret += ",זוגי";
				break;
			case DMask.D_PLURAL:
				ret += ",רבים";
				break;
			//default: ret += "";
		}
		switch (dmask & DMask.D_TENSEMASK)
		{
			case DMask.D_PAST:
				ret += ",עבר";
				break;
			case DMask.D_PRESENT:
				ret += ",הווה";
				break;
			case DMask.D_FUTURE:
				ret += ",עתיד";
				break;
			case DMask.D_IMPERATIVE:
				ret += ",ציווי";
				break;
			case DMask.D_INFINITIVE:
				ret += ",מקור";
				break;
			case DMask.D_BINFINITIVE:
				ret += ",מקור,ב";
				break;
			//default: ret += "";
		}
		if ((dmask & DMask.D_SPECNOUN) > 0)
		{
			ret += ",פרטי";
		}
		if ((dmask & DMask.D_OSMICHUT) > 0)
		{
			ret += ",סמיכות";
		}
		if ((dmask & DMask.D_OMASK) > 0)
		{
			ret+= ",כינוי/";
			switch (dmask & DMask.D_OGENDERMASK)
			{
				case DMask.D_OMASCULINE:
					ret += "ז";
					break;
				case DMask.D_OFEMININE:
					ret += "נ";
					break;
				//default: ret += "";
			}
			switch (dmask & DMask.D_OGUFMASK)
			{
				case DMask.D_OFIRST:
					ret += ",1";
					break;
				case DMask.D_OSECOND:
					ret += ",2";
					break;
				case DMask.D_OTHIRD:
					ret += ",3";
					break;
				//default: ret += "";
			}
			switch (dmask & DMask.D_ONUMMASK)
			{
				case DMask.D_OSINGULAR:
					ret += ",יחיד";
					break;
				case DMask.D_ODOUBLE:
					ret += ",זוגי";
					break;
				case DMask.D_OPLURAL:
					ret += ",רבים";
					break;
				//default: ret += "";
			}
		}

		return ret;
	}

	public static String DMask2EnglishString(Integer dmask)
	{
		String ret = "";
		switch (dmask & DMask.D_TYPEMASK)
		{
			case DMask.D_NOUN:
				ret = "Noun";
				break;
			case DMask.D_VERB:
				ret = "Verb";
				break;
			case DMask.D_ADJ:
				ret = "Adj";
				break;
			case 0:
				ret = "x";
				break;
			//default: ret = "";
		}

		// In few cases, both masculine and faminine are possible
		if (((dmask & DMask.D_GENDERMASK) & DMask.D_MASCULINE) > 0)
		{
			ret += ",Masculine";
		}
		if (((dmask & DMask.D_GENDERMASK) & DMask.D_FEMININE) > 0)
		{
			ret += ",Feminine";
		}

		switch (dmask & DMask.D_GUFMASK)
		{
			case DMask.D_FIRST:
				ret += ",1st";
				break;
			case DMask.D_SECOND:
				ret += ",2nd";
				break;
			case DMask.D_THIRD:
				ret += ",3rd";
				break;
			//default: ret += "";
		}
		switch (dmask & DMask.D_NUMMASK)
		{
			case DMask.D_SINGULAR:
				ret += ",Singular";
				break;
			case DMask.D_DOUBLE:
				ret += ",Dual";
				break;
			case DMask.D_PLURAL:
				ret += ",Plural";
				break;
			//default: ret += "";
		}
		switch (dmask & DMask.D_TENSEMASK)
		{
			case DMask.D_PAST:
				ret += ",Past";
				break;
			case DMask.D_PRESENT:
				ret += ",Present";
				break;
			case DMask.D_FUTURE:
				ret += ",Future";
				break;
			case DMask.D_IMPERATIVE:
				ret += ",Imperative";
				break;
			case DMask.D_INFINITIVE:
				ret += ",Infinitive";
				break;
			case DMask.D_BINFINITIVE:
				ret += ",B,Infinitive";
				break;
			//default: ret += "";
		}
		if ((dmask & DMask.D_SPECNOUN) > 0)
		{
			ret += ",Proper";
		}
		if ((dmask & DMask.D_OSMICHUT) > 0)
		{
			ret += ",Construct";
		}
		if ((dmask & DMask.D_OMASK) > 0)
		{
			ret += ",Pronominal/";
			switch (dmask & DMask.D_OGENDERMASK)
			{
				case DMask.D_OMASCULINE:
					ret += "Masculine";
					break;
				case DMask.D_OFEMININE:
					ret += "Feminine";
					break;
				//default: ret += "";
			}
			switch (dmask & DMask.D_OGUFMASK)
			{
				case DMask.D_OFIRST:
					ret += ",1st";
					break;
				case DMask.D_OSECOND:
					ret += ",2nd";
					break;
				case DMask.D_OTHIRD:
					ret += ",3rd";
					break;
				//default: ret += "";
			}
			switch (dmask & DMask.D_ONUMMASK)
			{
				case DMask.D_OSINGULAR:
					ret += ",Singular";
					break;
				case DMask.D_ODOUBLE:
					ret += ",Dual";
					break;
				case DMask.D_OPLURAL:
					ret += ",Plural";
					break;
				//default: ret += "";
			}
		}

		return ret;
	}
}