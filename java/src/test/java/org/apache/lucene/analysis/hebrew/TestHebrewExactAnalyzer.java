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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;

public class TestHebrewExactAnalyzer extends BaseTokenStreamTestCase {
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), TestBase.getHebrewExactAnalyzer(), 1000 * RANDOM_MULTIPLIER);
    }

    public void testBasics() throws IOException {
        HebrewExactAnalyzer a = TestBase.getHebrewExactAnalyzer();

        checkOneTerm(a, "בדיקה", "בדיקה$");
        checkOneTerm(a, "בדיקה$", "בדיקה$");

        // test non-hebrew
        checkOneTerm(a, "books", "books$");
        checkOneTerm(a, "book", "book$");
        checkOneTerm(a, "book$", "book$");
        checkOneTerm(a, "steven's", "steven's$");
        checkOneTerm(a, "steven\u2019s", "steven's$");
        checkOneTerm(a, "3", "3");
        //checkOneTerm(a, "steven\uFF07s", "steven");
    }

    public void testRegress() throws IOException {
        Analyzer analyzer = TestBase.getHebrewExactAnalyzer();
        String input = TestBase.readFileToString("./../test-files/1371379887130490.txt");
        String[] output = {"שחזור$", "מידע$", "מדיסק$", "קשיח$", "מחשבים$", "וטכנולוגיה$", "פורום$", "סטודנטים$", "של$", "אוניברסיטת$", "בן$",
                "גוריון$", "פורסם$", "היום$", "09", "23", "שלום$", "רציתי$", "לדעת$", "אם$", "יש$", "למישהו$", "המלצה$", "על$", "בית$", "עסק$",
                "שיעזור$", "לי$", "לשחזר$", "מידע$", "מדיסק$", "קשיח$", "שלי$", "כמובן$", "שיהיה$", "כמה$", "שיותר$", "טוב$", "וזול$", "תודה$"};
        assertAnalyzesTo(analyzer, input, output);
    }
}
