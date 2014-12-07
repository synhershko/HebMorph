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
package com.code972.hebmorph.datastructures;

import com.code972.hebmorph.MorphData;

import java.util.HashMap;

public class DictHebMorph {
    private HashMap<String, Integer> pref;
    private DictRadix<MorphData> dict;

    public DictHebMorph(DictRadix<MorphData> dict, HashMap<String, Integer> pref) {
        this.pref = pref;
        this.dict = dict;
    }

    //    getters.
    public DictRadix<MorphData> getRadix() {
        return dict;
    }

    public HashMap<String, Integer> getPref() {
        return pref;
    }

    public void clear() {
        dict.clear();
        pref.clear();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;
        DictHebMorph otherDict = (DictHebMorph) other;
        return (this.dict.equals(otherDict.dict) && this.pref.equals(otherDict.pref));
    }
}
