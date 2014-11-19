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

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;
import java.io.Reader;

public class TestStreamLemmasFilterWithOrigin extends BaseTokenStreamWithDictionaryTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            StreamLemmasFilter src = null;
            try {
                src = new StreamLemmasFilter(reader, getDictionary(false));
                src.setKeepOriginalWord(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Analyzer.TokenStreamComponents(src);
        }
    };

    /** blast some random strings through the analyzer */
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), a, 1000*RANDOM_MULTIPLIER);
    }

    /** test basic cases */
    @Repeat(iterations = 100)
    public void testBasics() throws IOException {

        checkOneTerm(a, "books", "books");
        checkOneTerm(a, "book", "book");
        checkOneTerm(a, "steven's", "steven's");
        checkOneTerm(a, "steven\u2019s", "steven's");
        //checkOneTerm(a, "steven\uFF07s", "steven's");

        assertAnalyzesTo(a, "בדיקה", new String[]{"בדיקה", "בדיקה"}, new int[] {0, 0}, new int[]{5 ,5}, new int[]{1,0});
        assertAnalyzesTo(a, "צה\"ל", new String[]{"צה\"ל", "צה\"ל"}, new int[]{0, 0}, new int[]{4, 4}, new int[]{1, 0});
        assertAnalyzesTo(a, "צה''ל", new String[]{"צה\"ל", "צה\"ל"}, new int[]{0, 0}, new int[]{5, 5}, new int[]{1, 0});

        checkAnalysisConsistency(random(), a, true, "בדיקה אחת שתיים", true);
    }

    public void testLemmatization() throws IOException {
        assertAnalyzesTo(a, "בדיקה", new String[]{"בדיקה", "בדיקה"}, new int[] {0, 0}, new int[]{5, 5});
        assertAnalyzesTo(a, "בדיקות", new String[]{"בדיקות", "בדיקה"}, new int[] {0, 0}, new int[]{6, 6});
        assertAnalyzesTo(a, "אימא", new String[]{"אימא", "אימא"}, new int[] {0, 0}, new int[]{4, 4});
        assertAnalyzesTo(a, "בדיקות אמא", new String[]{"בדיקות", "בדיקה", "אמא", "אימא"}, new int[] {0, 0, 7, 7}, new int[]{6, 6, 10, 10});
    }
}
