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
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.CharacterUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StreamLemmasFilter extends Tokenizer
{
	private StreamLemmatizer _streamLemmatizer;

	private final TermAttribute termAtt = addAttribute(TermAttribute.class);;
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);;
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);;
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);;

    private final CharacterUtils charUtils;

	private boolean alwaysSaveMarkedOriginal;
	private final LemmaFilterBase lemmaFilter;

	private final List<Token> stack = new ArrayList<Token>();
	private final List<Token> filterCache = new ArrayList<Token>();
	private int index = 0;
    private final Set<String> previousLemmas = new HashSet<String>();

	public StreamLemmasFilter(final Reader input, final StreamLemmatizer _lemmatizer)
	{
		this(input, _lemmatizer, null);
	}

	public StreamLemmasFilter(final Reader input, final StreamLemmatizer _lemmatizer, final LemmaFilterBase _lemmaFilter)
	{
        super(input);

        _streamLemmatizer = _lemmatizer;
        _streamLemmatizer.setStream(input);
        lemmaFilter = _lemmaFilter;

        charUtils = CharacterUtils.getInstance(Version.LUCENE_36);
	}

    public void setSuffixForExactMatch(Character c){
        _streamLemmatizer.setSuffixForExactMatch(c);
    }

	@Override
	public final boolean incrementToken() throws IOException {
		// Index all unique lemmas at the same position
		while (index < stack.size()) {
			final HebrewToken res = (HebrewToken)((stack.get(index) instanceof HebrewToken) ? stack.get(index) : null);
			index++;

            if ((res == null) || !previousLemmas.add(res.getLemma())) // Skip multiple lemmas (we will merge morph properties later)
				continue;

			if (createHebrewToken(res))
				return true;
		}

		// Reset state
		clearAttributes();
		index = 0;
		stack.clear();
        previousLemmas.clear();

		// Lemmatize next word in stream. The HebMorph lemmatizer will always return a token, unless
		// an unrecognized Hebrew word is hit, then an empty tokens array will be returned.
		final Reference<String> tempRefObject = new Reference<String>("");
		boolean tempVar = _streamLemmatizer.getLemmatizeNextToken(tempRefObject, stack) == 0;
        final String word = tempRefObject.ref;
		if (tempVar)
			return false; // EOS

		// Store the location of the word in the original stream
		offsetAtt.setOffset(correctOffset(_streamLemmatizer.getStartOffset()), correctOffset(_streamLemmatizer.getEndOffset()));

		// A non-Hebrew word
		if ((stack.size() == 1) && !(stack.get(0) instanceof HebrewToken)) {
            termAtt.setTermBuffer(word);

			Token tkn = stack.get(0);
			if (tkn.isNumeric()) {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric));
			} else {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew));

                // TODO: make this customizable
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
		// Do some filtering if requested...
		if ((lemmaFilter != null) && (lemmaFilter.filterCollection(stack, filterCache) != null)) {
			stack.clear();
			stack.addAll(filterCache);
		}

		// OOV case -- for now store word as-is and return true
		if (stack.isEmpty()) {
			// TODO: To allow for more advanced uses, fill stack with processed tokens and
			// SetPositionIncrement(0)

            termAtt.setTermBuffer(word + "$");
			typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));
			return true;
		}

		// If only one lemma was returned for this word
		if (stack.size() == 1) {
			HebrewToken hebToken = (HebrewToken)((stack.get(0) instanceof HebrewToken) ? stack.get(0) : null);

			// Index the lemma alone if it exactly matches the word minus prefixes
			if (!alwaysSaveMarkedOriginal && hebToken.getLemma().equals(word.substring(hebToken.getPrefixLength()))) {
				createHebrewToken(hebToken);
				posIncrAtt.setPositionIncrement(1);
				stack.clear();
				return true;
			} else { // Otherwise, index the lemma plus the original word marked with a unique flag to increase precision
				// DILEMMA: Does indexing word.Substring(hebToken.PrefixLength) + "$" make more or less sense?
				// For now this is kept the way it is below to support duality of SimpleAnalyzer and MorphAnalyzer
                termAtt.setTermBuffer(word + "$");
			}
		}

		// More than one lemma exist. Mark and store the original term to increase precision, while all
		// lemmas will be popped out of the stack and get stored at the next call to IncrementToken.
		else {
            termAtt.setTermBuffer(word + "$");
		}

        typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));

		return true;
	}


	protected boolean createHebrewToken(HebrewToken hebToken) {
        termAtt.setTermBuffer(hebToken.getLemma() == null ? hebToken.getText().substring(hebToken.getPrefixLength()) : hebToken.getLemma());
		posIncrAtt.setPositionIncrement(0);

		// TODO: typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Acronym));
        typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));

//
//             * Morph payload
//             *
//            byte[] data = new byte[1];
//            data[0] = (byte)morphResult.Mask; // TODO: Set bits selectively
//            Payload payload = new Payload(data);
//            payAtt.SetPayload(payload);
//

		return true;
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
