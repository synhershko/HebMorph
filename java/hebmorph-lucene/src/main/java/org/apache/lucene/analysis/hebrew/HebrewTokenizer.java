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

import com.code972.hebmorph.Reference;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.LingInfo;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.io.Reader;


/**
 Tokenizes a given stream using HebMorph's Tokenizer, removes prefixes where possible, and tags Tokens
 with appropriate types where possible
*/
public final class HebrewTokenizer extends Tokenizer
{

	private final com.code972.hebmorph.Tokenizer hebMorphTokenizer;
	private final DictRadix<Integer> prefixesTree;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);;
//	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

	public HebrewTokenizer(final Reader _input) {
		this(_input, LingInfo.buildPrefixTree(false), null);
	}

    public HebrewTokenizer(final Reader _input, final DictRadix<Byte> specialCases) {
        this(_input, LingInfo.buildPrefixTree(false), specialCases);
    }

	public HebrewTokenizer(final Reader _input, final DictRadix<Integer> _prefixesTree, final DictRadix<Byte> specialCases) {
        super(_input);
		hebMorphTokenizer = new com.code972.hebmorph.Tokenizer(_input, specialCases);
		prefixesTree = _prefixesTree;
	}

    public void setSuffixForExactMatch(final Character suffixForExactMatch) {
        this.hebMorphTokenizer.setSuffixForExactMatch(suffixForExactMatch);
    }

	public static interface TOKEN_TYPES {
		public static final int Hebrew = 0;
		public static final int NonHebrew = 1;
		public static final int Numeric = 2;
		public static final int Construct = 3;
		public static final int Acronym = 4;
        public static final int Mixed = 5;
	}

	public static final String[] TOKEN_TYPE_SIGNATURES = new String[] {
		"<HEBREW>",
		"<NON_HEBREW>",
		"<NUM>",
		"<CONSTRUCT>",
		"<ACRONYM>",
        "<MIXED>",
		null
	};

	public static String tokenTypeSignature(final int tokenType) {
		return TOKEN_TYPE_SIGNATURES[tokenType];
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();

		final Reference<String> nextToken = new Reference<String>(null);
        String nextTokenVal = null;
		int tokenType;

		// Used to loop over certain noise cases
		while (true) {
			tokenType = hebMorphTokenizer.nextToken(nextToken);
            nextTokenVal = nextToken.ref;

			if (tokenType == 0)
				return false; // EOS

            if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Hebrew) > 0 && prefixesTree != null) {
                // Ignore "words" which are actually only prefixes in a single word.
                // This first case is easy to spot, since the prefix and the following word will be
                // separated by a dash marked as a construct (סמיכות) by the Tokenizer
                if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Construct) > 0)
                {
                    if (isLegalPrefix(nextToken.ref))
                        continue;
                }

                // This second case is a bit more complex. We take a risk of splitting a valid acronym or
                // abbrevated word into two, so we send it to an external function to analyze the word, and
                // get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
                if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Acronym) > 0) {
                    nextTokenVal = nextToken.ref = tryStrippingPrefix(nextToken.ref);

                    // Re-detect acronym, in case it was a false positive
                    if (nextTokenVal.indexOf('"') == -1) {
                        tokenType &= ~com.code972.hebmorph.Tokenizer.TokenType.Acronym;
                    }
                }
            }

			break;
		}

		// Record the term string
		
		termAtt.copyBuffer(nextTokenVal.toCharArray(), 0, nextTokenVal.length());
		offsetAtt.setOffset(correctOffset(hebMorphTokenizer.getOffset()), correctOffset(hebMorphTokenizer.getOffset() + hebMorphTokenizer.getLengthInSource()));
        if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) > 0)
            keywordAtt.setKeyword(true);

		if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Hebrew) > 0)
		{
			if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Acronym) > 0)
			{
				typeAtt.setType(tokenTypeSignature(TOKEN_TYPES.Acronym));
			}
			else if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Construct) > 0)
			{
				typeAtt.setType(tokenTypeSignature(TOKEN_TYPES.Construct));
			}
			else
			{
				typeAtt.setType(tokenTypeSignature(TOKEN_TYPES.Hebrew));
			}
		}
		else if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Numeric) > 0)
		{
			typeAtt.setType(tokenTypeSignature(TOKEN_TYPES.Numeric));
		}
		else
		{
			typeAtt.setType(tokenTypeSignature(TOKEN_TYPES.NonHebrew));
		}

		return true;
	}

	@Override
	public void end()
	{
		// set final offset
		int finalOffset = correctOffset(hebMorphTokenizer.getOffset());
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		hebMorphTokenizer.reset(input);
	}

	public boolean isLegalPrefix(final String str)
	{
        try {
            prefixesTree.lookup(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
	}

	// See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
	public String tryStrippingPrefix(String word)
	{
		// TODO: Make sure we conform to the academy rules as closely as possible

		int firstQuote = word.indexOf('"');

		if (firstQuote > -1 && firstQuote < word.length() - 2)
		{
			if (isLegalPrefix(word.substring(0, firstQuote)))
			{
				return word.substring(firstQuote + 1, firstQuote + 1 + word.length() - firstQuote - 1);
			}
		}

		int firstSingleQuote = word.indexOf('\'');
		if (firstSingleQuote == -1)
		{
			return word;
		}

		if ((firstQuote > -1) && (firstSingleQuote > firstQuote))
		{
			return word;
		}

		if (isLegalPrefix(word.substring(0, firstSingleQuote)))
		{
			return word.substring(firstSingleQuote + 1, firstSingleQuote + 1 + word.length() - firstSingleQuote - 1);
		}

		return word;
	}
}