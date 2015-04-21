/**
 * ************************************************************************
 * Copyright (C) 2010-2015 by                                            *
 * Itamar Syn-Hershko <itamar at code972 dot com>                     *
 * *
 * This program is free software; you can redistribute it and/or modify  *
 * it under the terms of the GNU Affero General Public License           *
 * version 3, as published by the Free Software Foundation.              *
 * *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU Affero General Public License for more details.                   *
 * *
 * You should have received a copy of the GNU Affero General Public      *
 * License along with this program; if not, see                          *
 * <http://www.gnu.org/licenses/>.                                       *
 * ************************************************************************
 */
package org.apache.lucene.analysis.hebrew.TokenFilters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hebrew.HebrewTokenTypeAttribute;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;

public final class AddSuffixToTokenFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final HebrewTokenTypeAttribute hebTypeAtt = addAttribute(HebrewTokenTypeAttribute.class);

    private final Character suffix;

    public AddSuffixToTokenFilter(final TokenStream input, final Character suffixToAdd) {
        super(input);
        this.suffix = suffixToAdd;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) { // reached EOS -- return null
            return false;
        }
        if (hebTypeAtt.isHebrew() || hebTypeAtt.getType() == HebrewTokenTypeAttribute.HebrewType.NonHebrew) {
            termAtt.append(suffix);
        }

        return true;
    }
}
