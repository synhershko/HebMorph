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

import com.code972.hebmorph.HebrewToken;
import com.code972.hebmorph.Reference;
import com.code972.hebmorph.StreamLemmatizer;
import com.code972.hebmorph.Token;
import com.code972.hebmorph.lemmafilters.LemmaFilterBase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StreamLemmasFilter extends Tokenizer
{
	private final StreamLemmatizer _streamLemmatizer;

	private final TermAttribute termAtt = addAttribute(TermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final CharacterUtils charUtils;

	private boolean alwaysSaveMarkedOriginal;
	private final LemmaFilterBase lemmaFilter;

	private final List<Token> stack = new ArrayList<Token>();
	private final List<Token> filterCache = new ArrayList<Token>();
	private int index = 0;
    private final Set<String> previousLemmas = new HashSet<String>();

	public StreamLemmasFilter(final Reader input, final StreamLemmatizer _lemmatizer) {
		this(input, _lemmatizer, null);
	}

	public StreamLemmasFilter(final Reader input, final StreamLemmatizer _lemmatizer, final LemmaFilterBase _lemmaFilter) {
        super(input);

        _streamLemmatizer = _lemmatizer;
        _streamLemmatizer.setStream(input);
        lemmaFilter = _lemmaFilter;

        charUtils = CharacterUtils.getInstance(Version.LUCENE_36);
	}

    public void setSuffixForExactMatch(Character c){
        _streamLemmatizer.setSuffixForExactMatch(c);
    }

    private final Reference<String> tempRefObject = new Reference<>("");

	@Override
	public final boolean incrementToken() throws IOException {
        keywordAtt.setKeyword(false); // since this is also the Tokenizer, this only manages internal state

		// Index all unique lemmas at the same position
		while (index < stack.size()) {
			final HebrewToken res = (HebrewToken)((stack.get(index) instanceof HebrewToken) ? stack.get(index) : null);
			index++;

            if ((res == null) || !previousLemmas.add(res.getLemma())) // Skip multiple lemmas (we will merge morph properties later)
				continue;

			createHebrewToken(res);
            posIncrAtt.setPositionIncrement(0);
            return true;
		}

		// Reset state
		clearAttributes();
		index = 0;
		stack.clear();
        previousLemmas.clear();

		// Lemmatize next word in stream. The HebMorph lemmatizer will always return a token, unless
		// an unrecognized Hebrew word is hit, then an empty tokens array will be returned.
		final int tokenType = _streamLemmatizer.getLemmatizeNextToken(tempRefObject, stack);
        if (tokenType == 0) // EOS
			return false;

		// Store the location of the word in the original stream
		offsetAtt.setOffset(correctOffset(_streamLemmatizer.getStartOffset()), correctOffset(_streamLemmatizer.getEndOffset()));

        final String word = tempRefObject.ref;

        // TODO don't lemmatize common words as well
        if ((tokenType & com.code972.hebmorph.Tokenizer.TokenType.Exact) > 0) {
            keywordAtt.setKeyword(true);
        }

		// A non-Hebrew word
		if (stack.size() == 1 && !(stack.get(0) instanceof HebrewToken)) {
            termAtt.setTermBuffer(word);

			final Token tkn = stack.get(0);
			if (tkn.isNumeric()) {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric));
			} else {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew));

				// Applying LowerCaseFilter for Non-Hebrew terms
				char[] buffer = termAtt.termBuffer();
				int length = termAtt.termLength();
                for (int i = 0; i < length;) {
                    i += Character.toChars(
                            Character.toLowerCase(
                                    charUtils.codePointAt(buffer, i)), buffer, i);
                }
			}

			stack.clear();
			return true;
		}

		// If we arrived here, we hit a Hebrew word
        typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));
        // TODO: typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Acronym));

		// Do some filtering if requested...
		if (lemmaFilter != null && lemmaFilter.filterCollection(stack, filterCache) != null) {
			stack.clear();
			stack.addAll(filterCache);
		}

		// OOV case -- for now store word as-is and return true
		if (stack.isEmpty()) {
            termAtt.setTermBuffer(word);
            keywordAtt.setKeyword(true);
			return true;
		}

		// If only one lemma was returned for this word
		if (stack.size() == 1) {
			final HebrewToken hebToken = (HebrewToken)((stack.get(0) instanceof HebrewToken) ? stack.get(0) : null);

			// Index the lemma alone if it exactly matches the word minus prefixes
			if (!alwaysSaveMarkedOriginal && hebToken.getLemma().equals(word.substring(hebToken.getPrefixLength()))) {
				createHebrewToken(hebToken);
				stack.clear();
				return true;
			} else { // Otherwise, index the lemma plus the original word marked with a unique flag to increase precision
				// DILEMMA: Does indexing word.Substring(hebToken.PrefixLength) + "$" make more or less sense?
				// For now this is kept the way it is below to support duality of SimpleAnalyzer and MorphAnalyzer
                termAtt.setTermBuffer(word);
                keywordAtt.setKeyword(true);
			}
		}

		// More than one lemma exist. Mark and store the original term to increase precision, while all
		// lemmas will be popped out of the stack and get stored at the next call to IncrementToken.
		else {
            termAtt.setTermBuffer(word);
            keywordAtt.setKeyword(true);
		}

		return true;
	}

	protected void createHebrewToken(HebrewToken hebToken) {
        termAtt.setTermBuffer(hebToken.getLemma() == null ? hebToken.getText().substring(hebToken.getPrefixLength()) : hebToken.getLemma());
	}
    
	@Override
	public void reset(Reader input) throws IOException {
		super.reset(input);
		stack.clear();
		index = 0;
		_streamLemmatizer.setStream(input);
	}

    public void setAlwaysSaveMarkedOriginal(boolean alwaysSaveMarkedOriginal) {
        this.alwaysSaveMarkedOriginal = alwaysSaveMarkedOriginal;
    }
}
