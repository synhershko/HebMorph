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

import com.code972.hebmorph.datastructures.DictHebMorph;
import org.apache.lucene.analysis.AddSuffixFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import java.io.IOException;
import java.io.Reader;

public class HebrewExactAnalyzer extends HebrewAnalyzer {
    public HebrewExactAnalyzer(DictHebMorph dict) throws IOException {
        super(dict);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        // on exact - we don't care about suffixes at all, we always output original word with suffix only
        final HebrewTokenizer src = new HebrewTokenizer(reader, dict.getPref(), SPECIAL_TOKENIZATION_CASES);
        TokenStream tok = new NiqqudFilter(src);
        tok = new ASCIIFoldingFilter(tok);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new AddSuffixFilter(tok, '$') {
            @Override
            protected void handleCurrentToken() {
                if (CommonGramsFilter.GRAM_TYPE.equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric).equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Mixed).equals(typeAtt.type())) {
                    keywordAtt.setKeyword(true);
                    return;
                }

                suffixCurrent();
            }
        };
        return new TokenStreamComponents(src, tok);
    }
}
