package com.code972.hebmorph.hspell;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by egozy on 10/13/14.
 */
public class LoadUtil {
    private final static String DELIMETER = "#",
            PREFIX_H="PREFIX_H.txt",
            PREFIX_NOH="PREFIX_NOH.txt",
            DICT_FILE="DICT_FILE.gz";

    public static String getHspellPath() throws IOException {
        String hspellPath = null;
            ClassLoader classLoader = LoadUtil.class.getClassLoader();
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
        if (!hspellPath.endsWith("/")){
            hspellPath+= "/";
        }
        return hspellPath;
    }


    public static HashMap<String,Integer> readPrefixesFromFile(boolean allowHeHasheela) {
        HashMap<String,Integer> map = new HashMap<>();
        try{
            InputStreamReader reader;
            if (allowHeHasheela){
                reader = new InputStreamReader(new FileInputStream(getHspellPath() + PREFIX_H));
            }else{
                reader = new InputStreamReader(new FileInputStream(getHspellPath() + PREFIX_NOH));
            }
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str;
            while ((str = bufferedReader.readLine()) != null){
                String[] split = str.split(DELIMETER);
                if (split.length!=2){
                    //TODO: Error
                }else{
                    map.put(split[0],Integer.parseInt(split[1]));
                }
            }

            reader.close();

        }
        catch(IOException e){
        }
        return map;
    }


    public static DictRadix<MorphData> loadDicFromGzip() throws IOException {
        DictRadix<MorphData> dict = new DictRadix<>();
        GZIPInputStream reader = null;
        BufferedReader bufferedReader = null;
        int j=0;
        try {
            reader = new GZIPInputStream(new FileInputStream(LoadUtil.getHspellPath() + DICT_FILE));
            bufferedReader = new BufferedReader(new InputStreamReader(reader, Charset.forName("UTF-8")));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                j++;
                String[] split = str.split(DELIMETER); // 0=value,1=prefix,2=lemmas,3=descFlags
                if (split.length != 4) {
                    System.out.println("ERROR");
                    //TODO: error
                }
                MorphData md = new MorphData();
                md.setPrefixes(Short.parseShort(split[1]));
                String[] lemmas = split[2].split(",");
                for (int i=0;i<lemmas.length;i++){
                    if (lemmas[i].equals("null")){
                        lemmas[i] = null;
                    }
                }
                md.setLemmas(lemmas);
                String[] descStrings = split[3].split(",");
                Integer[] descInts = new Integer[descStrings.length];
                for (int i = 0; i < descStrings.length; i++) {
                    descInts[i] = Integer.parseInt(descStrings[i]);
                }
                Arrays.toString(descInts);
                md.setDescFlags(descInts);
                dict.addNode(split[0], md);
            }
        }catch (IOException e){
            System.out.println("ERROR : " + e);
            //TODO error
        }
        finally{
            if (bufferedReader != null) try { bufferedReader.close(); } catch (IOException ignored) {}
            if (reader != null) try { reader.close(); } catch (IOException ignored) {}
        }
        return dict;
    }







    //
    //
    //
    //##########################
    //used for making the files
    public static boolean writePrefixesToFile(HashMap<String, Integer> prefixTree, String fileName){
        try{
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName));
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (Map.Entry<String,Integer> pair:prefixTree.entrySet()){
                bufferedWriter.write(pair.getKey() + DELIMETER + pair.getValue() + "\n");
            }
            writer.close();
        }catch(IOException e){
            System.out.println("ERROR: could not write prefixes to file");
        }
        return true;
    }

    public static HashMap<String,Integer> readPrefixesFromFile(String fileName){
        HashMap<String,Integer> map = new HashMap<>();
        try{
            InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str;
            while ((str = bufferedReader.readLine()) != null){
                String[] split = str.split(DELIMETER);
                if (split.length!=2){
                    //TODO: Error
                }else{
                    map.put(split[0],Integer.parseInt(split[1]));
                }
            }

            reader.close();

        }
        catch(IOException e){
        }
        return map;
    }
}