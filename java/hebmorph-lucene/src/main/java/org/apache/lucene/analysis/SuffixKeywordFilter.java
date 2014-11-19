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
package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

public class SuffixKeywordFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    //    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final Character suffix;

    public SuffixKeywordFilter(final TokenStream input, final Character suffixToAdd) {
        super(input);
        this.suffix = suffixToAdd;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) { // reached EOS -- return null
            return false;
        }

        // If the filter is disabled, or this is not a keyword, skip and allow the stem / bigram to be returned
        if (!keywordAtt.isKeyword() || suffix == null || CommonGramsFilter.GRAM_TYPE.equals(typeAtt.type())) {
            return true;
        }

        termAtt.append(suffix);

        return true;
    }
}