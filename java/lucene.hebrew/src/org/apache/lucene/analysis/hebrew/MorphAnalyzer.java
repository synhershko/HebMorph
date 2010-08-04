/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
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
package org.apache.lucene.analysis.hebrew;

import hebmorph.StopWords;
import hebmorph.StreamLemmatizer;
import hebmorph.lemmafilters.LemmaFilterBase;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.StreamLemmasFilter;

public class MorphAnalyzer extends Analyzer
{
	/** An unmodifiable set containing some common Hebrew words that are usually not
	 useful for searching.

	*/
	public static final Set STOP_WORDS_SET = StopFilter.makeStopSet(StopWords.BasicStopWordsSet);

	/**
	 Set to true to mark tokens with a $ prefix also when there is only one lemma returned
	 from the lemmatizer. This is mainly here to allow the Hebrew-aware SimpleAnalyzer (in this
	 namespace) to perform searches on the same field used for the Morph analyzer. When used this
	 way, make sure to turn this on only while indexing, so searches don't get slower.
	 Default is false to save some index space.

	*/
	public boolean alwaysSaveMarkedOriginal = false;

	/**
	 A filter object to provide flexibility on deciding which lemmas are valid as index terms
	 and which are not.

	*/
	public LemmaFilterBase lemmaFilter = null;

	private boolean enableStopPositionIncrements = true;
	private StreamLemmatizer hebMorphLemmatizer;

	public MorphAnalyzer(StreamLemmatizer hml)
	{
		super();
		hebMorphLemmatizer = hml;
	}

	public MorphAnalyzer(String HSpellDataFilesPath) throws IOException
	{
		super();
		hebMorphLemmatizer = new StreamLemmatizer();
		hebMorphLemmatizer.initFromHSpellFolder(HSpellDataFilesPath, true, false);
	}

	private static class SavedStreams
	{
		public Tokenizer source;
		public TokenStream result;
	}

	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws java.io.IOException
	{
		Object tempVar = getPreviousTokenStream();
		SavedStreams streams = (SavedStreams)((tempVar instanceof SavedStreams) ? tempVar : null);
		if (streams == null)
		{
			streams = new SavedStreams();
			streams.source = new StreamLemmasFilter(reader, hebMorphLemmatizer, lemmaFilter, alwaysSaveMarkedOriginal);

			// This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
			// all by itself
			streams.result = new StopFilter(enableStopPositionIncrements, streams.source, STOP_WORDS_SET);

			setPreviousTokenStream(streams);
		}
		else
		{
			streams.source.reset(reader);
		}
		return streams.result;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader)
	{
		TokenStream result = new StreamLemmasFilter(reader, hebMorphLemmatizer, lemmaFilter, alwaysSaveMarkedOriginal);

		// This stop filter is here temporarily, until HebMorph is smart enough to clear stop words
		// all by itself
		result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

		return result;
	}
}