package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;

import java.io.File;
import java.io.IOException;

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
}
