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

import java.io.IOException;

public class TestHebrewIndexingAnalyzer extends BaseTokenStreamTestCase {
    public void testDictionaryLoaded() throws IOException {
        HebrewAnalyzer a = TestBase.getHebrewIndexingAnalyzer();
        assertEquals(HebrewAnalyzer.WordType.HEBREW, a.isRecognizedWord("אימא", false));
        assertEquals(HebrewAnalyzer.WordType.HEBREW, a.isRecognizedWord("בדיקה", false));
        assertEquals(HebrewAnalyzer.WordType.UNRECOGNIZED, a.isRecognizedWord("ץץץץץץ", false));
    }

    public void testBasics() throws IOException {
        Analyzer a = TestBase.getHebrewIndexingAnalyzer();

        assertAnalyzesTo(a, "אימא", new String[]{"אימא$", "אימא"}); // recognized word, lemmatized
        assertAnalyzesTo(a, "אימא$", new String[]{"אימא$", "אימא"}); // recognized word, lemmatized
        assertAnalyzesTo(a, "בדיקהבדיקה", new String[]{"בדיקהבדיקה$", "בדיקהבדיקה"}); // OOV
        assertAnalyzesTo(a, "בדיקהבדיקה$", new String[]{"בדיקהבדיקה$", "בדיקהבדיקה"}); // OOV
        assertAnalyzesTo(a, "ץץץץץץץץץץץ", new String[]{}); // Invalid, treated as noise
        assertAnalyzesTo(a, "ץץץץץץץץץץץ$", new String[]{}); // Invalid, treated as noise

        assertAnalyzesTo(a, "אנציקלופדיה", new String[]{"אנציקלופדיה$", "אנציקלופדיה"});
        assertAnalyzesTo(a, "אנצקלופדיה", new String[]{"אנצקלופדיה$", "אנציקלופדיה"});

        assertAnalyzesTo(a, "שמלות", new String[]{"שמלות$", "שמלה", "מל"});

        // Test non-Hebrew
        assertAnalyzesTo(a, "book", new String[]{"book$", "book"});
        assertAnalyzesTo(a, "book$", new String[]{"book$", "book"});
        assertAnalyzesTo(a, "steven's", new String[]{"steven's$", "steven's"});
        assertAnalyzesTo(a, "steven\u2019s", new String[]{"steven's$", "steven's"});
        //assertAnalyzesTo(a, "steven\uFF07s", new String[]{"steven's$", "steven's"});
        checkOneTerm(a, "3", "3");
    }
}
