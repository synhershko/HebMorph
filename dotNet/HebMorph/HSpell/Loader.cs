/***************************************************************************
 * HebMorph - making Hebrew properly searchable
 * 
 *   Copyright (C) 2010-2012                                               
 *      Itamar Syn-Hershko <itamar at code972 dot com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

using System;
using System.IO;
using System.IO.Compression;
using System.Collections.Generic;

using HebMorph.DataStructures;

namespace HebMorph.HSpell
{
    public class Loader
    {
        #region MorphDataLoader helper class
        private class MorphDataLoader : IDisposable
        {
            private readonly GZipStream fdesc, fstem;
            private List<int> dmasks;
            private int bufPos = 0;
            private readonly int[] buf = new int[5];

            internal MorphDataLoader(string descPath, string stemPath, string dmasksInfo)
            {
                fdesc = new GZipStream(File.OpenRead(descPath), CompressionMode.Decompress);
                fstem = new GZipStream(File.OpenRead(stemPath), CompressionMode.Decompress);
                dmasks = generateDmasks(dmasksInfo);
            }

            private readonly List<DMask> wordMasks = new List<DMask>();
            internal DMask[] ReadDescFile()
            {
                while ((buf[bufPos] = fdesc.ReadByte()) > -1)
                {
                    // Break on EOL or EOF
                    if (buf[bufPos] == '\n' || buf[bufPos] == 0)
                    {
                        bufPos = 0;
                        DMask[] ret = wordMasks.ToArray();
                        wordMasks.Clear();
                        return ret;
                    }
                    bufPos++;
                    if (bufPos % 2 == 0)
                    {
                        int i = buf[0] - 'A' + (buf[1] - 'A') * 26;
                        //wordMasks.Add((DMask)Constants.dmasks[i]);
                        wordMasks.Add((DMask)dmasks[i]);
                        bufPos = 0;
                        continue;
                    }
                }
                return null;
            }

            // Note: What HSpell call "stems", which we define as lemmas
            private readonly List<int> wordStems = new List<int>();
            internal List<int> ReadStemFile()
            {
                wordStems.Clear();
                while ((buf[bufPos] = fstem.ReadByte()) > -1)
                {
                    // Break on EOL or EOF
                    if (buf[bufPos] == '\n' || buf[bufPos] == 0)
                    {
                        bufPos = 0;
                        return wordStems;
                    }
                    bufPos++;
                    if (bufPos % 3 == 0)
                    {
                        wordStems.Add(buf[0] - 33 + (buf[1] - 33) * 94 + (buf[2] - 33) * 94 * 94);
                        bufPos = 0;
                        continue;
                    }
                }
                return null;
            }

            private static List<int> generateDmasks(string path)
            {
                List<int> dmasks = new List<int>();
                Boolean foundStartLine = false;
                String line;
                using (var fprefixes = new StreamReader(path))
                {
                    while ((line = fprefixes.ReadLine()) != null)
                    {
                        if (!foundStartLine)
                        {
                            if (line.Contains("dmasks[]"))
                            {
                                foundStartLine = true;
                            }
                            continue;
                        }
                        int i = tryParseInt(line);
                        if (i >= 0)
                        {
                            dmasks.Add(i);
                        }
                    }
                }
                return dmasks;
            }

            //Retrieves the integer value of string (which may end with ','). Returns -1 if cannot convert.
            private static int tryParseInt(String str)
            {
                if (str == null)
                {
                    return -1;
                }
                int length = str.Length;
                length = str.EndsWith(",") ? length - 1 : length;
                if (length == 0)
                {
                    return -1;
                }
                int num = 0;
                for (int i = 0; i < length; i++)
                {
                    char c = str[i];
                    if (c <= '/' || c >= ':')
                    {
                        return -1;
                    }
                    int digit = (int)c - (int)'0';
                    num *= 10;
                    num += digit;
                }
                return num;
            }

            #region IDisposable Members

            public void Dispose()
            {
                fdesc.Dispose();
                fstem.Dispose();
            }

            #endregion
        }
        #endregion

        public static int GetWordCountInHSpellFolder(string path)
        {
            string sizesFile = File.ReadAllText(Path.Combine(path, HSpell.Constants.SizesFile));
            int tmp = sizesFile.IndexOf(' ', sizesFile.IndexOf('\n'));
            tmp = Convert.ToInt32(sizesFile.Substring(tmp + 1));
            return tmp - 1; // hspell stores the actual word count + 1
        }


        public static DictRadix<MorphData> LoadDictionaryFromHSpellFolder(string path, bool bLoadMorphData)
        {
            if (path[path.Length - 1] != Path.DirectorySeparatorChar)
                path += Path.DirectorySeparatorChar;

            if (bLoadMorphData)
            {
                // Load the count of morphological data slots required
                int lookupLen = GetWordCountInHSpellFolder(path);
                var lookup = new string[lookupLen + 1];

                using (GZipStream fdict = new GZipStream(File.OpenRead(path + Constants.DictionaryFile), CompressionMode.Decompress))
                {
                    var sbuf = new char[Constants.MaxWordLength];
                    int c = 0, n, slen = 0, i = 0;
                    while ((c = fdict.ReadByte()) > -1)
                    {
                        if (c >= '0' && c <= '9') // No conversion required for chars < 0xBE
                        {
                            /* new word - finalize and save old word */
                            lookup[i++] = new string(sbuf, 0, slen);

                            /* and read how much to go back */
                            n = 0;
                            do
                            {
                                /* base 10... */
                                n *= 10;
                                n += (c - '0');
                            } while ((c = fdict.ReadByte()) > -1 && c >= '0' && c <= '9');
                            slen -= n;
                        }
                        sbuf[slen++] = ISO8859_To_Unicode(c);
                    }
                }

                using (var dataLoader = new MorphDataLoader(path + HSpell.Constants.DescFile, path + Constants.StemsFile, path + HSpell.Constants.DmasksFile))
                using (var fprefixes = new GZipStream(File.OpenRead(path + HSpell.Constants.PrefixesFile), CompressionMode.Decompress))
                {
                    DictRadix<MorphData> ret = new DictRadix<MorphData>();

                    for (int i = 0; lookup[i] != null; i++)
                    {
                        MorphData data = new MorphData();
                        data.Prefixes = Convert.ToByte(fprefixes.ReadByte()); // Read prefix hint byte
                        data.DescFlags = dataLoader.ReadDescFile();

                        var stemReferences = dataLoader.ReadStemFile();
                        data.Lemmas = new string[stemReferences.Count];
                        int stemPosition = 0;
                        foreach (int r in stemReferences)
                        {
                            // This is a bypass for the psuedo-stem "שונות", as defined by hspell
                            // TODO: Try looking into changing this in hspell itself
                            if (lookup[r].Equals("שונות") && !lookup[r].Equals(lookup[i]))
                            {
                                data.Lemmas[stemPosition++] = null;
                            }
                            else
                            {
                                data.Lemmas[stemPosition++] = lookup[r];
                            }
                        }
                        ret.AddNode(lookup[i], data);
                    }

                    return ret;
                }
            }
            else // Use optimized version for loading HSpell's dictionary files
            {
                using (var fdict = new GZipStream(File.OpenRead(path + HSpell.Constants.DictionaryFile), CompressionMode.Decompress))
                using (var fprefixes = new GZipStream(File.OpenRead(path + HSpell.Constants.PrefixesFile), CompressionMode.Decompress))
                {
                    var ret = new DictRadix<MorphData>();

                    var sbuf = new char[HSpell.Constants.MaxWordLength];
                    int c = 0, n, slen = 0;
                    while ((c = fdict.ReadByte()) > -1)
                    {
                        if (c >= '0' && c <= '9') // No conversion required for chars < 0xBE
                        {
                            /* new word - finalize old word first (set value) */
                            sbuf[slen] = '\0';

                            // TODO: Avoid creating new MorphData object, and enhance DictRadix to store
                            // the prefixes mask in the node itself
                            MorphData data = new MorphData();
                            data.Prefixes = Convert.ToByte(fprefixes.ReadByte()); // Read prefix hint byte
                            ret.AddNode(sbuf, data);

                            /* and read how much to go back */
                            n = 0;
                            do
                            {
                                /* base 10... */
                                n *= 10;
                                n += (c - '0');
                            } while ((c = fdict.ReadByte()) > -1 && c >= '0' && c <= '9');
                            slen -= n;
                        }
                        sbuf[slen++] = ISO8859_To_Unicode(c);
                    }

                    return ret;
                }
            }
        }

        // Mapping is based on
        // http://www.unicode.org/Public/MAPPINGS/ISO8859/8859-8.TXT
        // 0xDF, 0xFD, 0xFE aren't converted
        private static char ISO8859_To_Unicode(int c)
        {
            if (c >= 0xE0 && c <= 0xFA)
                return (char)(c + 0x4F0);
            else if (c <= 0xBE)
                return (char)c;
            return ' ';
        }
    }
}
