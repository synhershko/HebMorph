package com.code972.hebmorph;

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

    private boolean compareTwoDictionaries(DictRadix<MorphData> dict1, DictRadix<MorphData> dict2){
        if (dict1.getCount()!=dict2.getCount()){
            System.out.println("getCount is different: " + dict1.getCount() + "-" + dict2.getCount());
            return false;
        }
        DictRadix.RadixEnumerator en1 = (DictRadix.RadixEnumerator)dict1.iterator();
        DictRadix.RadixEnumerator en2 = (DictRadix.RadixEnumerator)dict2.iterator();
        while(en1.hasNext() && en2.hasNext()){
            if(!en1.getCurrentKey().equals(en2.getCurrentKey())){
                System.out.println("getCurrentKey is different : " + en1.getCurrentKey() + "-" + en2.getCurrentKey());
                return false;
            }
            if(!en1.next().equals(en2.next())){
                System.out.println("next is different");
                return false;
            }
        }
        if ((en1.hasNext() && !en2.hasNext()) || (!en1.hasNext() && en2.hasNext())){
            return false;
        }
        return true;
    }

    @Test
    public void comparePrefixes() throws IOException {
        //test with H
        Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
        DictRadix<MorphData> dict1 = loader.loadDictionaryFromHSpellData();
        HashMap<String,Integer> prefixes1 = FileUtils.getPrefixes(true);
        DictRadix<MorphData> dict2 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
        HashMap<String,Integer> prefixes2 = FileUtils.getPrefixes();
        assert(compareTwoDictionaries(dict1,dict2));
        assert(prefixes1!=prefixes2);
        assert(prefixes1.equals(prefixes2));
        //test just prefixes without H
        prefixes1 = FileUtils.getPrefixes(false);
        FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_NOH);
        prefixes2 = FileUtils.getPrefixes();
        assert(prefixes1!=prefixes2);
        assert(prefixes1.equals(prefixes2));
    }

    @Test
    public void testWriteEqualsRead() throws IOException {
        DictRadix<MorphData> dict1 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
        HashMap<String,Integer> pref1 = FileUtils.getPrefixes();
        FileUtils.saveDicAndPrefixesToGzip(dict1, pref1, FileUtils.getHspellPath() + "temp_dict.gz");
        DictRadix<MorphData> dict2 = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + "temp_dict.gz");
        HashMap<String,Integer> pref2 = FileUtils.getPrefixes();
        assert (compareTwoDictionaries(dict1, dict2));
        assert (pref1.equals(pref2));
        File file = new File(FileUtils.getHspellPath() + "temp_dict.gz");
        assert (file.delete());
    }

//    @Test
    public void timeTest() throws IOException {
        long startTime,endTime;
        startTime = System.nanoTime();
        Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        HashMap<String,Integer> prefixes = FileUtils.getPrefixes(true);
        endTime = System.nanoTime();
        double duration1 = (double)(endTime - startTime) / (1000000000);
        startTime = System.nanoTime();
        DictRadix<MorphData> dictLoad = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
        endTime = System.nanoTime();
        double duration2 = (double)(endTime - startTime) / (1000000000);
        System.out.println("old: " + duration1 + " seconds ---> new: " + duration2 + " seconds");
    }
}
