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
package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RealWorldTests extends TestBase {

    private static DictRadix<Byte> specialTokenizationCases = new DictRadix<>(false);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        specialTokenizationCases.addNode("H&M", new Byte((byte) 0));
        specialTokenizationCases.addNode("C++", new Byte((byte) 0));
        specialTokenizationCases.addNode("i-phone", new Byte((byte) 0));
        specialTokenizationCases.addNode("i-pad", new Byte((byte) 0));
    }

    @Test
    public void test() throws IOException {
        File files = new File("./../../test-files");
        for (File filename : files.listFiles()) {
            testImpl(filename.getPath());
        }
    }

    private void testImpl(String filename) throws IOException {
        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        final String contents = readFileToString(filename);
        final StreamLemmatizer sl = new StreamLemmatizer(new StringReader(contents), getDictionary(false), specialTokenizationCases);
        while (sl.getLemmatizeNextToken(token, results) != 0) {
            System.out.println(token.ref);
        }
    }
}
