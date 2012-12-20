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

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.StopWords;
import com.code972.hebmorph.StreamLemmatizer;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

public class MorphAnalyzer extends StopwordAnalyzerBase {
	/** An unmodifiable set containing some common Hebrew words that are usually not
	 useful for searching.
	*/
	public static final Set STOP_WORDS_SET = StopFilter.makeStopSet(Version.LUCENE_36, StopWords.BasicStopWordsSet);

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

	private final StreamLemmatizer hebMorphLemmatizer;
    private static final String DEFAULT_HSPELL_DATA_CLASSPATH = "hspell-data-files";

    public MorphAnalyzer(final Version version, final DictRadix<MorphData> dict) {
        super(version, STOP_WORDS_SET);
        hebMorphLemmatizer = new StreamLemmatizer(dict, false);
    }

    public MorphAnalyzer(final Version version, final StreamLemmatizer hml) {
        super(version, STOP_WORDS_SET);
        hebMorphLemmatizer = hml;
    }

    /**
     * Initializes using data files at the default location on the classpath.
     */
	public MorphAnalyzer(final Version version) {
        this(version, loadFromClasspath(DEFAULT_HSPELL_DATA_CLASSPATH));
	}

    /**
     * Initializes using data files at the specified location on the classpath.
     */
    public MorphAnalyzer(final Version version, final String hspellClasspath) {
        this(version, loadFromClasspath(hspellClasspath));
    }

    /**
     * Initializes using data files at the specified location (hspellPath must be a directory).
     */
    public MorphAnalyzer(final Version version, final File hspellPath) {
        this(version, loadFromPath(hspellPath));
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final StreamLemmasFilter src = new StreamLemmasFilter(reader, hebMorphLemmatizer, lemmaFilter, alwaysSaveMarkedOriginal);
        TokenStream tok = new LowerCaseFilter(matchVersion, src);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected boolean reset(final Reader reader) throws IOException {
                return super.reset(reader);
            }
        };
    }

    static private DictRadix<MorphData> loadFromClasspath(String pathInClasspath) {
        try {
            return Loader.loadDictionaryFromClasspath(pathInClasspath, true);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read data", ex);
        }
    }

    static private DictRadix<MorphData> loadFromPath(File path) {
        try {
            return Loader.loadDictionaryFromHSpellData(path, true);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read data", ex);
        }
    }
}