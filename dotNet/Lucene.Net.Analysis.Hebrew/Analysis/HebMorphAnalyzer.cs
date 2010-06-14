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
    public class HebMorphAnalyzer : Analyzer
    {
        /// <summary>An unmodifiable set containing some common Hebrew words that are usually not
        /// useful for searching.
        /// </summary>
        public static readonly System.Collections.Hashtable STOP_WORDS_SET = StopFilter.MakeStopSet(HebMorph.StopWords.BasicStopWordsSet);

        private bool enableStopPositionIncrements = true;
        private HebMorph.Lemmatizer hebMorphLemmatizer;

        public HebMorphAnalyzer(HebMorph.Lemmatizer hml)
            : base()
        {
            hebMorphLemmatizer = hml;
        }

        public HebMorphAnalyzer(string HSpellDataFilesPath)
            : base()
        {
            hebMorphLemmatizer = new HebMorph.Lemmatizer();
            hebMorphLemmatizer.InitFromHSpellFolder(HSpellDataFilesPath, true, false);
        }

        // TODO: Support loading external stop lists

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
                streams.source = new HebrewTokenizer(reader, hebMorphLemmatizer);
                
                // IMPORTANT: Currently we are filtering Niqqud characters _before_ passing it to HebMorph's analyzer.
                // Once the analyzer becomes aware of Niqqud characters, and uses them to correctly resolve or reduce ambiguities,
                // NiqqudFilter will need to be called _after_ using HebMorph analyzer.
                streams.result = new NiqqudFilter(streams.source);

                // TODO: should we ignoreCase in StopFilter?
                streams.result = new StopFilter(enableStopPositionIncrements, streams.result, STOP_WORDS_SET);

                if (hebMorphLemmatizer != null && hebMorphLemmatizer.IsInitialized)
                    streams.result = new HebMorphStemFilter(streams.result, hebMorphLemmatizer);

                // TODO: Apply LowerCaseFilter to NonHebrew tokens only
                streams.result = new LowerCaseFilter(streams.result);

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
            TokenStream result = new HebrewTokenizer(reader, hebMorphLemmatizer);

            // IMPORTANT: Currently we are filtering Niqqud characters _before_ passing it to HebMorph's analyzer.
            // Once the analyzer becomes aware of Niqqud characters, and uses them to correctly resolve or reduce ambiguities,
            // NiqqudFilter will need to be called _after_ using HebMorph analyzer.
            result = new NiqqudFilter(result);

            // TODO: should we ignoreCase in StopFilter?
            result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

            if (hebMorphLemmatizer != null && hebMorphLemmatizer.IsInitialized)
                result = new HebMorphStemFilter(result, hebMorphLemmatizer);

            // TODO: Apply LowerCaseFilter to NonHebrew tokens only
            result = new LowerCaseFilter(result);

            return result;
        }
    }
}
