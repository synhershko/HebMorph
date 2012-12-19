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

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StreamLemmasFilter extends Tokenizer
{
	private StreamLemmatizer _streamLemmatizer;

	private TermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private PositionIncrementAttribute posIncrAtt;
	private TypeAttribute typeAtt;
	//protected PayloadAttribute payAtt;

	private boolean alwaysSaveMarkedOriginal;
	private LemmaFilterBase lemmaFilter = null;

	private final List<Token> stack = new ArrayList<Token>();
	private final List<Token> filterCache = new ArrayList<Token>();
	private int index = 0;
    private final Set<String> previousLemmas = new HashSet<String>();

	public StreamLemmasFilter(Reader input, StreamLemmatizer _lemmatizer)
	{
        super(input);
		init(input, _lemmatizer, null, false);
	}

	public StreamLemmasFilter(Reader input, StreamLemmatizer _lemmatizer, boolean alwaysSaveMarkedOriginal)
	{
        super(input);
		init(input, _lemmatizer, null, alwaysSaveMarkedOriginal);
	}

	public StreamLemmasFilter(Reader input, StreamLemmatizer _lemmatizer, LemmaFilterBase _lemmaFilter, boolean alwaysSaveMarkedOriginal)
	{
        super(input);
		init(input, _lemmatizer, _lemmaFilter, alwaysSaveMarkedOriginal);
	}

	public StreamLemmasFilter(Reader input, StreamLemmatizer _lemmatizer, LemmaFilterBase _lemmaFilter)
	{
        super(input);
		init(input, _lemmatizer, _lemmaFilter, false);
	}

	private void init(Reader input, StreamLemmatizer _lemmatizer, LemmaFilterBase _lemmaFilter, boolean alwaysSaveMarkedOriginal)
	{
		termAtt = addAttribute(TermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);

		_streamLemmatizer = _lemmatizer;
		_streamLemmatizer.setStream(input);
		this.alwaysSaveMarkedOriginal = alwaysSaveMarkedOriginal;
		lemmaFilter = _lemmaFilter;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		// Index all unique lemmas at the same position
		while (index < stack.size()) {
			HebrewToken res = (HebrewToken)((stack.get(index) instanceof HebrewToken) ? stack.get(index) : null);
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
			setTermText(word);

			Token tkn = stack.get(0);
			if (tkn.isNumeric()) {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric));
			} else {
				typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew));

				// Applying LowerCaseFilter for Non-Hebrew terms
				char[] buffer = termAtt.termBuffer();
				int length = termAtt.termLength();
				for (int i = 0; i < length; i++)
				{
					buffer[i] = Character.toLowerCase(buffer[i]);
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

			setTermText(word + "$");
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
				setTermText(word + "$");
			}
		}

		// More than one lemma exist. Mark and store the original term to increase precision, while all
		// lemmas will be popped out of the stack and get stored at the next call to IncrementToken.
		else {
			setTermText(word + "$");
		}

        typeAtt.setType(HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew));

		return true;
	}


	protected boolean createHebrewToken(HebrewToken hebToken) {
		setTermText(hebToken.getLemma() == null ? hebToken.getText().substring(hebToken.getPrefixLength()) : hebToken.getLemma());
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


	private void setTermText(String token) {
		// Record the term string
		if (termAtt.termLength() < token.length()) {
			termAtt.setTermBuffer(token);
		} else { // Perform a copy to save on memory operations
	        char[] chars = token.toCharArray();
            termAtt.setTermBuffer(chars,0,chars.length);
            //char[] buf = termAtt.termBuffer();
			//token.CopyTo(0, buf, 0, token.length());
		}
		termAtt.setTermLength(token.length());
	}

    
	@Override
	public void reset(Reader input) throws IOException {
		super.reset(input);
		stack.clear();
		index = 0;
		_streamLemmatizer.setStream(input);
	}
}
