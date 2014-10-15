package com.code972.hebmorph;

/**
 * Created by egozy on 10/13/14.
 */

import com.code972.hebmorph.hspell.LoadUtil;

import java.io.IOException;
import java.util.HashMap;

public class UtilTester {

    private final String FILE_NAME = "PREFIX_NOH.txt";

//    @Test
    public void testBasicWriting() throws IOException {
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(false);
        LoadUtil.writePrefixesToFile(map, LoadUtil.getHspellPath() + FILE_NAME);
    }

//    @Test
    public void testBasicReading() throws IOException {
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(LoadUtil.getHspellPath() + FILE_NAME);
        System.out.println(map.toString());
    }

    //    @Test
    public void testReadingFromResource(){
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(false);
        System.out.println(map);
    }
}