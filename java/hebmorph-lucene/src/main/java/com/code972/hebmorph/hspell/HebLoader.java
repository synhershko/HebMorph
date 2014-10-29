package com.code972.hebmorph.hspell;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by egozy on 10/13/14.
 */
public class HebLoader {

    public static final int FILE_FORMAT_VERSION = 1;

    public final static String DELIMETER = "#",
            PREFIX_H = "prefix_h.gz",
            PREFIX_NOH = "prefix_noH.gz",
            PREFIXES_INDICATOR = "#PREFIXES",
            DICTIONARY_INDICATOR = "#DICTIONARY";
    public static final Charset ENCODING_USED = Charset.forName("UTF-8");

    //loads a dictionary with it's corresponding prefixes. Returns the dictionary, prefixes are stored as static members here.
    public static DictHebMorph loadDicAndPrefixesFromGzip(String fileName) throws IOException {
        DictRadix<MorphData> dict = new DictRadix<>();
        HashMap<String, Integer> prefixes = new HashMap<>();
        GZIPInputStream reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new GZIPInputStream(new FileInputStream(fileName));
            bufferedReader = new BufferedReader(new InputStreamReader(reader, ENCODING_USED));
            String str;
            if (!(Integer.parseInt(bufferedReader.readLine()) == FILE_FORMAT_VERSION)) {
                throw new IOException("Old format detected");
            }
            if (!bufferedReader.readLine().equals(PREFIXES_INDICATOR)) {
                throw new IOException("Unknown format detected");
            }
            while (!(str = bufferedReader.readLine()).equals(DICTIONARY_INDICATOR)) {
                String[] split = str.split(DELIMETER);
                if (split.length != 2) {
                    throw new IOException("Wrong format detected");
                } else {
                    prefixes.put(split[0], Integer.parseInt(split[1]));
                }
            }
            if (!str.equals(DICTIONARY_INDICATOR)) {
                throw new IOException("Wrong format detected");
            }
            while ((str = bufferedReader.readLine()) != null) {
                String[] split = str.split(DELIMETER); // 0=value,1=prefix,2=lemmas,3=descFlags
                if (split.length != 4) {
                    throw new IOException("Wrong format detected");
                }
                MorphData md = new MorphData();
                md.setPrefixes(Short.parseShort(split[1]));
                String[] lemmas = split[2].split(",");
                for (int i = 0; i < lemmas.length; i++) { //null and "null" are read the same
                    if (lemmas[i].equals("null")) {
                        lemmas[i] = null;
                    }
                }
                md.setLemmas(lemmas);
                String[] descStrings = split[3].split(",");
                Integer[] descInts = new Integer[descStrings.length];
                for (int i = 0; i < descStrings.length; i++) {
                    descInts[i] = Integer.parseInt(descStrings[i]);
                }
                md.setDescFlags(descInts);
                dict.addNode(split[0], md);
            }
        } finally {
            if (bufferedReader != null) try {
                bufferedReader.close();
            } catch (IOException ignored) {
            }
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
        return new DictHebMorph(dict, prefixes);
    }
}