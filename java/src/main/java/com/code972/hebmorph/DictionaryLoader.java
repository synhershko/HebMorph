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
import com.code972.hebmorph.hspell.HSpellLoader;

import java.io.*;
import java.nio.charset.Charset;

public class DictionaryLoader {

    public static DictHebMorph lookForDefaultDictionary() throws IOException {
        HSpellLoader loader = new HSpellLoader(new File(HSpellLoader.getHspellPath()),true);
        return loader.loadDictionaryFromHSpellData(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_H);
    }
}
