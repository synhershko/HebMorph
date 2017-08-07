package com.code972.hebmorph.hspell;

import com.code972.hebmorph.DictionaryLoader;
import com.code972.hebmorph.datastructures.DictHebMorph;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * DictionaryLoader implementation for loading hspell data files
 */
public class HSpellDictionaryLoader implements DictionaryLoader {
    @Override
    public String dictionaryLoaderName() {
        return "hspell";
    }

    @Override
    @Deprecated
    public String[] dictionaryPossiblePaths() {
        return getPossiblePaths();
    }

    @Override
    public String[] getPossiblePaths(final String ... basePaths) {
        final HashSet<String> paths = new HashSet<>();
        if (basePaths != null) {
            for (final String basePath : basePaths) {
                paths.add(Paths.get(basePath, "hspell-data-files").toAbsolutePath().toString());
            }
        }
        paths.add("/var/lib/hspell-data-files/");
        return paths.toArray(new String[paths.size()]);
    }

    @Override
    public DictHebMorph loadDictionary(final InputStream stream) throws IOException {
        throw new NotImplementedException();
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
