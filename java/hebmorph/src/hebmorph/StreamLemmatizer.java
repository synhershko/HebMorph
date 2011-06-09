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
package hebmorph;

import hebmorph.datastructures.DictRadix;
import hebmorph.hspell.Constants.DMask;
import java.io.IOException;
import java.io.Reader;
import java.util.List;


public class StreamLemmatizer extends Lemmatizer
{
	private Tokenizer _tokenizer;

	public StreamLemmatizer(DictRadix<MorphData> dict, boolean allowHeHasheela)
	{
		super(dict, allowHeHasheela);
	}

	public StreamLemmatizer(Reader input, DictRadix<MorphData> dict, boolean allowHeHasheela)
	{
		super(dict, allowHeHasheela);
		_tokenizer = new Tokenizer(input);
	}

	public void setStream(Reader input)
	{
		if (_tokenizer == null)
		{
			_tokenizer = new Tokenizer(input);
		}
		else
		{
			_tokenizer.reset(input);
		}
	}

	private int _startOffset, _endOffset;
	public int getStartOffset()
	{
		return _startOffset;
	}
	public int getEndOffset()
	{
		return _endOffset;
	}

	private boolean tolerateWhenLemmatizingStream = true;

	public int getLemmatizeNextToken(Reference<String> nextToken, List<Token> retTokens) throws IOException
	{
		retTokens.clear();

		int currentPos = 0;
		int tokenType;

		// Used to loop over certain noise cases
		while (true)
		{
			tokenType = _tokenizer.nextToken(nextToken);
			if (tokenType == 0)
			{
				return 0; // EOS
			}

			_startOffset = _tokenizer.getOffset();
			_endOffset = _tokenizer.getOffset() + _tokenizer.getLengthInSource();

			++currentPos;

			if ((tokenType & Tokenizer.TokenType.Hebrew) > 0)
			{
				// Right now we are blindly removing all Niqqud characters. Later we will try and make some
				// use of Niqqud for some cases. We do this before everything else to allow for a correct
				// identification of prefixes.
				nextToken.ref = removeNiqqud(nextToken.ref);

				// Ignore "words" which are actually only prefixes in a single word.
				// This first case is easy to spot, since the prefix and the following word will be
				// separated by a dash marked as a construct (סמיכות) by the Tokenizer
				if (((tokenType & Tokenizer.TokenType.Construct) > 0)
                    || ((tokenType & Tokenizer.TokenType.Acronym) > 0))
				{
					if (isLegalPrefix(nextToken.ref))
					{
						--currentPos; // this should be treated as a word prefix
						continue;
					}
				}

				// This second case is a bit more complex. We take a risk of splitting a valid acronym or
				// abbrevated word into two, so we send it to an external function to analyze the word, and
				// get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
				if ((tokenType & Tokenizer.TokenType.Acronym) > 0)
				{
					nextToken.ref = tryStrippingPrefix(nextToken.ref);

					// Re-detect acronym, in case it was a false positive
					if (nextToken.ref.indexOf('"') == -1)
					{
						tokenType &= ~Tokenizer.TokenType.Acronym;
					}
				}

				// TODO: Perhaps by easily identifying the prefixes above we can also rule out some of the
				// stem ambiguities retreived later...

				List<HebrewToken> lemmas = lemmatize(nextToken.ref);

				if ((lemmas != null) && (lemmas.size() > 0))
				{
					// TODO: Filter Stop Words based on morphological data (hspell 'x' identification)
					// TODO: Check for worthy lemmas, if there are none then perform tolerant lookup and check again...
					if ((tokenType & Tokenizer.TokenType.Construct) > 0)
					{
						// TODO: Test for (lemma.Mask & DMask.D_OSMICHUT) > 0
					}

					for (Token t : lemmas) // temp catch-all
					{
						retTokens.add(t);
					}
				}

				if (retTokens.isEmpty() && ((tokenType & Tokenizer.TokenType.Acronym) > 0))
				{
					// TODO: Perform Gimatria test
					// TODO: Treat an acronym as a noun and strip affixes accordingly?
					retTokens.add(new HebrewToken(nextToken.ref, 0, DMask.D_ACRONYM, nextToken.ref, 1.0f));
				}
				else if (tolerateWhenLemmatizingStream && retTokens.isEmpty())
				{
					lemmas = lemmatizeTolerant(nextToken.ref);
					if ((lemmas != null) && (lemmas.size() > 0))
					{
						// TODO: Keep only worthy lemmas, based on characteristics and score / confidence

						if ((tokenType & Tokenizer.TokenType.Construct) > 0)
						{
							// TODO: Test for (lemma.Mask & DMask.D_OSMICHUT) > 0
						}

						for (Token t : lemmas) // temp catch-all
						{
							retTokens.add(t);
						}
					}
					else // Word unknown to hspell - OOV case
					{
						// TODO: Right now we store the word as-is. Perhaps we can assume this is a Noun or a name,
						// and try removing prefixes and suffixes based on that?
						//retTokens.Add(new HebrewToken(nextToken, 0, 0, null, 1.0f));
					}
				}
			}
			else if ((tokenType & Tokenizer.TokenType.Numeric) > 0)
			{
				retTokens.add(new Token(nextToken.ref, true));
			}
			else
			{
				retTokens.add(new Token(nextToken.ref));
			}

			break;
		}

		return currentPos;
	}
}