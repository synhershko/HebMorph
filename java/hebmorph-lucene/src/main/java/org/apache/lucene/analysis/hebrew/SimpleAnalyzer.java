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

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.LingInfo;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public final class SimpleAnalyzer extends ReusableAnalyzerBase {
	/** An unmodifiable set containing some common Hebrew words that are usually not
	 useful for searching.
	*/
    private final CharArraySet commonWords;
	public static final DictRadix<Integer> PrefixTree = LingInfo.buildPrefixTree(false);

	private Map<String, char[]> suffixByTokenType = null;
    private final Version matchVersion;

    public SimpleAnalyzer(Version matchVersion) {
        this(matchVersion, null);
    }

    public SimpleAnalyzer(final Version matchVersion, final CharArraySet commonWords) {
        this.commonWords = commonWords;
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final HebrewTokenizer src = new HebrewTokenizer(reader, PrefixTree);
        TokenStream tok = new NiqqudFilter(src);
        tok = new LowerCaseFilter(matchVersion, tok);
        if (commonWords != null && commonWords.size() > 0)
            tok = new CommonGramsFilter(matchVersion, tok, commonWords, false);
        if ((suffixByTokenType != null) && (suffixByTokenType.size() > 0))
            tok = new AddSuffixFilter(tok, suffixByTokenType);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected boolean reset(final Reader reader) throws IOException {
                return super.reset(reader);
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