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
    public void compareLoaderWithLoad() throws IOException {
        String[] strings = {HebLoader.DICT_H, HebLoader.DICT_NOH};
        boolean[] booleans = {true, false};
        for (int i = 0; i < 2; i++) {
            Loader loader = new Loader(new File(HebLoader.getHspellPath()), true);
            DictRadix<MorphData> rad1 = loader.loadDictionaryFromHSpellData();
            HashMap<String, Integer> prefixes1 = HebLoader.readPrefixesFromFile(booleans[i]);
            DictHebMorph dict1 = new DictHebMorph(rad1, prefixes1);
            DictHebMorph dict2 = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + strings[i]);
            assert (dict1.equals(dict2));
        }
    }

    @Test
    public void testWriteEqualsRead() throws IOException {
        DictHebMorph dict1 = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_H);
        HebLoader.saveDicAndPrefixesToGzip(dict1, HebLoader.getHspellPath() + "temp_dict.gz");
        DictHebMorph dict2 = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + "temp_dict.gz");
        assert (dict1.equals(dict2));
        File file = new File(HebLoader.getHspellPath() + "temp_dict.gz");
        assert (file.delete());
    }

    @Test
    public void timeTest() throws IOException {
        long startTime, endTime;
        double duration1,duration2;
        startTime = System.nanoTime();
        Loader loader = new Loader(new File(HebLoader.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefixes = HebLoader.readPrefixesFromFile(true);
        endTime = System.nanoTime();
        duration1 = (double) (endTime - startTime) / (1000000000);
        startTime = System.nanoTime();
        DictHebMorph dictLoad = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_H);
        endTime = System.nanoTime();
        duration2 = (double) (endTime - startTime) / (1000000000);
        System.out.println("old: " + duration1 + " seconds ---> new: " + duration2 + " seconds");
    }
}
