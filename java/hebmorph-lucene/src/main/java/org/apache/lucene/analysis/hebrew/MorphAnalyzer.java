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
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SuffixKeywordFilter;
import org.apache.lucene.analysis.TokenStream;
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

	private boolean keepOriginalWord = false;

	private LemmaFilterBase lemmaFilter;

    private static final String DEFAULT_HSPELL_DATA_CLASSPATH = "hspell-data-files";
    private static final String DEFAULT_HSPELL_ENV_VARIABLE = "HSPELL_DATA_FILES_PATH";
    protected final Version matchVersion;
    private DictRadix<Byte> specialTokenizationCases;
    private final DictHebMorph dict;
    private Character suffixForExactMatch;

    public MorphAnalyzer(final Version matchVersion, final DictHebMorph dict) throws IOException {
        this(matchVersion, dict, null, null);
    }

    public MorphAnalyzer(final Version matchVersion, final DictHebMorph dict,
                         final CharArraySet commonWords, final DictRadix<Byte> specialTokenizationCases) throws IOException {
        this.matchVersion = matchVersion;
        this.dict = dict;
        this.specialTokenizationCases = specialTokenizationCases;
        this.commonWords = commonWords;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final StreamLemmasFilter src = new StreamLemmasFilter(reader, dict, specialTokenizationCases, commonWords, lemmaFilter);
        src.setKeepOriginalWord(keepOriginalWord);
        src.setSuffixForExactMatch(suffixForExactMatch);
        TokenStream tok = new SuffixKeywordFilter(src, '$');
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
    public void setKeepOriginalWord(boolean keepOriginalWord) {
        this.keepOriginalWord = keepOriginalWord;
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
        synonymMap.add(new CharsRef("תל אביב"), new CharsRef("ת\"א"), false);
        return synonymMap.build();
    }

    static private DictRadix<MorphData> loadFromClasspath(final String pathInClasspath) {
        try {
            Loader loader = new Loader(Thread.currentThread().getContextClassLoader(), pathInClasspath, true);
            return loader.loadDictionaryFromHSpellData();
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
            Loader loader = new Loader(path, true);
            return loader.loadDictionaryFromHSpellData();
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
       	Loader loader = new Loader(new File(hspellPath), true);
        return loader.loadDictionaryFromHSpellData();
    }
}