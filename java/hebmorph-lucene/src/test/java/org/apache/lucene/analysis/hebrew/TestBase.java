package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.FileUtils;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class TestBase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        String hspellPath = null;
        if (dict == null) {
            if (allowHeHasheela) {
                dict = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
            }
                else {
                dict = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_NOH);
            }
        }
        return dict;
    }

    protected static File[] getTestFiles() throws IOException {
        List<String> lookedAt = new ArrayList<>();
        for (String s : new String[] { ".", "..", "../.." }){
            File f = new File(s + "/test-files");
            if (f.exists()) return f.listFiles();
            lookedAt.add(f.getCanonicalPath());
        }
        throw new IOException("Cannot find test data, looked at " + lookedAt);
    }

    @AfterClass
    public static void cleanupDictionary() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }
}
