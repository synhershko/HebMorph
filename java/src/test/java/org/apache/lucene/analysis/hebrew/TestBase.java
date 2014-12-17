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

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TestBase {
    private static DictHebMorph dict;
    protected final Version LUCENE_VERSION = Version.LUCENE_4_9;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        if (dict == null) {
            DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
            HashMap<String, Integer> prefs = null;
            if (allowHeHasheela) {
                prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_H);
            } else {
                prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
            }
            dict = new DictHebMorph(radix, prefs);
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
        DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
        return new HebrewIndexingAnalyzer(new DictHebMorph(radix, prefs));
    }

    public static HebrewQueryAnalyzer getHebrewQueryAnalyzer() throws IOException {
        DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
        return new HebrewQueryAnalyzer(new DictHebMorph(radix, prefs));
    }

    public static HebrewQueryLightAnalyzer getHebrewQueryLightAnalyzer() throws IOException {
        DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
        return new HebrewQueryLightAnalyzer(new DictHebMorph(radix, prefs));
    }

    public static HebrewExactAnalyzer getHebrewExactAnalyzer() throws IOException {
        DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
        return new HebrewExactAnalyzer(new DictHebMorph(radix, prefs));
    }
}
