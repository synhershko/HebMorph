/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class DictionaryLoader {

    public static final int FILE_FORMAT_VERSION = 1;

    public final static String DELIMETER = "#",
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
                String[] lemmaStrings = split[2].split(",");
                String[] descStrings = split[3].split(",");
                if (lemmaStrings.length != descStrings.length) {
                    throw new IOException("Number of lemmas does not match number of descFlags");
                }
                MorphData.Lemma[] lemmas = new MorphData.Lemma[lemmaStrings.length];

                for (int i = 0; i < lemmas.length; i++) { //null and "null" are read the same
                    String lem = lemmaStrings[i].equals("null") ? null : lemmaStrings[i];
                    lemmas[i] = new MorphData.Lemma(lem, Integer.parseInt(descStrings[i]));
                }
                md.setLemmas(lemmas);
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