package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public abstract class TestBase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary(boolean allowHeHasheela) throws IOException {
        if (dict == null) {
//            String hspellPath = FileUtils.getHspellPath();
//            Loader loader = new Loader(new File(hspellPath), true);
//            dict = loader.loadDictionaryFromHSpellData();
            if (allowHeHasheela){
                dict = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_H);
            }
            else{
                dict = FileUtils.loadDicAndPrefixesFromGzip(FileUtils.getHspellPath() + FileUtils.DICT_NOH);
            }
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
