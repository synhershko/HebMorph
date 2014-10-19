package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.FileUtils;
import com.code972.hebmorph.hspell.Loader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Egozy on 19/10/2014.
 */
public class FileUtilsTester {

    @Test
    public void compareOldLoaderWithNewLoad() throws IOException {
        Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        DictRadix<MorphData> dictLoad = FileUtils.loadDicFromGzip();
        DictRadix.RadixEnumerator en1 = (DictRadix.RadixEnumerator)dictLoader.iterator();
        DictRadix.RadixEnumerator en2 = (DictRadix.RadixEnumerator)dictLoad.iterator();
        assert(dictLoader.getCount()==dictLoad.getCount());
        while(en1.hasNext() && en2.hasNext()){
            assert(en1.getCurrentKey().equals(en2.getCurrentKey()));
            assert(en1.next().equals(en2.next()));
        }
        if ((en1.hasNext() && !en2.hasNext()) || (!en1.hasNext() && en2.hasNext())){
            assert(false);
        }
    }

    @Test
    public void timeTest() throws IOException {
        long startTime,endTime;
        startTime = System.nanoTime();
        Loader loader = new Loader(new File(FileUtils.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        endTime = System.nanoTime();
        double duration1 = (double)(endTime - startTime) / (1000000000);
        startTime = System.nanoTime();
        DictRadix<MorphData> dictLoad = FileUtils.loadDicFromGzip();
        endTime = System.nanoTime();
        double duration2 = (double)(endTime - startTime) / (1000000000);
        System.out.println("old: " + duration1 + " seconds ---> new: " + duration2 + " seconds");
    }
}
