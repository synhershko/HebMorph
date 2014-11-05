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

import org.apache.lucene.analysis.AddSuffixFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CommonGramsFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class SimpleAnalyzer extends Analyzer {
    /**
     * An unmodifiable set containing some common Hebrew words that are usually not
     * useful for searching.
     */
    private final CharArraySet commonWords;

    private Map<String, char[]> suffixByTokenType = null;
    private HashMap<String, Integer> prefixesTree;
    private final Version matchVersion;
    private final SynonymMap acronymMergingMap;

    public SimpleAnalyzer(Version matchVersion, final HashMap<String, Integer> prefixes) throws IOException {
        this(matchVersion, prefixes, null);
    }

    public SimpleAnalyzer(final Version matchVersion, final HashMap<String, Integer> prefixes, final CharArraySet commonWords) throws IOException {
        this.commonWords = commonWords;
        this.matchVersion = matchVersion;
        this.prefixesTree = prefixes;
        this.acronymMergingMap = MorphAnalyzer.buildAcronymsMergingMap();
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final HebrewTokenizer src = new HebrewTokenizer(reader,prefixesTree);
        TokenStream tok = new NiqqudFilter(src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new SynonymFilter(tok, acronymMergingMap, false);
        if (commonWords != null && commonWords.size() > 0)
            tok = new CommonGramsFilter(matchVersion, tok, commonWords, false);
        if ((suffixByTokenType != null) && (suffixByTokenType.size() > 0))
            tok = new AddSuffixFilter(tok, suffixByTokenType);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }

    public void registerSuffix(String tokenType, String suffix) {
        if (suffixByTokenType == null)
            suffixByTokenType = new java.util.HashMap<>();

        if (!suffixByTokenType.containsKey(tokenType))
            suffixByTokenType.put(tokenType, suffix.toCharArray());
    }
}