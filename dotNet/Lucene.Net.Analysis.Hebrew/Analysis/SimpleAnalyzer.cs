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
using System.Collections.Generic;

namespace Lucene.Net.Analysis.Hebrew
{
    public class SimpleAnalyzer : Analyzer
    {
        /// <summary>An unmodifiable set containing some common Hebrew words that are usually not
        /// useful for searching.
        /// </summary>
        public static readonly ISet<String> STOP_WORDS_SET = StopFilter.MakeStopSet(HebMorph.StopWords.BasicStopWordsSet);
        public static readonly HebMorph.DataStructures.DictRadix<int> PrefixTree = HebMorph.HSpell.LingInfo.BuildPrefixTree(false);

        protected bool enableStopPositionIncrements = true;
        protected Dictionary<string, char[]> suffixByTokenType = null;

    	public SimpleAnalyzer()
    	{
			SetOverridesTokenStreamMethod<MorphAnalyzer>();
    	}

        public void RegisterSuffix(String tokenType, String suffix)
        {
            if (suffixByTokenType == null)
                suffixByTokenType = new Dictionary<string, char[]>();

            if (!suffixByTokenType.ContainsKey(tokenType))
                suffixByTokenType.Add(tokenType, suffix.ToCharArray());
        }

        // TODO: Support loading external stop lists

        private class SavedStreams
        {
            public Tokenizer source;
            public TokenStream result;
        };

        public override TokenStream ReusableTokenStream(string fieldName, System.IO.TextReader reader)
        {
			if (overridesTokenStreamMethod)
			{
				// LUCENE-1678: force fallback to tokenStream() if we
				// have been subclassed and that subclass overrides
				// tokenStream but not reusableTokenStream
				return TokenStream(fieldName, reader);
			}
            SavedStreams streams = PreviousTokenStream as SavedStreams;
            if (streams == null)
            {
                streams = new SavedStreams();

                streams.source = new HebrewTokenizer(reader, PrefixTree);

                // Niqqud normalization
                streams.result = new NiqqudFilter(streams.source);

                // TODO: should we ignoreCase in StopFilter?
                streams.result = new StopFilter(enableStopPositionIncrements, streams.result, STOP_WORDS_SET);

                // TODO: Apply LowerCaseFilter to NonHebrew tokens only
                streams.result = new LowerCaseFilter(streams.result);

                if (suffixByTokenType != null && suffixByTokenType.Count > 0)
                    streams.result = new AddSuffixFilter(streams.result, suffixByTokenType);

                PreviousTokenStream = streams;
            }
            else
            {
                streams.source.Reset(reader);
            }
            return streams.result;
        }

        public override TokenStream TokenStream(string fieldName, System.IO.TextReader reader)
        {
            TokenStream result = new HebrewTokenizer(reader, PrefixTree);

            // Niqqud normalization
            result = new NiqqudFilter(result);

            // TODO: should we ignoreCase in StopFilter?
            result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

            // TODO: Apply LowerCaseFilter to NonHebrew tokens only
            result = new LowerCaseFilter(result);

            if (suffixByTokenType != null && suffixByTokenType.Count > 0)
                result = new AddSuffixFilter(result, suffixByTokenType);

            return result;
        }
    }
}
