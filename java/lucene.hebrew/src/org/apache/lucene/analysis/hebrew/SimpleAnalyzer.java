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

package org.apache.lucene.analysis.hebrew;

import hebmorph.datastructures.DictRadix;
import hebmorph.hspell.LingInfo;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.AddSuffixFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

public class SimpleAnalyzer extends Analyzer
{
	/** An unmodifiable set containing some common Hebrew words that are usually not
	 useful for searching.

	*/
	public static final Set STOP_WORDS_SET = StopFilter.makeStopSet(hebmorph.StopWords.BasicStopWordsSet);
	public static final DictRadix<Integer> PrefixTree = LingInfo.buildPrefixTree(false);

	private boolean enableStopPositionIncrements = true;
	private Map<String, char[]> suffixByTokenType = null;

	public void registerSuffix(String tokenType, String suffix)
	{
		if (suffixByTokenType == null)
		{
			suffixByTokenType = new java.util.HashMap<String, char[]>();
		}

		if (!suffixByTokenType.containsKey(tokenType))
		{
			suffixByTokenType.put(tokenType, suffix.toCharArray());
		}
	}

	// TODO: Support loading external stop lists

	private static class SavedStreams
	{
		public Tokenizer source;
		public TokenStream result;
	}

	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
	{
		Object tempVar = getPreviousTokenStream();
		SavedStreams streams = (SavedStreams)((tempVar instanceof SavedStreams) ? tempVar : null);
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

			if ((suffixByTokenType != null) && (suffixByTokenType.size() > 0))
			{
				streams.result = new AddSuffixFilter(streams.result, suffixByTokenType);
			}

			setPreviousTokenStream(streams);
		}
		else
		{
			streams.source.reset(reader);
		}
		return streams.result;
	}

	@Override
	public TokenStream tokenStream(String arg0, Reader reader)
	{
		TokenStream result = new HebrewTokenizer(reader, PrefixTree);

		// Niqqud normalization
		result = new NiqqudFilter(result);

		// TODO: should we ignoreCase in StopFilter?
		result = new StopFilter(enableStopPositionIncrements, result, STOP_WORDS_SET);

		// TODO: Apply LowerCaseFilter to NonHebrew tokens only
		result = new LowerCaseFilter(result);

		if ((suffixByTokenType != null) && (suffixByTokenType.size() > 0))
		{
			result = new AddSuffixFilter(result, suffixByTokenType);
		}

		return result;
	}
}