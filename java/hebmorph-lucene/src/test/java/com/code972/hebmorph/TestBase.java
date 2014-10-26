package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HebLoader;
import com.code972.hebmorph.hspell.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;

public abstract class TestBase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        if (dict == null) {
            DictRadix<MorphData> radix = new Loader(new File(HebLoader.getHspellPath()), true).loadDictionaryFromHSpellData();
            HashMap<String, Integer> prefs = null;
            if (allowHeHasheela) {
                prefs = HebLoader.readPrefixesFromFile(HebLoader.getHspellPath() + HebLoader.PREFIX_H);
            } else {
                prefs = HebLoader.readPrefixesFromFile(HebLoader.getHspellPath() + HebLoader.PREFIX_NOH);
            }
            dict = new DictHebMorph(radix, prefs);
        }
        return dict;
    }

    protected static String readFileToString(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
