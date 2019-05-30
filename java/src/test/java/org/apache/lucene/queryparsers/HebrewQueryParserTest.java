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
package org.apache.lucene.queryparsers;

import org.apache.lucene.analysis.hebrew.TestBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

public class HebrewQueryParserTest extends TestBase {
    @Test
    public void ParsesAcronymsCorrectly() throws ParseException {

        QueryParser qp = new HebrewQueryParser("f", new StandardAnalyzer());
        qp.parse("צה\"ל");
        qp.parse("\"צהל\"");
        qp.parse("כל הכבוד לצה\"ל");
        qp.parse("\"כל הכבוד לצה\"ל\"");
        qp.parse("\"כל הכבוד\" לצה\"ל");

        qp.parse("מנכ\"לית");

        try {
            qp.parse("צה\"\"ל");
            qp.parse("\"צה\"ל");
            fail("Expected exception was not thrown");
        } catch (ParseException ex) {
        }
    }
}
