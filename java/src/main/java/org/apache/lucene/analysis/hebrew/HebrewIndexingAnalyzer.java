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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.hebrew.TokenFilters.*;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import java.io.IOException;
import java.io.Reader;

public class HebrewIndexingAnalyzer extends HebrewAnalyzer {
    public HebrewIndexingAnalyzer(DictHebMorph dict) throws IOException {
        super(dict);
    }

    public HebrewIndexingAnalyzer() throws IOException {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        // on indexing we should always keep both the stem and marked original word
        // will ignore $ && will always output all lemmas + origin word$
        // basically, if analyzerType == AnalyzerType.INDEXING)
        // TODO: use special tokenization cases, maybe common words too
        HebrewTokenizer src = new HebrewTokenizer(reader, dict.getPref());
        src.setSuffixForExactMatch(originalTermSuffix);
        TokenStream tok = new NiqqudFilter(src);
        tok = new ASCIIFoldingFilter(tok);
        tok = new LowerCaseFilter(tok);
        tok = new HebrewLemmatizerTokenFilter(tok, dict);
        tok = new AddSuffixToTokenFilter(tok, '$');
        return new TokenStreamComponents(src, tok);
    }

}
