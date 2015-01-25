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

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.HSpellLoader;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class TestBase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        if (dict == null) {
            HSpellLoader loader = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true);
            if (allowHeHasheela) {
                dict = loader.loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_H);
            } else {
                dict = loader.loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
            }
        }
        return dict;
    }

    protected static File[] getTestFiles() throws IOException {
        List<String> lookedAt = new ArrayList<>();
        for (String s : new String[]{".", "..", "../.."}) {
            File f = new File(s + "/test-files");
            if (f.exists()) return f.listFiles();
            lookedAt.add(f.getCanonicalPath());
        }
        throw new IOException("Cannot find test data, looked at " + lookedAt);
    }

    @AfterClass
    public static void cleanupDictionary() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }

    public static HebrewIndexingAnalyzer getHebrewIndexingAnalyzer() throws IOException {
        return new HebrewIndexingAnalyzer(new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH));
    }

    public static HebrewQueryAnalyzer getHebrewQueryAnalyzer() throws IOException {
        return new HebrewQueryAnalyzer(new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH));
    }

    public static HebrewQueryLightAnalyzer getHebrewQueryLightAnalyzer() throws IOException {
        return new HebrewQueryLightAnalyzer(new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH));
    }

    public static HebrewExactAnalyzer getHebrewExactAnalyzer() throws IOException {
        return new HebrewExactAnalyzer(new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH));
    }
}
