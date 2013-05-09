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
package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.datastructures.RealSortedList;
import com.code972.hebmorph.datastructures.RealSortedList.SortOrder;
import com.code972.hebmorph.hspell.LingInfo;

import java.util.List;

public class Lemmatizer
{
	private final DictRadix<MorphData> m_dict;
	private final DictRadix<Integer> m_prefixes;
    private DictRadix<MorphData> customWords;

	public Lemmatizer(final DictRadix<MorphData> dict, final boolean allowHeHasheela) {
        this(dict, LingInfo.buildPrefixTree(allowHeHasheela));
	}

    public Lemmatizer(final DictRadix<MorphData> dict, final DictRadix<Integer> prefixes) {
        this.m_dict = dict;
        this.m_prefixes = prefixes;
    }

	public boolean isLegalPrefix(final String str) {
        try {
            m_prefixes.lookup(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
	}

	// See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
	public String tryStrippingPrefix(String word)
	{
		// TODO: Make sure we conform to the academy rules as closely as possible

		int firstQuote = word.indexOf('"');

		if (firstQuote > -1 && firstQuote < word.length() - 2)
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
	static public String removeNiqqud(final String word)
	{
		final int length = word.length();
		final StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			if ((word.charAt(i) < 1455) || (word.charAt(i) > 1476)) { // current position is not a Niqqud character
				sb.append(word.charAt(i));
			}
		}
		return sb.toString();
	}

	public List<HebrewToken> lemmatize(final String word) {
		final RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

        byte prefLen = 0;
        Integer prefixMask;
        MorphData md = null;

        // Lookup the word in the custom words list. It is guaranteed to have only one lemma for a word,
        // so we can always access the first entry of a record - if we got any
        // If we find any results, we can immediately return
        if (customWords != null){
            try {
                md = customWords.lookup(word);
            } catch (IllegalArgumentException e) {
            }

            if (md != null) { // exact match was found in the custom words list
                ret.addUnique(new HebrewToken(word, (byte)0, md.getDescFlags()[0], md.getLemmas()[0], 1.0f));
                return ret;
            } else { // try stripping prefixes
                while (true) {
                    // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                    if (word.length() - prefLen < 2)
                        break;

                    try {
                        prefixMask = m_prefixes.lookup(word.substring(0, ++prefLen));
                    } catch (IllegalArgumentException e) {
                        break;
                    }

                    try {
                        md = customWords.lookup(word.substring(prefLen));
                    } catch (IllegalArgumentException e) {
                        md = null;
                    }
                    if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
                        if ((LingInfo.DMask2ps(md.getDescFlags()[0]) & prefixMask) > 0) {
                            ret.addUnique(new HebrewToken(word, prefLen, md.getDescFlags()[0], md.getLemmas()[0], 0.9f));
                        }
                    }
                }
                if (ret.size() > 0) return  ret;
            }
        }

        // Continue with looking up the word in the standard dictionary
        try {
            md = m_dict.lookup(word);
        } catch (IllegalArgumentException e) {
            md = null;
        }
		if (md != null) {
			for (int result = 0; result < md.getLemmas().length; result++) {
				ret.addUnique(new HebrewToken(word, (byte)0, md.getDescFlags()[result], md.getLemmas()[result], 1.0f));
			}
		} else if (word.endsWith("'")) { // Try ommitting closing Geresh
            try {
                md = m_dict.lookup(word.substring(0, word.length() - 1));
            } catch (IllegalArgumentException e) {
                md = null;
            }
			if (md != null) {
				for (int result = 0; result < md.getLemmas().length; result++) {
					ret.addUnique(new HebrewToken(word, (byte)0, md.getDescFlags()[result], md.getLemmas()[result], 1.0f));
				}
			}
		}

        prefLen = 0;
		while (true) {
			// Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
			if (word.length() - prefLen < 2)
				break;

            try {
			    prefixMask = m_prefixes.lookup(word.substring(0, ++prefLen));
            } catch (IllegalArgumentException e) {
                break;
            }

            try {
                md = m_dict.lookup(word.substring(prefLen));
            } catch (IllegalArgumentException e) {
                md = null;
            }
			if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
				for (int result = 0; result < md.getLemmas().length; result++) {
					if ((LingInfo.DMask2ps(md.getDescFlags()[result]) & prefixMask) > 0) {
						ret.addUnique(new HebrewToken(word, prefLen, md.getDescFlags()[result], md.getLemmas()[result], 0.9f));
					}
				}
			}
		}
		return ret;
	}

	public List<HebrewToken> lemmatizeTolerant(final String word) {
		final RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);

        // Don't try tolerating long words. Longest Hebrew word is 19 chars long
        // http://en.wikipedia.org/wiki/Longest_words#Hebrew
        if (word.length() > 20) {
            return ret;
        }

		byte prefLen = 0;
		Integer prefixMask;

		List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.lookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
		if (tolerated != null)
		{
			for (DictRadix<MorphData>.LookupResult lr : tolerated)
			{
				for (int result = 0; result < lr.getData().getLemmas().length; result++)
				{
					ret.addUnique(new HebrewToken(lr.getWord(), (byte)0, lr.getData().getDescFlags()[result], lr.getData().getLemmas()[result], lr.getScore()));
				}
			}
		}

		prefLen = 0;
		while (true)
		{
			// Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
			if (word.length() - prefLen < 2)
				break;

            try {
			    prefixMask = m_prefixes.lookup(word.substring(0, ++prefLen));
            } catch (IllegalArgumentException e) {
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
        return ret;
	}

    public DictRadix<MorphData> getCustomWords() {
        return customWords;
    }

    public void setCustomWords(DictRadix<MorphData> customWords) {
        this.customWords = customWords;
    }
}