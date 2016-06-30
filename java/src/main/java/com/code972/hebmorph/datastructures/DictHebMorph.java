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
    private HashMap<String, MorphData> mds;

    public DictHebMorph(){
        dict = new DictRadix<>();
        pref = new HashMap<>();
        mds = new HashMap<>();
    }

    public void addNode(String s, MorphData md) {
        this.mds.put(s,md);
        this.dict.addNode(s,md);
    }

    public void addNode(char[] s, MorphData md) {
        addNode(new String(s),md);
    }

    public final DictRadix<MorphData> getRadix() {
        return dict;
    }

    public final HashMap<String, Integer> getPref() {
        return pref;
    }

    public void setPref(final HashMap<String, Integer> prefs) {
        this.pref = prefs;
    }

    public final MorphData lookup (final String key){
        return mds.get(key);
    }

    public void clear() {
        dict.clear();
        pref.clear();
        mds.clear();
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
        return (this.dict.equals(otherDict.dict) && this.pref.equals(otherDict.pref) && this.mds.equals(otherDict.mds));
    }
}
