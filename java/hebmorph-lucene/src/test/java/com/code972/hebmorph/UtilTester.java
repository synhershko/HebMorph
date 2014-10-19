package com.code972.hebmorph;

/**
 * Created by egozy on 10/13/14.
 */

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Constants;
import com.code972.hebmorph.hspell.LoadUtil;
import com.code972.hebmorph.hspell.Loader;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class UtilTester {

    @Test
    public void compareLoaderWithLoad() throws IOException {
        long startTime,endTime;
        startTime = System.nanoTime();
        Loader loader = new Loader(new File(LoadUtil.getHspellPath()), true);
        DictRadix<MorphData> dictLoader = loader.loadDictionaryFromHSpellData();
        endTime = System.nanoTime();
        double duration1 = (double)(endTime - startTime) / (1000000000);
        startTime = System.nanoTime();
        DictRadix<MorphData> dictLoad = LoadUtil.loadDicFromGzip();
        endTime = System.nanoTime();
        double duration2 = (double)(endTime - startTime) / (1000000000);
        System.out.println("old: " + duration1 + " seconds ---> new: " + duration2 + " seconds");
        DictRadix.RadixEnumerator en1 = (DictRadix.RadixEnumerator)dictLoader.iterator();
        DictRadix.RadixEnumerator en2 = (DictRadix.RadixEnumerator)dictLoad.iterator();
        while(en1.hasNext() && en2.hasNext()){
            assert(en1.getCurrentKey().equals(en2.getCurrentKey()));
            assert(en1.next().equals(en2.next()));
        }
    }
//    @Test
    public void testBasicWriting() throws IOException {
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(false);
        LoadUtil.writePrefixesToFile(map, LoadUtil.getHspellPath() + "PREFIX_NOH.txt");
    }

//    @Test
    public void testBasicReading() throws IOException {
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(LoadUtil.getHspellPath() + "PREFIX_NOH.txt");
        System.out.println(map.toString());
    }

    //    @Test
    public void testReadingFromResource(){
        HashMap<String,Integer> map = LoadUtil.readPrefixesFromFile(false);
        System.out.println(map);
    }

    InputStream fdict;
    InputStream fprefixes;
    int lookupLen;
    InputStream fdesc;
    InputStream fstem;
    List<String> dmasks;
//    @Test
    public void playWithLoader() throws IOException {
            // Load the count of morphological data slots required
        fdict = new GZIPInputStream(new FileInputStream(LoadUtil.getHspellPath() + Constants.dictionaryFile));
        fprefixes = new GZIPInputStream(new FileInputStream(LoadUtil.getHspellPath() + Constants.prefixesFile));
        lookupLen = Loader.getWordCountInHSpellFolder(new FileInputStream(LoadUtil.getHspellPath() + Constants.sizesFile));
        fdesc = new GZIPInputStream(new FileInputStream(LoadUtil.getHspellPath() + Constants.descFile));
        fstem = new GZIPInputStream(new FileInputStream(LoadUtil.getHspellPath() + Constants.stemsFile));
        dmasks = new ArrayList<>();
        boolean foundStartLine = false;
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(LoadUtil.getHspellPath() + Constants.dmaskFile)));
        while ((line = reader.readLine()) != null) {
            if (!foundStartLine) {
                if (line.contains("dmasks[]")) {
                    foundStartLine = true;
                }
                continue;
            }
            dmasks.add(line);
        }
        reader.close();
            final String lookup[] = new String[lookupLen + 1];
            try {
                final char[] sbuf = new char[Constants.MaxWordLength];
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
                if (fdict != null) try { fdict.close(); } catch (IOException ignored) {}
            }
            GZIPOutputStream writer = null;
//            FileOutputStream writer = null;
            BufferedWriter bufferedWriter = null;
            final DictRadix<MorphData> ret = new DictRadix<MorphData>();
            try {
//                writer = (new FileOutputStream(LoadUtil.getHspellPath() + "DICT_FILE.txt"));
                writer = new GZIPOutputStream(new FileOutputStream(LoadUtil.getHspellPath() + "DICT_FILE.gz"));
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(writer, Charset.forName("UTF-8")));
                for (int i = 0; lookup[i] != null; i++) {
//                    System.out.println(i);
                    MorphData data = new MorphData();
                    data.setPrefixes((short) fprefixes.read()); // Read prefix hint byte
                    data.setDescFlags(readDescFile(fdesc));
                    final List<Integer> stemReferences = readStemFile(fstem);
                    final String[] lemmas = new String[stemReferences.size()];
                    int stemPosition = 0;
                    for (int r : stemReferences) {
                        // This is a bypass for the psuedo-stem "שונות", as defined by hspell
                        // TODO: Try looking into changing this in hspell itself
                        if (lookup[r].equals("שונות") && !lookup[r].equals(lookup[i])) {
                            lemmas[stemPosition++] = null;
                        } else {
                            lemmas[stemPosition++] = lookup[r];
                        }
                    }
                    data.setLemmas(lemmas);
                    ret.addNode(lookup[i], data);
                    String writtenString = new String();
                    writtenString+= (lookup[i] + "#" + data.getPrefixes() + "#");
                    for (String str:data.getLemmas()){
                        writtenString += (str + ",");
                    }

                    writtenString+=("#");
                    for (int d:data.getDescFlags()){
                        writtenString+=(d + ",");
                    }
                    writtenString+="\n";
//                    System.out.println(writtenString);
                    bufferedWriter.write(writtenString);
                }

            } finally {
                if (fprefixes != null) try { fprefixes.close(); } catch (IOException ignored) {}
                if (fdesc != null) try { fdesc.close(); } catch (IOException ignored) {}
                if (fstem != null) try { fstem.close(); } catch (IOException ignored) {}
                if (fstem != null) try { fstem.close(); } catch (IOException ignored) {}
                if (bufferedWriter != null) try { bufferedWriter.close(); } catch (IOException ignored) {}
                if (writer != null) try { writer.close(); } catch (IOException ignored) {}
            }
        System.out.println(ret.getCount());
    }
    private int bufPos = 0;
    private final int[] buf = new int[5];

    private static char ISO8859_To_Unicode(int c) {
        if ((c >= 0xE0) && (c <= 0xFA))
        {
            return (char)(c + 0x4F0);
        }
        else if (c <= 0xBE)
        {
            return (char)c;
        }
        return ' ';
    }

    private final ArrayList<Integer> wordMasks = new ArrayList<Integer>();
    final Integer[] readDescFile(InputStream fdesc) throws IOException {
        while ((buf[bufPos] = fdesc.read()) > -1) {
            // Break on EOL or EOF
            if ((buf[bufPos] == '\n') || (buf[bufPos] == 0))
            {
                bufPos = 0;
                Integer[] ret = wordMasks.toArray(new Integer[wordMasks.size()]);
                wordMasks.clear();
                return ret;
            }
            bufPos++;
            if (bufPos % 2 == 0) {
                int i = buf[0] - 'A' + (buf[1] - 'A') * 26;
                wordMasks.add(Integer.valueOf(dmasks.get(i).substring(0, dmasks.get(i).length() - 1)));
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
}