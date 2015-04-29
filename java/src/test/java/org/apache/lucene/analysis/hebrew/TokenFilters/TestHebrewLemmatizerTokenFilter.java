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
package org.apache.lucene.analysis.hebrew.TokenFilters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.BaseTokenStreamWithDictionaryTestCase;
import org.apache.lucene.analysis.hebrew.HebrewTokenizer;

import java.io.IOException;
import java.io.Reader;

public class TestHebrewLemmatizerTokenFilter extends BaseTokenStreamWithDictionaryTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            Tokenizer src = null;
            TokenStream tok = null;
            try {
                src = new HebrewTokenizer(reader, getDictionary().getPref());
                tok = new HebrewLemmatizerTokenFilter(src, getDictionary());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TokenStreamComponents(src, tok);
        }
    };

    public void testBasicTerms() throws IOException {
        assertAnalyzesTo(a, "books", new String[]{"books", "books"});
        assertAnalyzesTo(a, "steven's", new String[]{"steven's", "steven's"});
        assertAnalyzesTo(a, "steven\u2019s", new String[]{"steven's", "steven's"});

        assertAnalyzesTo(a, "57", new String[]{"57"});

        assertAnalyzesTo(a, "בדיקה", new String[]{"בדיקה", "בדיקה"});
        assertAnalyzesTo(a, "בדיקות", new String[]{"בדיקות", "בדיקה"});
        assertAnalyzesTo(a, "אימא", new String[]{"אימא", "אימא" });
        assertAnalyzesTo(a, "אמא", new String[]{"אמא", "אימא"});
        assertAnalyzesTo(a, "צה\"ל", new String[]{"צה\"ל", "צה\"ל"});
        assertAnalyzesTo(a, "צה''ל", new String[]{"צה\"ל", "צה\"ל"});

    }

    public void testBasicStreams() throws IOException {
        assertAnalyzesTo(a, "one two three test", new String[]{"one","one", "two","two","three","three","test","test"});
        assertAnalyzesTo(a, "בדיקה אחת שתיים שולחן", new String[]{"בדיקה","בדיקה", "אחת","אחד","שתיים","שניים","שולחן","שולחן", "שולח"});
        assertAnalyzesTo(a, "one אחת 57", new String[]{"one","one", "אחת","אחד","57"});
    }
}
