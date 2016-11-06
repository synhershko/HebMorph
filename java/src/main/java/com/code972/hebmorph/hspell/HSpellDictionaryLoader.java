package com.code972.hebmorph.hspell;

import com.code972.hebmorph.DictionaryLoader;
import com.code972.hebmorph.datastructures.DictHebMorph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * DictionaryLoader implementation for loading hspell data files
 */
public class HSpellDictionaryLoader implements DictionaryLoader {
    @Override
    public String dictionaryLoaderName() {
        return "hspell";
    }

    @Override
    public DictHebMorph loadDictionaryFromPath(String path) throws IOException {
        if (!path.endsWith("/")) {
            path += "/";
        }

        final File file = new File(path);
        if (file.isDirectory()) {
            HSpellLoader loader = new HSpellLoader(new File(path), true);
            return loader.loadDictionaryFromHSpellData(new FileInputStream(new File(path, HSpellLoader.PREFIX_H)));
        } else {
            throw new IOException("Expected a folder. Cannot load dictionary from HSpell files.");
        }
    }

    @Override
    public DictHebMorph loadDictionaryFromDefaultPath() throws IOException {
        HSpellLoader loader = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true);
        return loader.loadDictionaryFromHSpellData(new FileInputStream(new File(HSpellLoader.getHspellPath(), HSpellLoader.PREFIX_NOH)));
    }
}
