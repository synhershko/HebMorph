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
using System.Collections.Generic;
using System.Text;

namespace Lucene.Net.Analysis.Hebrew
{
    public class MorphAnalyzer : Analyzer
    {
        /// <summary>An unmodifiable set containing some common Hebrew words that are usually not
        /// useful for searching.
        /// </summary>
        public static readonly System.Collections.Hashtable STOP_WORDS_SET = StopFilter.MakeStopSet(HebMorph.StopWords.BasicStopWordsSet);

        private bool enableStopPositionIncrements = true;
        private HebMorph.StreamLemmatizer hebMorphLemmatizer;

        public MorphAnalyzer(HebMorph.StreamLemmatizer hml)
            : base()
        {
            hebMorphLemmatizer = hml;
        }

        public MorphAnalyzer(string HSpellDataFilesPath)
            : base()
        {
            hebMorphLemmatizer = new HebMorph.StreamLemmatizer();
            hebMorphLemmatizer.InitFromHSpellFolder(HSpellDataFilesPath, true, false);
        }

        private class SavedStreams
        {
            public Tokenizer source;
            public TokenStream result;
        };

        public override TokenStream ReusableTokenStream(string fieldName, System.IO.TextReader reader)
        {
            SavedStreams streams = GetPreviousTokenStream() as SavedStreams;
            if (streams == null)
            {
                streams = new SavedStreams();
                streams.source = new StreamLemmasFilter(reader, hebMorphLemmatizer);

                // This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
                // all by itself
                streams.result = new StopFilter(enableStopPositionIncrements, streams.source, STOP_WORDS_SET);

                SetPreviousTokenStream(streams);
            }
            else
            {
                streams.source.Reset(reader);
            }
            return streams.result;
        }

        public override TokenStream TokenStream(string fieldName, System.IO.TextReader reader)
        {
            TokenStream result = new StreamLemmasFilter(reader, hebMorphLemmatizer);

            // This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
            // all by itself
            result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

            return result;
        }
    }
}
