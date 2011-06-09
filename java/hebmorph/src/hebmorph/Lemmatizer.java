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
package hebmorph;
 
import hebmorph.datastructures.DictRadix;
import hebmorph.datastructures.RealSortedList;
import hebmorph.datastructures.RealSortedList.SortOrder;
import hebmorph.hspell.LingInfo;
import java.util.List;

public class Lemmatizer
{
	private DictRadix<MorphData> m_dict;
	private DictRadix<Integer> m_prefixes;

	public Lemmatizer(DictRadix<MorphData> dict, boolean allowHeHasheela)
	{
        m_dict = dict;
		m_prefixes = LingInfo.buildPrefixTree(allowHeHasheela);
	}

	public boolean isLegalPrefix(String str)
	{
		Integer lookup = m_prefixes.lookup(str);
		if ((lookup!=null) && (lookup > 0))
		{
			return true;
		}

		return false;
	}

	// See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
	public String tryStrippingPrefix(String word)
	{
		// TODO: Make sure we conform to the academy rules as closely as possible

		int firstQuote = word.indexOf('"');

		if (firstQuote > -1)
		{
			if (isLegalPrefix(word.substring(0, firstQuote)))
			{
				return word.substring(firstQuote + 1, firstQuote + 1 + word.length() - firstQuote - 1);
			}
		}

		int firstSingleQuote = word.indexOf('\'');
		if (firstSingleQuote == -1)
		{
			return word;
		}

		if ((firstQuote > -1) && (firstSingleQuote > firstQuote))
		{
			return word;
		}

		if (isLegalPrefix(word.substring(0, firstSingleQuote)))
		{
			return word.substring(firstSingleQuote + 1, firstSingleQuote + 1 + word.length() - firstSingleQuote - 1);
		}

		return word;
	}

	/**
	 Removes all Niqqud character from a word

	 @param word A string to remove Niqqud from
	 @return A new word "clean" of Niqqud chars
	*/
	static public String removeNiqqud(String word)
	{
		int length = word.length();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			if ((word.charAt(i) < 1455) || (word.charAt(i) > 1476)) // current position is not a Niqqud character
			{
				sb.append(word.charAt(i));
			}
		}
		return sb.toString();
	}

	public List<HebrewToken> lemmatize(String word)
	{
		// TODO: Verify word to be non-empty and contain Hebrew characters?

		RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

		MorphData md = m_dict.lookup(word);
		if (md != null)
		{
			for (int result = 0; result < md.getLemmas().length; result++)
			{
				ret.addUnique(new HebrewToken(word, 0, md.getDescFlags()[result], md.getLemmas()[result], 1.0f));
			}
		}
		else if (word.endsWith("'")) // Try ommitting closing Geresh
		{
			md = m_dict.lookup(word.substring(0, word.length() - 1));
			if (md != null)
			{
				for (int result = 0; result < md.getLemmas().length; result++)
				{
					ret.addUnique(new HebrewToken(word, 0, md.getDescFlags()[result], md.getLemmas()[result], 1.0f));
				}
			}
		}

		int prefLen = 0;
		Integer prefixMask;
		while (true)
		{
			// Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
			if (word.length() - prefLen < 2)
			{
				break;
			}

			prefixMask = m_prefixes.lookup(word.substring(0, ++prefLen));
			if ((prefixMask== null) ||  (prefixMask== 0)) // no such prefix
			{
				break;
			}

			md = m_dict.lookup(word.substring(prefLen));
			if ((md != null) && ((md.getPrefixes() & prefixMask) > 0))
			{
				for (int result = 0; result < md.getLemmas().length; result++)
				{
					if ((LingInfo.DMask2ps(md.getDescFlags()[result]) & prefixMask) > 0)
					{
						ret.addUnique(new HebrewToken(word, prefLen, md.getDescFlags()[result], md.getLemmas()[result], 0.9f));
					}
				}
			}
		}

		if (ret.size() > 0)
		{
			return ret;
		}
		return null;
	}

	public List<HebrewToken> lemmatizeTolerant(String word)
	{
		// TODO: Verify word to be non-empty and contain Hebrew characters?

		RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

		int prefLen = 0;
		Integer prefixMask;

		List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.lookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
		if (tolerated != null)
		{
			for (DictRadix<MorphData>.LookupResult lr : tolerated)
			{
				for (int result = 0; result < lr.getData().getLemmas().length; result++)
				{
					ret.addUnique(new HebrewToken(lr.getWord(), 0, lr.getData().getDescFlags()[result], lr.getData().getLemmas()[result], lr.getScore()));
				}
			}
		}

		prefLen = 0;
		while (true)
		{
			// Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
			if (word.length() - prefLen < 2)
			{
				break;
			}

			prefixMask = m_prefixes.lookup(word.substring(0, ++prefLen));
			if ((prefixMask ==null) || (prefixMask == 0)) // no such prefix
			{
				break;
			}

			tolerated = m_dict.lookupTolerant(word.substring(prefLen), LookupTolerators.TolerateEmKryiaAll);
			if (tolerated != null)
			{
				for (DictRadix<MorphData>.LookupResult lr : tolerated)
				{
					for (int result = 0; result < lr.getData().getLemmas().length; result++)
					{
						if ((LingInfo.DMask2ps(lr.getData().getDescFlags()[result]) & prefixMask) > 0)
						{
							ret.addUnique(new HebrewToken(word.substring(0, prefLen) + lr.getWord(), prefLen, lr.getData().getDescFlags()[result], lr.getData().getLemmas()[result], lr.getScore() * 0.9f));
						}
					}
				}
			}
		}

		if (ret.size() > 0)
		{
			return ret;
		}
		return null;
	}
}