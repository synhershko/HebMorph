/***************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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
 ***************************************************************************/

using System;
using System.Text;
using System.Data;
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
            private GZipStream fdesc, fstem;

            private int bufPos = 0;
            private int[] buf = new int[5];

            internal MorphDataLoader(string descPath, string stemPath)
            {
                fdesc = new GZipStream(File.OpenRead(descPath), CompressionMode.Decompress);
                fstem = new GZipStream(File.OpenRead(stemPath), CompressionMode.Decompress);
            }

            private List<DMask> wordMasks = new List<DMask>();
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
                        wordMasks.Add((DMask)HSpell.Constants.dmasks[i]);
                        bufPos = 0;
                        continue;
                    }
                }
                return null;
            }

            // Note: What HSpell call "stems", which we define as lemmas
            private List<int> wordStems = new List<int>();
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

            #region IDisposable Members

            public void Dispose()
            {
                fdesc.Dispose();
                fstem.Dispose();
            }

            #endregion
        }
        #endregion

        public static DictRadix<MorphData> LoadDictionaryFromHSpellFolder(string path, bool bLoadMorphData)
        {
            if (path[path.Length - 1] != Path.DirectorySeparatorChar)
                path += Path.DirectorySeparatorChar;

            if (bLoadMorphData)
            {
                // Load the count of morphologic data slots required
                string sizesFile = File.ReadAllText(path + HSpell.Constants.SizesFile);
                int lookupLen = sizesFile.IndexOf(' ', sizesFile.IndexOf('\n'));
                lookupLen = Convert.ToInt32(sizesFile.Substring(lookupLen + 1));
                string[] lookup = new string[lookupLen + 1];

                using (GZipStream fdict = new GZipStream(File.OpenRead(path + HSpell.Constants.DictionaryFile), CompressionMode.Decompress))
                {
                    char[] sbuf = new char[HSpell.Constants.MaxWordLength];
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

                using (MorphDataLoader dataLoader = new MorphDataLoader(path + HSpell.Constants.DescFile,
                        path + HSpell.Constants.StemsFile))
                {
                    using (GZipStream fprefixes = new GZipStream(File.OpenRead(path + HSpell.Constants.PrefixesFile), CompressionMode.Decompress))
                    {
                        DictRadix<MorphData> ret = new DictRadix<MorphData>();

                        for (int i = 0; lookup[i] != null; i++)
                        {
                            MorphData data = new MorphData();
                            data.Prefixes = Convert.ToByte(fprefixes.ReadByte()); // Read prefix hint byte
                            data.DescFlags = dataLoader.ReadDescFile();

                            List<int> stemReferences = dataLoader.ReadStemFile();
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
            }
            else // Use optimized version for loading HSpell's dictionary files
            {
                using (GZipStream fdict = new GZipStream(File.OpenRead(path + HSpell.Constants.DictionaryFile), CompressionMode.Decompress))
                {
                    using (GZipStream fprefixes = new GZipStream(File.OpenRead(path + HSpell.Constants.PrefixesFile), CompressionMode.Decompress))
                    {
                        DictRadix<MorphData> ret = new DictRadix<MorphData>();

                        char[] sbuf = new char[HSpell.Constants.MaxWordLength];
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
