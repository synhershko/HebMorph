/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.util.CharArraySet;

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

    public SimpleAnalyzer(final HashMap<String, Integer> prefixes) throws IOException {
        this(prefixes, null);
    }

    public SimpleAnalyzer(final HashMap<String, Integer> prefixes, final CharArraySet commonWords) throws IOException {
        this.commonWords = commonWords;
        this.prefixesTree = prefixes;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final HebrewTokenizer src = new HebrewTokenizer(reader, prefixesTree);
        TokenStream tok = new NiqqudFilter(src);
        tok = new LowerCaseFilter(tok);
        //consider adding a suffix filter?
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