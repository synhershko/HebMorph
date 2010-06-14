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

using Lucene.Net.Util;
using Lucene.Net.Analysis.Tokenattributes;

namespace Lucene.Net.Analysis.Hebrew
{
    /// <summary>
    /// Tokenizes a given stream using HebMorph's Tokenizer, removes prefixes where possible, and tags Tokens
    /// with appropriate types where possible
    /// </summary>
    public class HebrewTokenizer : Tokenizer
    {
        private HebMorph.Lemmatizer hebMorphAnalyzer;
        private HebMorph.Tokenizer hebMorphTokenizer;

        private TermAttribute termAtt;
        private OffsetAttribute offsetAtt;
        //private PositionIncrementAttribute posIncrAtt;
        private TypeAttribute typeAtt;

        #region Constructors
        public HebrewTokenizer(System.IO.TextReader input, HebMorph.Lemmatizer _hebMorphAnalyzer)
            //: base(input) <- converts to CharStream, and causes issues due to a call to ReadToEnd in ctor
        {
            Init(input, _hebMorphAnalyzer);
        }

        private void Init(System.IO.TextReader input, HebMorph.Lemmatizer _hebMorphAnalyzer)
        {
            termAtt = (TermAttribute)AddAttribute(typeof(TermAttribute));
            offsetAtt = (OffsetAttribute)AddAttribute(typeof(OffsetAttribute));
            //posIncrAtt = (PositionIncrementAttribute)AddAttribute(typeof(PositionIncrementAttribute));
            typeAtt = (TypeAttribute)AddAttribute(typeof(TypeAttribute));
            this.hebMorphTokenizer = new HebMorph.Tokenizer(input);
            this.hebMorphAnalyzer = _hebMorphAnalyzer;
        }
        #endregion

        #region Token types
        public enum TOKEN_TYPES : int
        {
            Hebrew = 0,
            NonHebrew = 1,
            Numeric = 2,
            Construct = 3,
            Acronym = 4,
        }

        public static readonly string[] TOKEN_TYPE_SIGNATURES = new string[]
        {
            "<HEBREW>",
            "<NON_HEBREW>",
            "<NUM>",
            "<CONSTRUCT>",
            "<ACRONYM>",
            null
        };

        public static string TokenTypeSignature(TOKEN_TYPES tokenType)
        {
            return TOKEN_TYPE_SIGNATURES[(int)tokenType];
        }
        #endregion

        public override bool IncrementToken()
        {
            ClearAttributes();
            int start = hebMorphTokenizer.Offset;

            string nextToken;
            HebMorph.Tokenizer.TokenType tokenType;

            // Used to loop over certain noise cases
            while (true)
            {
                tokenType = hebMorphTokenizer.NextToken(out nextToken);
                if (tokenType == 0)
                    return false; // EOS

                // Ignore "words" which are actually only prefixes in a single word.
                // This first case is easy to spot, since the prefix and the following word will be
                // separated by a dash marked as a construct (סמיכות) by the Tokenizer
                if ((tokenType & HebMorph.Tokenizer.TokenType.Construct) > 0)
                {
                    if (hebMorphAnalyzer.IsLegalPrefix(nextToken))
                        continue;
                }

                // This second case is a bit more complex. We take a risk of splitting a valid acronym or
                // abbrevated word into two, so we send it to an external function to analyze the word, and
                // get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
                if ((tokenType & HebMorph.Tokenizer.TokenType.Acronym) > 0)
                {
                    nextToken = hebMorphAnalyzer.TryStrippingPrefix(nextToken);

                    // Redect acronym, in case it was a false positive
                    if (nextToken.IndexOf('"') == -1)
                        tokenType |= ~HebMorph.Tokenizer.TokenType.Acronym;
                }

                // TODO: Perhaps by easily identifying the prefixes above we can also rule out some of the
                // stem ambiguities retreived later...

                // TODO: Add support for MaxTokenLength like in StandardTokenizer?

                break;
            }

            // Record the term string
            if (termAtt.TermLength() < nextToken.Length)
                termAtt.SetTermBuffer(nextToken);
            else // Perform a copy to save on memory operations
            {
                char[] buf = termAtt.TermBuffer();
                nextToken.CopyTo(0, buf, 0, nextToken.Length);
            }
            termAtt.SetTermLength(nextToken.Length);

            offsetAtt.SetOffset(CorrectOffset(start), CorrectOffset(start + nextToken.Length));

            if ((tokenType & HebMorph.Tokenizer.TokenType.Hebrew) > 0)
            {
                if ((tokenType & HebMorph.Tokenizer.TokenType.Acronym) > 0)
                    typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Acronym));
                if ((tokenType & HebMorph.Tokenizer.TokenType.Construct) > 0)
                    typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Construct));
                else
                    typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Hebrew));
            }
            else if ((tokenType & HebMorph.Tokenizer.TokenType.Numeric) > 0)
            {
                typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Numeric));
            }
            else
                typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.NonHebrew));

            return true;
        }

        public override void End()
        {
            // set final offset
            int finalOffset = CorrectOffset(hebMorphTokenizer.Offset);
            offsetAtt.SetOffset(finalOffset, finalOffset);
        }

        public override void Reset(System.IO.TextReader input)
        {
            base.Reset(input);
            hebMorphTokenizer.Reset(input);
        }
    }
}
