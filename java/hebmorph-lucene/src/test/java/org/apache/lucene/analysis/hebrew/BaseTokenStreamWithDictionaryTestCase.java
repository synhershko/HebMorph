package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.FileUtils;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;

/**
 * Created by synhershko on 21/06/14.
 */
public abstract class BaseTokenStreamWithDictionaryTestCase extends BaseTokenStreamTestCase {
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

    @AfterClass
    public static void cleanup() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }
}
