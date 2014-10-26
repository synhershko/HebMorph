package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HebLoader;
import com.code972.hebmorph.hspell.Loader;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;

/**
 * Created by Egozy on 19/10/2014.
 */
public class HebLoaderTester {

    @Test
    public void testWriteEqualsRead() throws IOException {
        DictHebMorph dict1 = basicDictionary();
        HebLoader.saveDicAndPrefixesToGzip(dict1, HebLoader.getHspellPath() + "temp_dict.gz");
        DictHebMorph dict2 = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + "temp_dict.gz");
        assert (dict1.equals(dict2));
        File file = new File(HebLoader.getHspellPath() + "temp_dict.gz");
        assert (file.delete());
    }

    private DictHebMorph basicDictionary(){
        DictHebMorph dict;
        HashMap<String,Integer> prefs = new HashMap<>();
        prefs.put("ב",43);
        prefs.put("בכ",42);
        prefs.put("ה",32);
        prefs.put("ו",60);
        DictRadix<MorphData> radix = new DictRadix<>();
        MorphData md = new MorphData();
        md.setLemmas(new String[]{"אנציקלופדיה"});
        md.setPrefixes((short)63);
        md.setDescFlags(new Integer[]{77});
        radix.addNode("אנציקלופדיה",md);
        md = new MorphData();
        md.setLemmas(new String[]{"לימוד","לימוד"});
        md.setPrefixes((short)63);
        md.setDescFlags(new Integer[]{0,201});
        radix.addNode("לימוד",md);
        dict = new DictHebMorph(radix,prefs);
        return dict;
    }
}
