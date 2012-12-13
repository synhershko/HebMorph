/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
 *                                                                         *
 *   Distributed under the GNU General Public License, Version 2.0.        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation (v2).                                    *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Steet, Fifth Floor, Boston, MA  02111-1307, USA.          *
 **************************************************************************/
package com.code972.hebmorph.hspell;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictRadix;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;



public class Loader
{
	public static final String DEFAULT_HSPELL_DATA_CLASSPATH = "hspell-data-files";


	private static class MorphDataLoader// implements IDisposable
	{
		private InputStream fdesc, fstem;

		private int bufPos = 0;
		private int[] buf = new int[5];

		public MorphDataLoader(InputStream fdesc, InputStream fstem)
		{
			this.fdesc = fdesc;
			this.fstem = fstem;
		}
		
		private java.util.ArrayList<Integer> wordMasks = new java.util.ArrayList<Integer>();
		public final Integer[] readDescFile() throws IOException
		{
			while ((buf[bufPos] = fdesc.read()) > -1)
			{
				// Break on EOL or EOF
				if ((buf[bufPos] == '\n') || (buf[bufPos] == 0))
				{
					bufPos = 0;
					Integer[] ret = wordMasks.toArray(new Integer[]{});
					wordMasks.clear();
					return ret;
				}
				bufPos++;
				if (bufPos % 2 == 0)
				{
					int i = buf[0] - 'A' + (buf[1] - 'A') * 26;
					wordMasks.add(Constants.dmasks[i]);
					bufPos = 0;
					continue;
				}
			}
			return null;
		}

		// Note: What HSpell call "stems", which we define as lemmas
		private java.util.ArrayList<Integer> wordStems = new java.util.ArrayList<Integer>();
		public final List<Integer> readStemFile() throws IOException
		{
			wordStems.clear();
			while ((buf[bufPos] = fstem.read()) > -1)
			{
				// Break on EOL or EOF
				if ((buf[bufPos] == '\n') || (buf[bufPos] == 0))
				{
					bufPos = 0;
					return wordStems;
				}
				bufPos++;
				if (bufPos % 3 == 0)
				{
					wordStems.add(buf[0] - 33 + (buf[1] - 33) * 94 + (buf[2] - 33) * 94 * 94);
					bufPos = 0;
					continue;
				}
			}
			return null;
		}
	}


    public static DictRadix<MorphData> loadDictionaryFromDefaultClasspath(boolean bLoadMorphData) throws IOException {
        return loadDictionaryFromClasspath(DEFAULT_HSPELL_DATA_CLASSPATH, bLoadMorphData);
    }

    public static DictRadix<MorphData> loadDictionaryFromClasspath(String pathInClasspath, boolean bLoadMorphData) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(pathInClasspath);
        if(url == null)
            throw new FileNotFoundException("Cannot find '" + pathInClasspath + "' in classpath.");
        return loadDictionaryFromUrl(url.toString(), bLoadMorphData);
    }

    
	public static DictRadix<MorphData> loadDictionaryFromUrl(String url, boolean bLoadMorphData) throws IOException {
		HspellData hspell = new HspellData(url);
		try {
			return loadDictionaryFromHSpellData(hspell, bLoadMorphData);
		}
		finally {
            hspell.close();
		}
	}
	
	private static DictRadix<MorphData> loadDictionaryFromHSpellData(HspellData hspell, boolean bLoadMorphData) throws IOException
	{
		InputStream fdict = hspell.getDictionaryStream();
		InputStream fprefixes = hspell.getPrefixesStream();
		InputStream fstem = hspell.getStemStream();
		InputStream fdesc = hspell.getDescriptionStream();
		
		if (bLoadMorphData)
		{
			String[] lookup = new String[hspell.getLookupLength()+ 1];
			{
			char[] sbuf = new char[Constants.MaxWordLength];
			int c = 0, n, slen = 0, i = 0;
			while ((c = fdict.read()) > -1)
			{
				if ((c >= '0') && (c <= '9')) // No conversion required for chars < 0xBE
				{
					// new word - finalize and save old word
					lookup[i++] = new String(sbuf, 0, slen);

					// and read how much to go back
					n = 0;
					do
					{
						// base 10...
						n *= 10;
						n += (c - '0');
					} while (((c = fdict.read()) > -1) && (c >= '0') && (c <= '9'));
					slen -= n;
				}
				sbuf[slen++] = ISO8859_To_Unicode(c);
			}
			}
			
			MorphDataLoader dataLoader = new MorphDataLoader(fdesc,fstem);
			DictRadix<MorphData> ret = new DictRadix<MorphData>();

			for (int i = 0; lookup[i] != null; i++)
			{
				MorphData data = new MorphData();
				//data.Prefixes = Byte.parseByte(fprefixes.ReadByte()); // Read prefix hint byte
				data.setPrefixes(fprefixes.read()); // Read prefix hint byte
				data.setDescFlags(dataLoader.readDescFile());

				List<Integer> stemReferences = dataLoader.readStemFile();
				data.setLemmas(new String[stemReferences.size()]);
				int stemPosition = 0;
				for (int r : stemReferences)
				{
					// This is a bypass for the psuedo-stem "שונות", as defined by hspell
					// TODO: Try looking into changing this in hspell itself
					if (lookup[r].equals("שונות") && !lookup[r].equals(lookup[i]))
					{
						data.getLemmas()[stemPosition++] = null;
					}
					else
					{
						data.getLemmas()[stemPosition++] = lookup[r];
					}
				}
				ret.addNode(lookup[i], data);
			}

			return ret;

		}
		else // Use optimized version for loading HSpell's dictionary files
		{

			DictRadix<MorphData> ret = new DictRadix<MorphData>();

			char[] sbuf = new char[Constants.MaxWordLength];
			int c = 0, n, slen = 0;
			while ((c = fdict.read()) > -1)
			{
				if ((c >= '0') && (c <= '9')) // No conversion required for chars < 0xBE
				{
					// new word - finalize old word first (set value)
					sbuf[slen] = '\0';

					// TODO: Avoid creating new MorphData object, and enhance DictRadix to store
					// the prefixes mask in the node itself
					MorphData data = new MorphData();
					data.setPrefixes(fprefixes.read()); // Read prefix hint byte
					ret.addNode(sbuf, data);

					// and read how much to go back
					n = 0;
					do
					{
						// base 10...
						n *= 10;
						n += (c - '0');
					} while (((c = fdict.read()) > -1) && (c >= '0') && (c <= '9'));
					slen -= n;
				}
				sbuf[slen++] = ISO8859_To_Unicode(c);
			}

			return ret;
		}
	}

	// Mapping is based on
	// http://www.unicode.org/Public/MAPPINGS/ISO8859/8859-8.TXT
	// 0xDF, 0xFD, 0xFE aren't converted
	private static char ISO8859_To_Unicode(int c)
	{
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