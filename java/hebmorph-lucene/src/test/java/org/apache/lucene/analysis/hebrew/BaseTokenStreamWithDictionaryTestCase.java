package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import com.code972.hebmorph.hspell.HebLoader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.junit.AfterClass;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by synhershko on 21/06/14.
 */
public abstract class BaseTokenStreamWithDictionaryTestCase extends BaseTokenStreamTestCase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        if (dict == null) {
            DictRadix<MorphData> radix = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
            HashMap<String, Integer> prefs = null;
            if (allowHeHasheela) {
                prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HebLoader.PREFIX_H);
            } else {
                prefs = HSpellLoader.readPrefixesFromFile(HSpellLoader.getHspellPath() + HebLoader.PREFIX_NOH);
            }
            dict = new DictHebMorph(radix, prefs);
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
