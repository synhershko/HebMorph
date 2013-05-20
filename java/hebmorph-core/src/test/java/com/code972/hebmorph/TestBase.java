package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public abstract class TestBase {
    private static DictRadix<MorphData> dict;

    protected synchronized DictRadix<MorphData> getDictionary() throws IOException {
        String hspellPath = null;
        if (dict == null) {
            ClassLoader classLoader = TestBase.class.getClassLoader();
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

            dict = Loader.loadDictionaryFromHSpellData(new File(hspellPath), true);
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
        }
        finally {
            stream.close();
        }
    }
}
