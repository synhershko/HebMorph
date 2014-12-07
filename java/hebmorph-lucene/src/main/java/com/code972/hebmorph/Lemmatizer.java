/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.datastructures.RealSortedList;
import com.code972.hebmorph.datastructures.RealSortedList.SortOrder;
import com.code972.hebmorph.hspell.LingInfo;

import java.util.HashMap;
import java.util.List;

public class Lemmatizer {
    private final DictHebMorph dictHeb;

    public Lemmatizer(final DictHebMorph dict) {
        this.dictHeb = dict;
    }

    public boolean isLegalPrefix(final String str) {
        return dictHeb.getPref().containsKey(str);
    }

    // See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
    public String tryStrippingPrefix(String word) {
        // TODO: Make sure we conform to the academy rules as closely as possible

        int firstQuote = word.indexOf('"');

        if (firstQuote > -1 && firstQuote < word.length() - 2) {
            if (isLegalPrefix(word.substring(0, firstQuote))) {
                return word.substring(firstQuote + 1, firstQuote + 1 + word.length() - firstQuote - 1);
            }
        }

        int firstSingleQuote = word.indexOf('\'');
        if (firstSingleQuote == -1) {
            return word;
        }

        if ((firstQuote > -1) && (firstSingleQuote > firstQuote)) {
            return word;
        }

        if (isLegalPrefix(word.substring(0, firstSingleQuote))) {
            return word.substring(firstSingleQuote + 1, firstSingleQuote + 1 + word.length() - firstSingleQuote - 1);
        }

        return word;
    }

