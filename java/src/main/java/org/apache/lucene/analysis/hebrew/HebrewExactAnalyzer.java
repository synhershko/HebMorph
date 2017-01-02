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
package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.datastructures.DictHebMorph;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hebrew.TokenFilters.AddSuffixTokenFilter;
import org.apache.lucene.analysis.hebrew.TokenFilters.NiqqudFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import java.io.IOException;

public class HebrewExactAnalyzer extends HebrewAnalyzer {
    public HebrewExactAnalyzer(DictHebMorph dict) throws IOException {
        super(dict);
    }

    public HebrewExactAnalyzer() throws IOException {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        // on exact - we don't care about suffixes at all, we always output original word with suffix only
        final HebrewTokenizer src = new HebrewTokenizer(dict.getPref(), SPECIAL_TOKENIZATION_CASES);
        src.setSuffixForExactMatch(originalTermSuffix);
        TokenStream tok = new NiqqudFilter(src);
        tok = new ASCIIFoldingFilter(tok);
        tok = new LowerCaseFilter(tok);
        tok = new AddSuffixTokenFilter(tok, '$');
        return new TokenStreamComponents(src, tok);
    }
}
