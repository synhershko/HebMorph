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

import com.code972.hebmorph.DictionaryLoader;
import com.code972.hebmorph.datastructures.DictHebMorph;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

import java.io.IOException;

public abstract class BaseTokenStreamWithDictionaryTestCase extends BaseTokenStreamTestCase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary() throws IOException {
        if (dict == null) {
            dict = DictionaryLoader.loadDictFromPath(com.code972.hebmorph.TestBase.DICT_PATH);
        }
        return dict;
    }

    @AfterClass
    public static void cleanup() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }
}