    /**
     * Removes all Niqqud character from a word
     *
     * @param word A string to remove Niqqud from
     * @return A new word "clean" of Niqqud chars
     */
    static public String removeNiqqud(final String word) {
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
        DictRadix<MorphData> m_dict = dictHeb.getRadix();
        HashMap<String, Integer> m_pref = dictHeb.getPref();

        try {
            md = m_dict.lookup(word);
        } catch (IllegalArgumentException e) {
            md = null;
        }
        if (md != null) {
            for (int result = 0; result < md.getLemmas().length; result++) {
                ret.addUnique(new HebrewToken(word, (byte) 0, md.getLemmas()[result], 1.0f));
            }
        } else if (word.endsWith("'")) { // Try ommitting closing Geresh
            try {
                md = m_dict.lookup(word.substring(0, word.length() - 1));
            } catch (IllegalArgumentException e) {
                md = null;
            }
            if (md != null) {
                for (int result = 0; result < md.getLemmas().length; result++) {
                    ret.addUnique(new HebrewToken(word, (byte) 0, md.getLemmas()[result], 1.0f));
                }
            }
        }

        prefLen = 0;
        while (true) {
            // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
            if (word.length() - prefLen < 2)
                break;
            if ((prefixMask = m_pref.get(word.substring(0, ++prefLen))) == null)
                break;

            try {
                md = m_dict.lookup(word.substring(prefLen));
            } catch (IllegalArgumentException e) {
                md = null;
            }
            if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
                for (int result = 0; result < md.getLemmas().length; result++) {
                    if ((LingInfo.DMask2ps(md.getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                        ret.addUnique(new HebrewToken(word, prefLen, md.getLemmas()[result], 0.9f));
                    }
                }
            }
        }
        return ret;
    }

    public List<HebrewToken> lemmatizeTolerant(final String word) {
        final RealSortedList<HebrewToken> ret = new RealSortedList<HebrewToken>(SortOrder.Desc);
        DictRadix<MorphData> m_dict = dictHeb.getRadix();
        HashMap<String, Integer> m_pref = dictHeb.getPref();
        // Don't try tolerating long words. Longest Hebrew word is 19 chars long
        // http://en.wikipedia.org/wiki/Longest_words#Hebrew
        if (word.length() > 20) {
            return ret;
        }

        byte prefLen = 0;
        Integer prefixMask;

        List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.lookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
        if (tolerated != null) {
            for (DictRadix<MorphData>.LookupResult lr : tolerated) {
                for (int result = 0; result < lr.getData().getLemmas().length; result++) {
                    ret.addUnique(new HebrewToken(lr.getWord(), (byte) 0, lr.getData().getLemmas()[result], lr.getScore()));
                }
            }
        }

        prefLen = 0;
        while (true) {
            // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
            if (word.length() - prefLen < 2)
                break;

            if ((prefixMask = m_pref.get(word.substring(0, ++prefLen))) == null)
                break;

            tolerated = m_dict.lookupTolerant(word.substring(prefLen), LookupTolerators.TolerateEmKryiaAll);
            if (tolerated != null) {
                for (DictRadix<MorphData>.LookupResult lr : tolerated) {
                    for (int result = 0; result < lr.getData().getLemmas().length; result++) {
                        if ((LingInfo.DMask2ps(lr.getData().getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                            ret.addUnique(new HebrewToken(word.substring(0, prefLen) + lr.getWord(), prefLen, lr.getData().getLemmas()[result], lr.getScore() * 0.9f));
                        }
                    }
                }
            }
        }
        return ret;
    }

    public final WordType isRecognizedWord(final String word, final boolean tolerate) {
        byte prefLen = 0;
        Integer prefixMask;
        MorphData md;
        DictRadix<MorphData> m_dict = dictHeb.getRadix();
        HashMap<String, Integer> m_pref = dictHeb.getPref();

        try {
            if (m_dict.lookup(word) != null) return WordType.HEBREW;
        } catch (IllegalArgumentException e) {
        }

        if (word.endsWith("'")) { // Try ommitting closing Geresh
            try {
                if (m_dict.lookup(word.substring(0, word.length() - 1)) != null) return WordType.HEBREW;
            } catch (IllegalArgumentException e) {
            }
        }

        prefLen = 0;
        while (true) {
            // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
            if (word.length() - prefLen < 2)
                break;

            if ((prefixMask = m_pref.get(word.substring(0, ++prefLen))) == null)
                break;

            try {
                md = m_dict.lookup(word.substring(prefLen));
            } catch (IllegalArgumentException e) {
                md = null;
            }
            if ((md != null) && ((md.getPrefixes() & prefixMask) > 0)) {
                for (int result = 0; result < md.getLemmas().length; result++) {
                    if ((LingInfo.DMask2ps(md.getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                        return WordType.HEBREW_WITH_PREFIX;
                    }
                }
            }
        }

        if (tolerate) {
            // Don't try tolerating long words. Longest Hebrew word is 19 chars long
            // http://en.wikipedia.org/wiki/Longest_words#Hebrew
            if (word.length() > 20) {
                return WordType.UNRECOGNIZED;
            }

            List<DictRadix<MorphData>.LookupResult> tolerated = m_dict.lookupTolerant(word, LookupTolerators.TolerateEmKryiaAll);
            if (tolerated != null && tolerated.size() > 0) {
                return WordType.HEBREW_TOLERATED;
            }

            prefLen = 0;
            while (true) {
                // Make sure there are at least 2 letters left after the prefix (the words של, שלא for example)
                if (word.length() - prefLen < 2)
                    break;

                if ((prefixMask = m_pref.get(word.substring(0, ++prefLen))) == null)
                    break;

                tolerated = m_dict.lookupTolerant(word.substring(prefLen), LookupTolerators.TolerateEmKryiaAll);
                if (tolerated != null) {
                    for (DictRadix<MorphData>.LookupResult lr : tolerated) {
                        for (int result = 0; result < lr.getData().getLemmas().length; result++) {
                            if ((LingInfo.DMask2ps(lr.getData().getLemmas()[result].getDescFlag()) & prefixMask) > 0) {
                                return WordType.HEBREW_TOLERATED_WITH_PREFIX;
                            }
                        }
                    }
                }
            }
        }

        return WordType.UNRECOGNIZED;
    }
}