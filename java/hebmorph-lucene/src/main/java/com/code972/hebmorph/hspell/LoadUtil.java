package com.code972.hebmorph.hspell;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by egozy on 10/13/14.
 */
public class LoadUtil {
    private final static String DELIMETER = "#",
            PREFIX_H="PREFIX_H.txt",
            PREFIX_NOH="PREFIX_NOH.txt";

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
                writer.write(pair.getKey() + DELIMETER + pair.getValue() + "\n");
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