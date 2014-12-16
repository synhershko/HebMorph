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

import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;

import java.io.IOException;

public class TestHebrewExactAnalyzer extends BaseTokenStreamTestCase {
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), HSpellLoader.getHebrewExactAnalyzer(), 1000 * RANDOM_MULTIPLIER);
    }

    public void testBasics() throws IOException {
        HebrewExactAnalyzer a = HSpellLoader.getHebrewExactAnalyzer();

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
}
