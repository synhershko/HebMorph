/***************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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
 ***************************************************************************/

using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph
{
    public class StreamLemmatizer : Lemmatizer
    {
        private Tokenizer _tokenizer;

        public StreamLemmatizer()
        {
        }

        public StreamLemmatizer(System.IO.TextReader input)
        {
            SetStream(input);
        }

        public void SetStream(System.IO.TextReader input)
        {
            _tokenizer = new Tokenizer(input);
        }

        public int LemmatizeNextToken(out string nextToken, IList<Token> retTokens)
        {
            retTokens.Clear();

            int currentPos = 0;
            Tokenizer.TokenType tokenType;

            // Used to loop over certain noise cases
            while (true)
            {
                tokenType = _tokenizer.NextToken(out nextToken);
                if (tokenType == 0)
                    return 0; // EOS

                ++currentPos;

                if ((tokenType & Tokenizer.TokenType.Hebrew) > 0)
                {
                    // Right now we are blindly removing all Niqqud characters. Later we will try and make some
                    // use of Niqqud for some cases. We do this before everything else to allow for a correct
                    // identification of prefixes.
                    nextToken = RemoveNiqqud(nextToken);

                    // Ignore "words" which are actually only prefixes in a single word.
                    // This first case is easy to spot, since the prefix and the following word will be
                    // separated by a dash marked as a construct (סמיכות) by the Tokenizer
                    if ((tokenType & Tokenizer.TokenType.Construct) > 0)
                    {
                        if (IsLegalPrefix(nextToken))
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
                        nextToken = TryStrippingPrefix(nextToken);

                        // Re-detect acronym, in case it was a false positive
                        if (nextToken.IndexOf('"') == -1)
                            tokenType |= ~Tokenizer.TokenType.Acronym;
                    }

                    // TODO: Perhaps by easily identifying the prefixes above we can also rule out some of the
                    // stem ambiguities retreived later...

                    IList<HebrewToken> lemmas = Lemmatize(nextToken);

                    if (lemmas != null && lemmas.Count > 0)
                    {
                        // TODO: Filter Stop Words based on morphological data
                        // TODO: Check for worthy lemmas, if there are none then perform tolerant lookup and check again...
                        if ((tokenType & Tokenizer.TokenType.Construct) > 0)
                        {
                            // TODO: Test for (lemma.Mask & DMask.D_OSMICHUT) > 0
                        }

                        foreach (Token t in lemmas) // temp catch-all
                            retTokens.Add(t);
                    }

                    if (retTokens.Count == 0 && (tokenType & Tokenizer.TokenType.Acronym) > 0)
                    {
                        // TODO: Perform Gimatria test
                        // TODO: Treat an acronym as a noun and strip affixes accordingly?
                        retTokens.Add(new HebrewToken(nextToken, 0, HebMorph.HSpell.DMask.D_ACRONYM, nextToken, 1.0f));
                    }
                    else if (retTokens.Count == 0)
                    {
                        lemmas = LemmatizeTolerant(nextToken);
                        if (lemmas != null && lemmas.Count > 0)
                        {
                            // TODO: Keep only worthy lemmas, based on characteristics and score / confidence

                            if ((tokenType & Tokenizer.TokenType.Construct) > 0)
                            {
                                // TODO: Test for (lemma.Mask & DMask.D_OSMICHUT) > 0
                            }

                            foreach (Token t in lemmas) // temp catch-all
                                retTokens.Add(t);
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
                    retTokens.Add(new Token(nextToken, true));
                else
                    retTokens.Add(new Token(nextToken));

                break;
            }

            return currentPos;
        }
    }
}
