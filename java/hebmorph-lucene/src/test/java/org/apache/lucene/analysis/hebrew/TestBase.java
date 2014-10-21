package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.hspell.HebLoader;
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
                dict = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_H);
            }
                else {
                dict = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_NOH);
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
