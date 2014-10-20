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

import com.code972.hebmorph.*;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StreamLemmasFilter extends Tokenizer
{
	private final StreamLemmatizer _streamLemmatizer;
    private final CharArraySet commonWords;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final CharacterUtils charUtils;

	private final LemmaFilterBase lemmaFilter;
	private final List<Token> stack = new ArrayList<Token>();
	private final List<Token> filterCache = new ArrayList<Token>();
	private int index = 0;
    private final Set<String> previousLemmas = new HashSet<String>();
    private boolean keepOriginalWord;

    public StreamLemmasFilter(final Reader input, final DictHebMorph dict) {
		this(input, dict, null, null, null);
	}

	public StreamLemmasFilter(final Reader input, final DictHebMorph dict, final LemmaFilterBase lemmaFilter) {
        this(input, dict, null, null, lemmaFilter);
    }

    public StreamLemmasFilter(final Reader input, final DictHebMorph dict, final CharArraySet commonWords, final LemmaFilterBase lemmaFilter) {
        this(input, dict, null, commonWords, lemmaFilter);
    }

    public StreamLemmasFilter(Reader _input, DictHebMorph dict,
                              DictRadix<Byte> specialTokenizationCases, CharArraySet commonWords, LemmaFilterBase lemmaFilter) {
        super(_input);
        _streamLemmatizer = new StreamLemmatizer(input, dict, specialTokenizationCases);
        this.commonWords = commonWords != null ? commonWords : CharArraySet.EMPTY_SET;
        this.lemmaFilter = lemmaFilter;
        charUtils = CharacterUtils.getInstance(Version.LUCENE_46);
    }

    public void setSuffixForExactMatch(Character c){
        _streamLemmatizer.setSuffixForExactMatch(c);
    }

    public void setCustomWords(DictRadix<MorphData> customWords) {
        _streamLemmatizer.setCustomWords(customWords);
    }

    private final Reference<String> tempRefObject = new Reference<>("");

    private int currentStartOffset, currentEndOffset;

	@Override
	public final boolean incrementToken() throws IOException {
        clearAttributes();

		// Index all unique lemmas at the same position
        while (index < stack.size()) {
            final HebrewToken res = (HebrewToken)((stack.get(index) instanceof HebrewToken) ? stack.get(index) : null);
            index++;

            if ((res == null) || !previousLemmas.add(res.getLemma())) // Skip multiple lemmas (we will merge morph properties later)
                continue;

            createHebrewToken(res);
            offsetAtt.setOffset(currentStartOffset, currentEndOffset);
            typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));
            posIncrAtt.setPositionIncrement(0);

            return true;
        }

        // Reset state
        index = 0;
		stack.clear();
        previousLemmas.clear();

		// Lemmatize next word in stream. The HebMorph lemmatizer will always return a token, unless
		// an unrecognized Hebrew word is hit, then an empty tokens array will be returned.
		final int tokenType = _streamLemmatizer.getLemmatizeNextToken(tempRefObject, stack);
        if (tokenType == 0) { // EOS
			return false;
        }

		// Store the location of the word in the original stream
        currentStartOffset = correctOffset(_streamLemmatizer.getStartOffset());
        currentEndOffset = correctOffset(_streamLemmatizer.getEndOffset());
		offsetAtt.setOffset(currentStartOffset, currentEndOffset);

        final String word = tempRefObject.ref;
        if (commonWords.contains(word)) { // common words should be treated later using dedicated filters
            termAtt.copyBuffer(word.toCharArray(), 0, word.length());
            typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));
            stack.clear();

            if (!keepOriginalWord) {
                if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) > 0) {
                    keywordAtt.setKeyword(true);
                }
                return true;
            }

            keywordAtt.setKeyword(true);
            if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) == 0) {
                stack.add(new HebrewToken(word, (byte)0, 0, word, 1.0f));
            }

            return true;
        }

        // Mark request for exact matches in queries, if configured in the tokenizer
        if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) > 0) {
            keywordAtt.setKeyword(true);
        }

        // A non-Hebrew word
        if (stack.size() == 1 && !(stack.get(0) instanceof HebrewToken)) {
            termAtt.copyBuffer(word.toCharArray(), 0, word.length());

            final Token tkn = stack.get(0);
            if (tkn.isNumeric()) {
                typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric));
            } else {
                typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew));
            }

            applyLowercaseFilter();

            stack.clear();
            return true;
        }

        // If we arrived here, we hit a Hebrew word
        typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));
        // TODO: typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Acronym));

		// Do some filtering if requested...
		if (lemmaFilter != null && lemmaFilter.filterCollection(word, stack, filterCache) != null) {
			stack.clear();
			stack.addAll(filterCache);
		}

		// OOV case - store the word as-is, and also output a suffixed version of it
		if (stack.isEmpty()) {
            termAtt.copyBuffer(word.toCharArray(), 0, word.length());

            if (keepOriginalWord) {
                keywordAtt.setKeyword(true);
            }

            if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Mixed) > 0) {
                typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Mixed));
                applyLowercaseFilter();
                return true;
            }
            if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) > 0) {
                applyLowercaseFilter();
                return true;
            }

            if (keepOriginalWord)
                stack.add(new HebrewToken(word, (byte)0, 0, word, 1.0f));

			return true;
		}

        // Mark and store the original term to increase precision, while all lemmas
        // will be popped out of the stack and get stored at the next call to IncrementToken.
        if (keepOriginalWord) {
            termAtt.copyBuffer(word.toCharArray(), 0, word.length());
            keywordAtt.setKeyword(true);
            return true;
        }

        // If !keepOriginalWord
        final HebrewToken hebToken = (HebrewToken)stack.get(0);
        if (stack.size() == 1) { // only one lemma was found
            stack.clear();
        } else { // // more than one lemma exist.
            index = 1;
            previousLemmas.add(hebToken.getLemma());
        }
        createHebrewToken(hebToken);

		return true;
	}

    private void applyLowercaseFilter() {
        charUtils.toLowerCase(termAtt.buffer(), 0, termAtt.length());
    }

    protected void createHebrewToken(HebrewToken hebToken) {
        String tokenVal = hebToken.getLemma() == null ? hebToken.getText().substring(hebToken.getPrefixLength()) : hebToken.getLemma();
		termAtt.copyBuffer(tokenVal.toCharArray(), 0, tokenVal.length());
	}

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        int finalOffset = correctOffset(_streamLemmatizer.getEndOffset());
        currentStartOffset = currentEndOffset = finalOffset;
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        stack.clear();
        filterCache.clear();
        previousLemmas.clear();
        index = 0;
        _streamLemmatizer.reset(input);
    }

    @Override
	public void reset() throws IOException {
        super.reset();
		stack.clear();
        filterCache.clear();
        previousLemmas.clear();
		index = 0;
        currentStartOffset = currentEndOffset = 0;
		_streamLemmatizer.reset(input);
	}

    public void setKeepOriginalWord(boolean keepOriginalWord) {
        this.keepOriginalWord = keepOriginalWord;
    }
}
