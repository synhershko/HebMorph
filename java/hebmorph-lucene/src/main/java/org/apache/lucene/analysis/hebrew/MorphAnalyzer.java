/***************************************************************************
 *   Copyright (C) 2010-2013 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com> (initial Java port)           *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.StreamLemmatizer;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class MorphAnalyzer extends Analyzer {
	/** An unmodifiable set containing some common Hebrew words that are usually not
	 useful for searching.
	*/
    private final CharArraySet commonWords;

	private boolean alwaysSaveMarkedOriginal = false;

	private LemmaFilterBase lemmaFilter;

	private final StreamLemmatizer hebMorphLemmatizer;
    private final SynonymMap acronymMergingMap;
    private static final String DEFAULT_HSPELL_DATA_CLASSPATH = "hspell-data-files";
    private static final String DEFAULT_HSPELL_ENV_VARIABLE = "HSPELL_DATA_FILES_PATH";
    protected final Version matchVersion;
    private Character suffixForExactMatch;

    public MorphAnalyzer(final Version matchVersion, final DictRadix<MorphData> dict, final DictRadix<Byte> specialTokenizationCases,
                         final CharArraySet commonWords) throws IOException {
        this(matchVersion, new StreamLemmatizer(null, dict, false, specialTokenizationCases), commonWords);
    }

    public MorphAnalyzer(final Version matchVersion, final DictRadix<MorphData> dict, final CharArraySet commonWords) throws IOException {
        this(matchVersion, new StreamLemmatizer(null, dict, false), commonWords);
    }

    /**
     * Initializes using data files at the default location on the classpath.
     */
    public MorphAnalyzer(final Version version) throws IOException {
        this(version, loadFromClasspath(DEFAULT_HSPELL_DATA_CLASSPATH), null);
    }

    /**
     * Initializes using data files at the specified location on the classpath.
     */
    public MorphAnalyzer(final Version version, final String hspellClasspath) throws IOException {
        this(version, hspellClasspath, null);
    }

    public MorphAnalyzer(final Version version, final String hspellClasspath, final CharArraySet commonWords) throws IOException {
        this(version, loadFromClasspath(hspellClasspath), commonWords);
    }

    /**
     * Initializes using data files at the specified location (hspellPath must be a directory).
     */
    public MorphAnalyzer(final Version version, final File hspellPath) throws IOException {
        this(version, hspellPath, null);
    }

    /**
     * Initializes using data files at the specified location (hspellPath must be a directory).
     */
    public MorphAnalyzer(final Version version, final File hspellPath, final CharArraySet commonWords) throws IOException {
        this(version, loadFromPath(hspellPath), commonWords);
    }

    public MorphAnalyzer(final Version matchVersion, final StreamLemmatizer hml, final CharArraySet commonWords) throws IOException {
        this.matchVersion = matchVersion;
        hebMorphLemmatizer = hml;
        acronymMergingMap = buildAcronymsMergingMap();
        this.commonWords = commonWords;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final StreamLemmasFilter src = new StreamLemmasFilter(reader, hebMorphLemmatizer, commonWords, lemmaFilter);
        src.setAlwaysSaveMarkedOriginal(alwaysSaveMarkedOriginal);
        src.setSuffixForExactMatch(suffixForExactMatch);

        TokenStream tok = new SynonymFilter(src, acronymMergingMap, false);
        if (commonWords != null && commonWords.size() > 0)
            tok = new CommonGramsFilter(matchVersion, tok, commonWords, false);
        tok = new SuffixKeywordFilter(tok, '$');
        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }

    public void setSuffixForExactMatch(Character suffixForExactMatch) {
        this.suffixForExactMatch = suffixForExactMatch;
    }

    /**
     A filter object to provide flexibility on deciding which lemmas are valid as index terms
     and which are not.
     */
    public void setLemmaFilter(LemmaFilterBase lemmaFilter) {
        this.lemmaFilter = lemmaFilter;
    }

    /**
     Set to true to mark tokens with a $ prefix also when there is only one lemma returned
     from the lemmatizer. This is mainly here to allow the Hebrew-aware SimpleAnalyzer (in this
     namespace) to perform searches on the same field used for the Morph analyzer. When used this
     way, make sure to turn this on only while indexing, so searches don't get slower.
     Default is false to save some index space.
     */
    public void setAlwaysSaveMarkedOriginal(boolean alwaysSaveMarkedOriginal) {
        this.alwaysSaveMarkedOriginal = alwaysSaveMarkedOriginal;
    }

    public static SynonymMap buildAcronymsMergingMap() throws IOException {
        SynonymMap.Builder synonymMap = new SynonymMap.Builder(true);
        synonymMap.add(new CharsRef("אף על פי כן"), new CharsRef("אעפ\"כ"), false);
        synonymMap.add(new CharsRef("אף על פי"), new CharsRef("אע\"פ"), false);
        synonymMap.add(new CharsRef("כמו כן"), new CharsRef("כמו\"כ"), false);
        synonymMap.add(new CharsRef("על ידי"), new CharsRef("ע\"י"), false);
        synonymMap.add(new CharsRef("על פי"), new CharsRef("ע\"פ"), false);
        synonymMap.add(new CharsRef("כל כך"), new CharsRef("כ\"כ"), false);
        synonymMap.add(new CharsRef("בדרך כלל"), new CharsRef("בד\"כ"), false);
        return synonymMap.build();
    }

    static private DictRadix<MorphData> loadFromClasspath(final String pathInClasspath) {
        try {
            return Loader.loadDictionaryFromClasspath(pathInClasspath, true);
        } catch (IOException ex) {
       		try {
       			// Try to use environment variable if failed with classpath
				return loadFromEnvVariable();
			} catch (IOException e) {
				throw new IllegalStateException("Failed to read data", ex);
			}
        }
    }

    static private DictRadix<MorphData> loadFromPath(final File path) {
        try {
            return Loader.loadDictionaryFromHSpellData(path, true);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read data", ex);
        }
    }
    
    static private DictRadix<MorphData> loadFromEnvVariable() throws IOException {
       	String hspellPath = System.getenv(DEFAULT_HSPELL_ENV_VARIABLE);
       	if (hspellPath == null) {
       		throw new IllegalStateException("Failed to load hspell dictionary files. They should be configured " +
       				"in classpath or by " + DEFAULT_HSPELL_ENV_VARIABLE + " environment variable");
       	}
       	return Loader.loadDictionaryFromHSpellData(new File(hspellPath), true);
    }
}