package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.hspell.HebLoader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

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
                dict = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_H);
            }
            else {
                dict = HebLoader.loadDicAndPrefixesFromGzip(HebLoader.getHspellPath() + HebLoader.DICT_NOH);
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
