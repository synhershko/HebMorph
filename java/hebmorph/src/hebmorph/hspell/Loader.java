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
package hebmorph.hspell;

import hebmorph.MorphData;
import hebmorph.datastructures.DictRadix;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.zip.GZIPInputStream;



public class Loader
{

	private static class MorphDataLoader// implements IDisposable
	{
		private GZIPInputStream fdesc, fstem;

		private int bufPos = 0;
		private int[] buf = new int[5];

		public MorphDataLoader(String descPath, String stemPath) throws FileNotFoundException, IOException
		{
			fdesc = new GZIPInputStream(new FileInputStream(descPath));
			fstem = new GZIPInputStream(new FileInputStream(stemPath));
		}

		public void dispose()
		{
			try
			{
				fdesc.close();
			}
			catch(IOException e)
			{
			}
			try
			{
				fstem.close();
			}
			catch(IOException e)
			{
			}
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

	public static DictRadix<MorphData> loadDictionaryFromHSpellFolder(String path, boolean bLoadMorphData) throws IOException
	{
		if (path.charAt(path.length() - 1) != File.separatorChar)
		{
			path += File.separatorChar;
		}

		if (bLoadMorphData)
		{
			// Load the count of morphologic data slots required
			String sizesFile = readFileToString(new File(path + Constants.SizesFile));
			int lookupLen = sizesFile.indexOf(' ', sizesFile.indexOf('\n'));
			lookupLen = Integer.parseInt(sizesFile.substring(lookupLen + 1).trim());
			String[] lookup = new String[lookupLen + 1];

			GZIPInputStream fdict = new GZIPInputStream(new FileInputStream(path + Constants.DictionaryFile));
			try
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
			finally
			{
				fdict.close();
			}

			MorphDataLoader dataLoader = new MorphDataLoader(path + Constants.DescFile, path + Constants.StemsFile);
			try
			{
				GZIPInputStream fprefixes = new GZIPInputStream(new FileInputStream(path + Constants.PrefixesFile));
				try
				{
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
				finally
				{
					fprefixes.close();
				}
			}
			finally
			{
				dataLoader.dispose();
			}
		}
		else // Use optimized version for loading HSpell's dictionary files
		{

			GZIPInputStream fdict = new GZIPInputStream(new FileInputStream(path + Constants.DictionaryFile));
			try
			{
				GZIPInputStream fprefixes = new GZIPInputStream(new FileInputStream(path + Constants.PrefixesFile));
				try
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
				finally
				{
					fprefixes.close();
				}
			}
			finally
			{
				fdict.close();
			}
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

	private static String readFileToString(File file) throws IOException
	{
		FileInputStream fileIS = new FileInputStream(file);
		InputStreamReader input = new InputStreamReader(fileIS, "UTF-8");
		StringWriter output = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        input.close();
        return output.toString();
	}
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
}