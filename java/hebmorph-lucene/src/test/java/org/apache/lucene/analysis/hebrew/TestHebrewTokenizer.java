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
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class TestHebrewTokenizer extends BaseTokenStreamTestCase {

    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            DictRadix<Byte> specialCases = new DictRadix<>();
            specialCases.addNode("c++", (byte)0);

            final HebrewTokenizer src = new HebrewTokenizer(reader, HSpellLoader.readDefaultPrefixes(), specialCases);
            return new Analyzer.TokenStreamComponents(src);
        }
    };

    /** blast some random strings through the analyzer */
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), a, 1000*RANDOM_MULTIPLIER);
    }

    /** test stopwords and stemming */
    @Repeat(iterations = 100)
    public void testBasics() throws IOException {
        checkOneTerm(a, "books", "books");
        checkOneTerm(a, "book", "book");
        checkOneTerm(a, "steven's", "steven's");
        checkOneTerm(a, "steven\u2019s", "steven's");
        //checkOneTerm(a, "steven\uFF07s", "steven's");

        checkOneTerm(a, "בדיקה", "בדיקה");
        checkOneTerm(a, "צה\"ל", "צה\"ל");
        checkOneTerm(a, "צה''ל", "צה\"ל");

        checkAnalysisConsistency(random(), a, true, "בדיקה אחת שתיים", true);
    }

    public void testHyphen() throws Exception {
        assertTokenStreamContents(tokenStream("some-dashed-phrase"),
                new String[] { "some", "dashed", "phrase" });
    }

    public void testSpecialCases() throws Exception {
        assertTokenStreamContents(tokenStream("a++ b++ c++"),
                new String[] { "a", "b", "c++" });
        assertTokenStreamContents(tokenStream("c++\nc++! c++, c++. (c++)"),
                new String[] { "c++", "c++", "c++", "c++", "c++" });
    }

    TokenStream tokenStream(String text) throws IOException {
        Reader reader = new StringReader(text);
        return a.tokenStream("foo", reader);
    }
}
