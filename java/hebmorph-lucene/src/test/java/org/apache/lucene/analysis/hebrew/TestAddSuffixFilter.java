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

import org.apache.lucene.analysis.*;

import java.io.IOException;
import java.io.Reader;

public class TestAddSuffixFilter extends BaseTokenStreamTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = new MockTokenizer(reader, MockTokenizer.KEYWORD, false);
            return new TokenStreamComponents(t, new AddSuffixFilter(t, '$') {
                @Override
                protected void handleCurrentToken() {
                    duplicateCurrentToken();
                    suffixCurrent();
                }
            });
        }
    };

    Analyzer a2 = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = new MockTokenizer(reader, MockTokenizer.KEYWORD, false);
            return new TokenStreamComponents(t, new AddSuffixFilter(t, '$') {
                @Override
                protected void handleCurrentToken() {
                    suffixCurrent();
                }
            });
        }
    };

    public void testBasicTerms() throws IOException {
        assertAnalyzesTo(a, "book", new String[]{"book$", "book"});
        assertAnalyzesTo(a2, "book", new String[]{"book$"});
    }
}
