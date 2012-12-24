/***************************************************************************
 *   Copyright (C) 2010-2013 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com> (initial Java port)           *
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

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictRadix;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class Loader {

    public static DictRadix<MorphData> loadDictionaryFromClasspath(String pathInClasspath, boolean bLoadMorphData) throws IOException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource(pathInClasspath);
        if(url == null)
            throw new FileNotFoundException("Cannot find '" + pathInClasspath + "' in classpath.");
        return loadDictionaryFromHSpellData(new File(url.toString()), bLoadMorphData);
    }

    public static int getWordCountInHSpellFolder(File path) throws IOException {
        int tmp;
        String sizes = Files.readAllLines(new File(path, Constants.sizesFile).toPath(), Charset.defaultCharset()).get(1);
        tmp = sizes.indexOf(' ', sizes.indexOf('\n'));
        tmp = Integer.parseInt(sizes.substring(tmp + 1).trim());
        return tmp - 1; // hspell stores the actual word count + 1
    }
	
	public static DictRadix<MorphData> loadDictionaryFromHSpellData(final File hspellFolder, boolean loadMorphData) throws IOException {
        if (!hspellFolder.exists() || !hspellFolder.isDirectory())
            throw new IllegalArgumentException("Invalid hspell data folder provided");

		if (loadMorphData) {
            // Load the count of morphological data slots required
            int lookupLen = getWordCountInHSpellFolder(hspellFolder);
			final String lookup[] = new String[lookupLen + 1];

            InputStream fdict = null;
            try {
                fdict = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.dictionaryFile)));
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

            final DictRadix<MorphData> ret = new DictRadix<MorphData>();
            InputStream fprefixes = null, fdesc = null, fstem = null;
            try {
                fprefixes = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.prefixesFile)));
                fdesc = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.descFile)));
                fstem = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.stemsFile)));

                final Loader loader = new Loader();
                for (int i = 0; lookup[i] != null; i++) {
                    MorphData data = new MorphData();
                    data.setPrefixes((short) fprefixes.read()); // Read prefix hint byte
                    data.setDescFlags(loader.readDescFile(fdesc));

                    final List<Integer> stemReferences = loader.readStemFile(fstem);
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
                }
            } finally {
                if (fprefixes != null) try { fprefixes.close(); } catch (IOException ignored) {}
                if (fdesc != null) try { fdesc.close(); } catch (IOException ignored) {}
                if (fstem != null) try { fstem.close(); } catch (IOException ignored) {}
            }

			return ret;

		} else { // Use optimized version for loading HSpell's dictionary files
			DictRadix<MorphData> ret = new DictRadix<MorphData>();

            InputStream fprefixes = null, fdict = null;
            try {
                fdict = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.dictionaryFile)));
                fprefixes = new GZIPInputStream(new FileInputStream(new File(hspellFolder, Constants.prefixesFile)));

                final char[] sbuf = new char[Constants.MaxWordLength];
                int c = 0, n, slen = 0;
                while ((c = fdict.read()) > -1) {
                    if ((c >= '0') && (c <= '9')) { // No conversion required for chars < 0xBE
                        // new word - finalize old word first (set value)
                        sbuf[slen] = '\0';

                        // TODO: Avoid creating new MorphData object, and enhance DictRadix to store
                        // the prefixes mask in the node itself
                        MorphData data = new MorphData();
                        data.setPrefixes((short) fprefixes.read()); // Read prefix hint byte
                        ret.addNode(sbuf, data);

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
                if (fprefixes != null) try { fprefixes.close(); } catch (IOException ignored) {}
                if (fdict != null) try { fdict.close(); } catch (IOException ignored) {}
            }

			return ret;
		}
	}

    private int bufPos = 0;
    private final int[] buf = new int[5];

    private final java.util.ArrayList<Integer> wordMasks = new java.util.ArrayList<Integer>();
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
                wordMasks.add(Constants.dmasks[i]);
                bufPos = 0;
                continue;
            }
        }
        return null;
    }

    // Note: What HSpell call "stems", which we define as lemmas
    private final java.util.ArrayList<Integer> wordStems = new java.util.ArrayList<Integer>();
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
}