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

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

public abstract class TestBase {
    private static DictHebMorph dict;

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

    protected static String readFileToString(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
