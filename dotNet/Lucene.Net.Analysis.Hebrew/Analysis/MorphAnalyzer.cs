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

using HebMorph;
using HebMorph.DataStructures;

namespace Lucene.Net.Analysis.Hebrew
{
    public class MorphAnalyzer : Analyzer
    {
        /// <summary>An unmodifiable set containing some common Hebrew words that are usually not
        /// useful for searching.
        /// </summary>
        public static readonly System.Collections.Generic.ISet<string> STOP_WORDS_SET = StopFilter.MakeStopSet(HebMorph.StopWords.BasicStopWordsSet);

        /// <summary>
        /// Set to true to mark tokens with a $ prefix also when there is only one lemma returned
        /// from the lemmatizer. This is mainly here to allow the Hebrew-aware SimpleAnalyzer (in this
        /// namespace) to perform searches on the same field used for the Morph analyzer. When used this
        /// way, make sure to turn this on only while indexing, so searches don't get slower.
        /// Default is false to save some index space.
        /// </summary>
        public bool alwaysSaveMarkedOriginal = false;

        /// <summary>
        /// Returns a boolean value indicating whether or not the MorphAnalyzer is ready to be used (useful
        /// for reusing the analyzer object, and for checking if loading of auxiliary data was successful).
        /// </summary>
        public bool IsInitialized { get { return (hebMorphLemmatizer != null && hebMorphLemmatizer.IsInitialized); } }

        /// <summary>
        /// A filter object to provide flexibility on deciding which lemmas are valid as index terms
        /// and which are not.
        /// </summary>
        public HebMorph.LemmaFilters.LemmaFilterBase lemmaFilter = null;

        protected bool enableStopPositionIncrements = true;
        protected readonly HebMorph.StreamLemmatizer hebMorphLemmatizer;

		public MorphAnalyzer(MorphAnalyzer other)
			: base()
		{
			hebMorphLemmatizer = other.hebMorphLemmatizer;
			SetOverridesTokenStreamMethod<MorphAnalyzer>();
		}

        public MorphAnalyzer(HebMorph.StreamLemmatizer hml)
            : base()
        {
            hebMorphLemmatizer = hml;
			SetOverridesTokenStreamMethod <MorphAnalyzer>();
        }

        public MorphAnalyzer(string HSpellDataFilesPath)
            : base()
        {
			hebMorphLemmatizer = new StreamLemmatizer(HSpellDataFilesPath, true, false);
			SetOverridesTokenStreamMethod<MorphAnalyzer>();
        }

	    public MorphAnalyzer(DictRadix<MorphData> dict)
	    {
			hebMorphLemmatizer = new HebMorph.StreamLemmatizer(dict, false);
	    }

	    protected class SavedStreams
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
				PreviousTokenStream = streams;
                streams.source = new StreamLemmasFilter(reader, hebMorphLemmatizer,
                    lemmaFilter, alwaysSaveMarkedOriginal);

                // This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
                // all by itself
                streams.result = new StopFilter(enableStopPositionIncrements, streams.source, STOP_WORDS_SET);
            }
            else
            {
                streams.source.Reset(reader);
            }
            return streams.result;
        }

        public override TokenStream TokenStream(string fieldName, System.IO.TextReader reader)
        {
            TokenStream result = new StreamLemmasFilter(reader, hebMorphLemmatizer,
                lemmaFilter, alwaysSaveMarkedOriginal);

            // This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
            // all by itself
            result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

            return result;
        }
    }
}
