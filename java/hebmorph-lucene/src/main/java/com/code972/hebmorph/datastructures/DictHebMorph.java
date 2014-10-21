package com.code972.hebmorph.datastructures;

import com.code972.hebmorph.MorphData;

import java.util.HashMap;

/**
 * Created by Egozy on 20/10/2014.
 */
public class DictHebMorph {
    //consider whether or not make it generic. for now, let's roll without
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
