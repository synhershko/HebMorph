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
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.TokenFilters.AddSuffixTokenFilter;

import java.io.IOException;
import java.io.Reader;

public class TestAddSuffixFilter extends BaseTokenStreamWithDictionaryTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = null;
            try {
                t = new HebrewTokenizer(reader, getDictionary().getPref());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TokenStreamComponents(t, new AddSuffixTokenFilter(t, '$'));
        }
    };

    public void testBasicTerms() throws IOException {
        assertAnalyzesTo(a, "book", new String[]{"book$"});
        assertAnalyzesTo(a, "שלום", new String[]{"שלום$"});
        assertAnalyzesTo(a, "123", new String[]{"123"});
        assertAnalyzesTo(a, "book שלום 123", new String[]{"book$", "שלום$", "123"});

    }
}
