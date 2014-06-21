package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;

/**
 * Created by synhershko on 21/06/14.
 */
public abstract class BaseTokenStreamWithDictionaryTestCase extends BaseTokenStreamTestCase {
    private static DictRadix<MorphData> dict;

    protected synchronized DictRadix<MorphData> getDictionary() throws IOException {
        String hspellPath = null;
        if (dict == null) {
            ClassLoader classLoader = TermPositionVectorTest.class.getClassLoader();
            File folder = new File(classLoader.getResource("").getPath());
            while (true) {
                File tmp = new File(folder, "hspell-data-files");
                if (tmp.exists() && tmp.isDirectory()) {
                    hspellPath = tmp.toString();
                    break;
                }

                folder = folder.getParentFile();
                if (folder == null) break;
            }

            if (hspellPath == null)
                throw new IllegalArgumentException("path to hspell data folder couldn't be found");

            dict = new Loader(new File(hspellPath), true).loadDictionaryFromHSpellData();
        }
        return dict;
    }

    @AfterClass
    public static void cleanup() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }
}
