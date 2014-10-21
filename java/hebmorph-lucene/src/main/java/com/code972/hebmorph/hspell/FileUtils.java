package com.code972.hebmorph.hspell;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by egozy on 10/13/14.
 */
public class FileUtils {

    public final static String DELIMETER = "#",
            PREFIX_H = "prefix_h.gz",
            PREFIX_NOH = "prefix_noH.gz",
            DICT_H = "dict_h.gz",
            DICT_NOH = "dict_noH.gz",
            PREFIXES_INDICATOR = "#PREFIXES",
            DICTIONARY_INDICATOR = "#DICTIONARY";
    public static final Charset ENCODING_USED = Charset.forName("UTF-8");

    public static String getHspellPath() throws IOException {
        String hspellPath = null;
        ClassLoader classLoader = FileUtils.class.getClassLoader();
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
        if (hspellPath == null) {
            throw new IllegalArgumentException("path to hspell data folder couldn't be found");
        }
        if (!hspellPath.endsWith("/")) {
            hspellPath += "/";
        }
        return hspellPath;
    }

    //used when loading using the Loader and thus prefixes aren't loaded automatically
    public static HashMap<String, Integer> readPrefixesFromFile(boolean allowHeHasheela) {
        HashMap<String, Integer> map = new HashMap<>();
        GZIPInputStream reader = null;
        BufferedReader bufferedReader = null;
        try {
            if (allowHeHasheela) {
                reader = new GZIPInputStream(new FileInputStream(FileUtils.getHspellPath() + PREFIX_H));
            } else {
                reader = new GZIPInputStream(new FileInputStream(FileUtils.getHspellPath() + PREFIX_NOH));
            }
            bufferedReader = new BufferedReader(new InputStreamReader(reader, ENCODING_USED));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                String[] split = str.split(DELIMETER);
                if (split.length != 2) {
                    //TODO: Error
                } else {
                    map.put(split[0], Integer.parseInt(split[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
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
        return map;
    }

    //saves a complete dictionary and the corresponding prefixes to fileName.
    public static void saveDicAndPrefixesToGzip(DictHebMorph dict, String fileName) throws IOException {
        GZIPOutputStream writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            writer = new GZIPOutputStream(new FileOutputStream(fileName));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer, ENCODING_USED));
            //write the prefixes
            bufferedWriter.write(PREFIXES_INDICATOR + "\n");
            for (Map.Entry<String, Integer> pair : dict.getPref().entrySet()) {
                bufferedWriter.write(pair.getKey() + FileUtils.DELIMETER + pair.getValue() + "\n");
            }
            //write the dictionary
            bufferedWriter.write(DICTIONARY_INDICATOR + "\n");
            DictRadix.RadixEnumerator en = (DictRadix.RadixEnumerator) dict.getRadix().iterator();
            while (en.hasNext()) {
                String mdName = en.getCurrentKey();
                MorphData md = (MorphData) en.next();
                String writtenString = new String();
                writtenString += (mdName + "#" + md.getPrefixes() + "#");
                for (String str : md.getLemmas()) {
                    writtenString += (str + ",");
                }
                writtenString += ("#");
                for (int d : md.getDescFlags()) {
                    writtenString += (d + ",");
                }
                writtenString += "\n";
                bufferedWriter.write(writtenString);
            }
        } finally {
            if (bufferedWriter != null) try {
                bufferedWriter.close();
            } catch (IOException ignored) {
            }
            if (writer != null) try {
                writer.close();
            } catch (IOException ignored) {
            }
        }
    }

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
            if (!bufferedReader.readLine().equals(PREFIXES_INDICATOR)) {
                //TODO:ERROR
            }
            while (!(str = bufferedReader.readLine()).equals(DICTIONARY_INDICATOR)) {
                String[] split = str.split(DELIMETER);
                if (split.length != 2) {
                    //TODO: Error
                } else {
                    prefixes.put(split[0], Integer.parseInt(split[1]));
                }
            }
            if (!str.equals(DICTIONARY_INDICATOR)) {
                //TODO:ERROR
            }
            while ((str = bufferedReader.readLine()) != null) {
                String[] split = str.split(DELIMETER); // 0=value,1=prefix,2=lemmas,3=descFlags
                if (split.length != 4) {
                    System.out.println("ERROR");
                    //TODO: error
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
        DictHebMorph ret = new DictHebMorph(dict, prefixes);
        return ret;
    }
}