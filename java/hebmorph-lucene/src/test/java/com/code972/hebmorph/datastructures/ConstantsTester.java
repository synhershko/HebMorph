package com.code972.hebmorph;

/**
 * Created by egozy on 10/13/14.
 */

import com.code972.hebmorph.hspell.ConstantsHandler;
import com.code972.hebmorph.hspell.LingInfo;
import org.junit.*;

import java.io.IOException;
import java.util.HashMap;

public class ConstantsTester {

    private final String FILE_NAME = "PREFIX_NOH.txt";

    @Test
//    public void testBasicWriting() throws IOException {
        HashMap<String,Integer> map = ConstantsHandler.readPrefixesFromFile(false);
        ConstantsHandler.writePrefixesToFile(map,ConstantsHandler.getHspellPath() + FILE_NAME);
    }

//    @Test
    public void testBasicReading() throws IOException {
        HashMap<String,Integer> map = ConstantsHandler.readPrefixesFromFile(ConstantsHandler.getHspellPath() + FILE_NAME);
        System.out.println(map.toString());
    }

    //    @Test
    public void testReadingFromResource(){
        HashMap<String,Integer> map = ConstantsHandler.readPrefixesFromFile(false);
        System.out.println(map);
    }
}