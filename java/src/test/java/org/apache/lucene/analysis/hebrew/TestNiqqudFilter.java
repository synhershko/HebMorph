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
package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.TokenFilters.NiqqudFilter;

import java.io.IOException;
import java.io.Reader;

public class TestNiqqudFilter extends BaseTokenStreamTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = new MockTokenizer(reader, MockTokenizer.KEYWORD, false);
            return new TokenStreamComponents(t, new NiqqudFilter(t));
        }
    };

    /**
     * blast some random strings through the analyzer
     */
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), a, 1000 * RANDOM_MULTIPLIER);
    }

    public void testBasicTerms() throws IOException {
        checkOneTerm(a, "foo", "foo");
    }
}
