package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.FileUtils;
import com.code972.hebmorph.hspell.Loader;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by Egozy on 19/10/2014.
 */
public class FileUtilsTester {

    @Test
    public void compareLoaderWithLoad() throws IOException {
        String[] strings = {FileUtils.DICT_H, FileUtils.DICT_NOH};
        boolean[] booleans = {true, false};
        for (int i = 0; i < 2; i++) {
            Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
            DictRadix<MorphData> rad1 = loader.loadDictionaryFromHSpellData();
            HashMap<String, Integer> prefixes1 = FileUtils.readPrefixesFromFile(booleans[i]);
            DictHebMorph dict1 = new DictHebMorph(rad1, prefixes1);
            DictHebMorph dict2 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + strings[i]);
            assert (dict1.equals(dict2));
        }
    }

    @Test
    public void testWriteEqualsRead() throws IOException {
        DictHebMorph dict1 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
        FileUtils.saveDicAndPrefixesToGzip(dict1, FileUtils.getHspellPath() + "temp_dict.gz");
        DictHebMorph dict2 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + "temp_dict.gz");
        assert (dict1.equals(dict2));
        File file = new File(FileUtils.getHspellPath() + "temp_dict.gz");
        assert (file.delete());
    }

    //    @Test
    public void timeTest() throws IOException {
        long startTime, endTime;
        startTime = System.nanoTime();
        Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        HashMap<String, Integer> prefixes = FileUtils.readPrefixesFromFile(true);
        endTime = System.nanoTime();
        double duration1 = (double) (endTime - startTime) / (1000000000);
        startTime = System.nanoTime();
        DictHebMorph dictLoad = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
        endTime = System.nanoTime();
        double duration2 = (double) (endTime - startTime) / (1000000000);
        System.out.println("old: " + duration1 + " seconds ---> new: " + duration2 + " seconds");
    }
}
