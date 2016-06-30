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
package com.code972.hebmorph.hspell;

import com.code972.hebmorph.DescFlag;
import com.code972.hebmorph.DictionaryLoader;
import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.PrefixType;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class HSpellLoader {

    public final static String DELIMETER = "#",
            PREFIXES_INDICATOR = "#PREFIXES",
            DICTIONARY_INDICATOR = "#DICTIONARY";
    
    public static final String dictionaryFile = "hebrew.wgz";
    public static final String prefixesFile = dictionaryFile + ".prefixes";
    public static final String stemsFile = dictionaryFile + ".stems";
    public static final String descFile = dictionaryFile + ".desc";
    public static final String sizesFile = dictionaryFile + ".sizes";
    public static final String dmaskFile = "dmask.c";

    public final static String PREFIX_H = "prefix_h.gz", PREFIX_NOH = "prefix_noH.gz";

    protected List<Integer> dmasks;
    protected final boolean loadMorphData;
    private int lookupLen;

    protected InputStream fdict, fprefixes;
    protected InputStream fdesc = null, fstem = null;

    public HSpellLoader(File hspellFolder, boolean loadMorphData) throws IOException {
        this(new FileInputStream(new File(hspellFolder, sizesFile)), new FileInputStream(new File(hspellFolder, dmaskFile)),
                new FileInputStream(new File(hspellFolder, dictionaryFile)), new FileInputStream(new File(hspellFolder, prefixesFile)),
                new FileInputStream(new File(hspellFolder, descFile)), new FileInputStream(new File(hspellFolder, stemsFile)), loadMorphData);

        if (!hspellFolder.exists() || !hspellFolder.isDirectory())
            throw new IllegalArgumentException("Invalid hspell data folder provided");
    }

    /**
     * @param classloader
     * @param hspellFolder  resources folder in which the hspell data is in; must end with /
     * @param loadMorphData
     * @throws java.io.IOException
     */
    public HSpellLoader(final ClassLoader classloader, final String hspellFolder, final boolean loadMorphData) throws IOException {
        this(classloader.getResourceAsStream(hspellFolder + sizesFile), classloader.getResourceAsStream(hspellFolder + dmaskFile),
                classloader.getResourceAsStream(hspellFolder + dictionaryFile), classloader.getResourceAsStream(hspellFolder + prefixesFile),
                classloader.getResourceAsStream(hspellFolder + descFile), classloader.getResourceAsStream(hspellFolder + stemsFile), loadMorphData);
    }

    public HSpellLoader(InputStream sizesFile, InputStream dmasksFile, InputStream dictFile, InputStream prefixesFile, InputStream descFile, InputStream stemsFile, boolean loadMorphData) throws IOException {
        fdict = new GZIPInputStream(dictFile);
        fprefixes = new GZIPInputStream(prefixesFile);
        this.loadMorphData = loadMorphData;
        if (loadMorphData) {
            dmasks = new ArrayList<>();
            boolean foundStartLine = false;
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(dmasksFile));
            while ((line = reader.readLine()) != null) {
                if (!foundStartLine) {
                    if (line.contains("dmasks[]")) {
                        foundStartLine = true;
                    }
                    continue;
                }
                int i = tryParseInt(line);
                if (i >= 0) {
                    dmasks.add(i);
                }
            }
            reader.close();
            lookupLen = getWordCountInHSpellFolder(sizesFile);
            fdesc = new GZIPInputStream(descFile);
            fstem = new GZIPInputStream(stemsFile);
        }
    }

    public static String getHspellPath() {
        String hspellPath = null;
        ClassLoader classLoader = HSpellLoader.class.getClassLoader();
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

    public static HashMap<String, Integer> readDefaultPrefixes() {
        return readPrefixesFromFile(HSpellLoader.getHspellPath() + HSpellLoader.PREFIX_NOH);
    }

    //used when loading using the Loader and thus prefixes aren't loaded automatically
    public static HashMap<String, Integer> readPrefixesFromFile(String prefixPath) {
        HashMap<String, Integer> map = new HashMap<>();
        GZIPInputStream reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new GZIPInputStream(new FileInputStream(prefixPath));
            bufferedReader = new BufferedReader(new InputStreamReader(reader, DictionaryLoader.ENCODING_USED));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                String[] split = str.split(DELIMETER);
                if (split.length != 2) {
                    throw new IOException("Wrong format detected\n");
                } else {
                    map.put(split[0], Integer.parseInt(split[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
            return null;
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

    public DictHebMorph loadDictionaryFromHSpellData(String prefixPath) throws IOException {
        DictHebMorph dict = new DictHebMorph();
        dict.setPref(readPrefixesFromFile(prefixPath));
        if (loadMorphData) {
            // Load the count of morphological data slots required
            final String lookup[] = new String[lookupLen + 1];
            try {
                final char[] sbuf = new char[DictionaryLoader.MaxWordLength];
                int c = 0, n, slen = 0, i = 0;
                while ((c = fdict.read()) > -1) {
                    if ((c >= '0') && (c <= '9')) { // No conversion required for chars < 0xBE
                        // new word - finalize and save old word
                        lookup[i++] = new String(sbuf, 0, slen);

                        // and read how much to go back
                        n = 0;
                        do {
                            // base 10...
                            n *= 10;
                            n += (c - '0');
                        } while (((c = fdict.read()) > -1) && (c >= '0') && (c <= '9'));
                        slen -= n;
                    }
                    sbuf[slen++] = ISO8859_To_Unicode(c);
                }
            } finally {
                if (fdict != null) try {
                    fdict.close();
                } catch (IOException ignored) {
                }
            }
            try {
                for (int i = 0; lookup[i] != null; i++) {
                    MorphData data = new MorphData();
                    data.setPrefixes((short) fprefixes.read()); // Read prefix hint byte
                    Integer[] descFlags = readDescFile(fdesc);
                    final List<Integer> stemReferences = readStemFile(fstem);
                    final MorphData.Lemma[] lemmas = new MorphData.Lemma[stemReferences.size()];
                    int stemPosition = 0;
                    for (int r : stemReferences) {
                        String lemma;
                        // This is a bypass for the psuedo-stem "שונות", as defined by hspell
                        // TODO: Try looking into changing this in hspell itself
                        if (lookup[r].equals("שונות") && !lookup[r].equals(lookup[i])) {
                            lemma = null;
                        } else {
                            lemma = lookup[r];
                        }
                        lemmas[stemPosition] = new MorphData.Lemma(lemma, DescFlag.create((byte) (descFlags[stemPosition] & 3)), dmaskToPrefix(descFlags[stemPosition]));
                        stemPosition++;
                    }
                    data.setLemmas(lemmas);
                    dict.addNode(lookup[i], data);
                }
            } finally {
                if (fprefixes != null) try {
                    fprefixes.close();
                } catch (IOException ignored) {
                }
                if (fdesc != null) try {
                    fdesc.close();
                } catch (IOException ignored) {
                }
                if (fstem != null) try {
                    fstem.close();
                } catch (IOException ignored) {
                }
            }
        } else { // Use optimized version for loading HSpell's dictionary files
            try {
                final char[] sbuf = new char[DictionaryLoader.MaxWordLength];
                int c = 0, n, slen = 0;
                while ((c = fdict.read()) > -1) {
                    if ((c >= '0') && (c <= '9')) { // No conversion required for chars < 0xBE
                        // new word - finalize old word first (set value)
                        sbuf[slen] = '\0';
                        // TODO: Avoid creating new MorphData object, and enhance DictRadix to store
                        // the prefixes mask in the node itself
                        MorphData data = new MorphData();
                        data.setPrefixes((short) fprefixes.read()); // Read prefix hint byte
                        dict.addNode(sbuf, data);
                        // and read how much to go back
                        n = 0;
                        do {
                            // base 10...
                            n *= 10;
                            n += (c - '0');
                        } while (((c = fdict.read()) > -1) && (c >= '0') && (c <= '9'));
                        slen -= n;
                    }
                    sbuf[slen++] = ISO8859_To_Unicode(c);
                }

            } finally {
                if (fprefixes != null) try {
                    fprefixes.close();
                } catch (IOException ignored) {
                }
                if (fdict != null) try {
                    fdict.close();
                } catch (IOException ignored) {
                }
            }
        }
        return dict;
    }

    public static int getWordCountInHSpellFolder(File path) throws IOException {
        return getWordCountInHSpellFolder(new FileInputStream(new File(path, sizesFile)));
    }

    public static int getWordCountInHSpellFolder(InputStream inputStream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        reader.readLine();
        final String sizes = reader.readLine();
        reader.close();

        int tmp;
        tmp = sizes.indexOf(' ', sizes.indexOf('\n'));
        tmp = Integer.parseInt(sizes.substring(tmp + 1).trim());
        return tmp - 1; // hspell stores the actual word count + 1
    }

    private int bufPos = 0;
    private final int[] buf = new int[5];

    private final ArrayList<Integer> wordMasks = new ArrayList<Integer>();

    final Integer[] readDescFile(InputStream fdesc) throws IOException {
        while ((buf[bufPos] = fdesc.read()) > -1) {
            // Break on EOL or EOF
            if ((buf[bufPos] == '\n') || (buf[bufPos] == 0)) {
                bufPos = 0;
                Integer[] ret = wordMasks.toArray(new Integer[wordMasks.size()]);
                wordMasks.clear();
                return ret;
            }
            bufPos++;
            if (bufPos % 2 == 0) {

                int i = buf[0] - 'A' + (buf[1] - 'A') * 26;
                wordMasks.add(dmasks.get(i));
                bufPos = 0;
                continue;
            }
        }
        return null;
    }

    // Note: What HSpell call "stems", which we define as lemmas
    private final ArrayList<Integer> wordStems = new ArrayList<Integer>();

    final List<Integer> readStemFile(InputStream fstem) throws IOException {
        wordStems.clear();
        while ((buf[bufPos] = fstem.read()) > -1) {
            // Break on EOL or EOF
            if ((buf[bufPos] == '\n') || (buf[bufPos] == 0)) {
                bufPos = 0;
                return wordStems;
            }
            bufPos++;
            if (bufPos % 3 == 0) {
                wordStems.add(buf[0] - 33 + (buf[1] - 33) * 94 + (buf[2] - 33) * 94 * 94);
                bufPos = 0;
                continue;
            }
        }
        return null;
    }

    // Mapping is based on
    // http://www.unicode.org/Public/MAPPINGS/ISO8859/8859-8.TXT
    // 0xDF, 0xFD, 0xFE aren't converted
    private static char ISO8859_To_Unicode(int c) {
        if ((c >= 0xE0) && (c <= 0xFA)) {
            return (char) (c + 0x4F0);
        } else if (c <= 0xBE) {
            return (char) c;
        }
        return ' ';
    }

    private final static int descFlags_noun = 69;
    private final static int descFlags_person_name = 262145;
    private final static int descFlags_place_name = 262153;
    private final static int descFlags_empty = 0;

    public static DictRadix<MorphData> loadCustomWords(final InputStream customWordsStream, final DictRadix<MorphData> dictRadix) throws IOException {
        if (customWordsStream == null)
            return null;

        final BufferedReader input = new BufferedReader(new InputStreamReader(customWordsStream, Charset.forName("UTF-8")));
        final Hashtable<String, String> secondPass = new Hashtable<>();
        final DictRadix<MorphData> custom = new DictRadix<>();
        String line;
        while ((line = input.readLine()) != null) {
            String[] cells = line.split(" ");
            if (cells.length < 2)
                continue;

            MorphData md = null;
            switch (cells[1]) {
                case "שםעצם":
                    md = new MorphData();
                    md.setPrefixes((short) 63);
                    md.setLemmas(new MorphData.Lemma[]{new MorphData.Lemma(cells[0], DescFlag.D_NOUN, dmaskToPrefix(descFlags_noun))});
                    break;
                case "שםחברה":
                case "שםפרטי":
                    md = new MorphData();
                    md.setPrefixes((short) 8);
                    md.setLemmas(new MorphData.Lemma[]{new MorphData.Lemma(cells[0], DescFlag.D_PROPER, dmaskToPrefix(descFlags_person_name))});
                    break;
                case "שםמקום":
                    md = new MorphData();
                    md.setPrefixes((short) 8);
                    md.setLemmas(new MorphData.Lemma[]{new MorphData.Lemma(cells[0], DescFlag.D_PROPER, dmaskToPrefix(descFlags_place_name))});
                    break;
                case "שםמדויק":
                    md = new MorphData();
                    md.setPrefixes((short) 0);
                    md.setLemmas(new MorphData.Lemma[]{new MorphData.Lemma(cells[0], DescFlag.D_PROPER, dmaskToPrefix(descFlags_empty))});
                    break;
            }

            if (md == null) { // allow to associate new entries with other custom entries
                try {
                    md = custom.lookup(cells[1], false);
                } catch (IllegalArgumentException ignored_ex) {
                }
            }

            if (md == null) {
                try {
                    md = dictRadix.lookup(cells[1], false);
                } catch (IllegalArgumentException ignored_ex) {
                }
            }

            if (md != null) {
                custom.addNode(cells[0], md);
            } else {
                secondPass.put(cells[0], cells[1]);
            }
        }

        for (final Map.Entry<String, String> entry : secondPass.entrySet()) {
            try {
                custom.lookup(entry.getKey(), false);
                continue; // we already stored this word somehow
            } catch (IllegalArgumentException expected_ex) {
            }

            try {
                final MorphData md = custom.lookup(entry.getValue(), false);
                if (md != null) custom.addNode(entry.getKey(), md);
            } catch (IllegalArgumentException ignored_ex) {
            }
        }

        return custom;
    }

    //Retrieves the integer value of string (which may end with ','). Returns -1 if cannot convert.
    public static int tryParseInt(String str) {
        if (str == null) {
            return -1;
        }
        int length = str.length();
        length = str.endsWith(",") ? length - 1 : length;
        if (length == 0) {
            return -1;
        }
        int num = 0;
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return -1;
            }
            int digit = (int) c - (int) '0';
            num *= 10;
            num += digit;
        }
        return num;
    }

    // find the prefixes required by a word according to its details
    private static PrefixType dmaskToPrefix(Integer dmask) {
        PrefixType specifier;
        if ((dmask & DMask.D_TYPEMASK) == DMask.D_VERB) {
            if ((dmask & DMask.D_TENSEMASK) == DMask.D_IMPERATIVE) {
                specifier = PrefixType.PS_IMPER;
            } else if ((dmask & DMask.D_TENSEMASK) != DMask.D_PRESENT) {
                specifier = PrefixType.PS_VERB;
            } else if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0)) {
                specifier = PrefixType.PS_NONDEF;
            } else {
                specifier = PrefixType.PS_ALL;
            }
            /*TODO I feel that this may lead to a bug with ליפול and other infinitives that
             * did not loose their initial lamed.  I should correct this all the way from
             * woo.pl*/
            if ((dmask & DMask.D_TENSEMASK) == DMask.D_INFINITIVE) {
                specifier = PrefixType.PS_L;
            } else if ((dmask & DMask.D_TENSEMASK) == DMask.D_BINFINITIVE) {
                specifier = PrefixType.PS_B;
            }
        } else if (((dmask & DMask.D_TYPEMASK) == DMask.D_NOUN) || ((dmask & DMask.D_TYPEMASK) == DMask.D_ADJ)) {
            if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0) || ((dmask & DMask.D_SPECNOUN) > 0)) {
                specifier = PrefixType.PS_NONDEF;
            } else {
                specifier = PrefixType.PS_ALL;
            }
        } else {
            specifier = PrefixType.PS_ALL;
        }
        return specifier;
    }

    private static interface DMask {
        public static final int D_NOUN = 1;
        public static final int D_VERB = 2;
        public static final int D_ADJ = 3;
        public static final int D_TYPEMASK = 3;
        public static final int D_GENDERBASE = 4;
        public static final int D_MASCULINE = 4;
        public static final int D_FEMININE = 8;
        public static final int D_GENDERMASK = 12;
        public static final int D_GUFBASE = 16;
        public static final int D_FIRST = 16;
        public static final int D_SECOND = 32;
        public static final int D_THIRD = 48;
        public static final int D_GUFMASK = 48;
        public static final int D_NUMBASE = 64;
        public static final int D_SINGULAR = 64;
        public static final int D_DOUBLE = 128;
        public static final int D_PLURAL = 192;
        public static final int D_NUMMASK = 192;
        public static final int D_TENSEBASE = 256;
        public static final int D_INFINITIVE = 256;
        public static final int D_BINFINITIVE = 1536;
        public static final int D_PAST = 512;
        public static final int D_PRESENT = 768;
        public static final int D_FUTURE = 1024;
        public static final int D_IMPERATIVE = 1280;
        public static final int D_TENSEMASK = 1792;
        public static final int D_OGENDERBASE = 2048;
        public static final int D_OMASCULINE = 2048;
        public static final int D_OFEMININE = 4096;
        public static final int D_OGENDERMASK = 6144;
        public static final int D_OGUFBASE = 8192;
        public static final int D_OFIRST = 8192;
        public static final int D_OSECOND = 16384;
        public static final int D_OTHIRD = 24576;
        public static final int D_OGUFMASK = 24576;
        public static final int D_ONUMBASE = 32768;
        public static final int D_OSINGULAR = 32768;
        public static final int D_ODOUBLE = 65536;
        public static final int D_OPLURAL = 98304;
        public static final int D_ONUMMASK = 98304;
        public static final int D_OMASK = 129024;
        public static final int D_OSMICHUT = 131072;
        public static final int D_SPECNOUN = 262144;
        public static final int D_STARTBIT = 524288;
        public static final int D_ACRONYM = 1048576;
    }
}
