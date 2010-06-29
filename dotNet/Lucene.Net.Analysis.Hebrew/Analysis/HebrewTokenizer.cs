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
        private HebMorph.Tokenizer hebMorphTokenizer;
        private HebMorph.DataStructures.DictRadix<int> prefixesTree;

        private TermAttribute termAtt;
        private OffsetAttribute offsetAtt;
        //private PositionIncrementAttribute posIncrAtt;
        private TypeAttribute typeAtt;

        #region Constructors
        public HebrewTokenizer(System.IO.TextReader _input)
            //: base(input) <- converts to CharStream, and causes issues due to a call to ReadToEnd in ctor
        {
            Init(_input, HebMorph.HSpell.LingInfo.BuildPrefixTree(false));
        }

        public HebrewTokenizer(System.IO.TextReader _input, HebMorph.DataStructures.DictRadix<int> _prefixesTree)
            //: base(input) <- converts to CharStream, and causes issues due to a call to ReadToEnd in ctor
        {
            Init(_input, _prefixesTree);
        }

        private void Init(System.IO.TextReader _input, HebMorph.DataStructures.DictRadix<int> _prefixesTree)
        {
            termAtt = (TermAttribute)AddAttribute(typeof(TermAttribute));
            offsetAtt = (OffsetAttribute)AddAttribute(typeof(OffsetAttribute));
            //posIncrAtt = (PositionIncrementAttribute)AddAttribute(typeof(PositionIncrementAttribute));
            typeAtt = (TypeAttribute)AddAttribute(typeof(TypeAttribute));
            this.hebMorphTokenizer = new HebMorph.Tokenizer(_input);
            this.prefixesTree = _prefixesTree;
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
                    if (IsLegalPrefix(nextToken))
                        continue;
                }

                // This second case is a bit more complex. We take a risk of splitting a valid acronym or
                // abbrevated word into two, so we send it to an external function to analyze the word, and
                // get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
                if ((tokenType & HebMorph.Tokenizer.TokenType.Acronym) > 0)
                {
                    nextToken = TryStrippingPrefix(nextToken);

                    // Re-detect acronym, in case it was a false positive
                    if (nextToken.IndexOf('"') == -1)
                        tokenType |= ~HebMorph.Tokenizer.TokenType.Acronym;
                }

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

        public bool IsLegalPrefix(string str)
        {
            if (prefixesTree.Lookup(str) > 0)
                return true;

            return false;
        }

        // See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
        public string TryStrippingPrefix(string word)
        {
            // TODO: Make sure we conform to the academy rules as closely as possible

            int firstQuote = word.IndexOf('"');

            if (firstQuote > -1)
            {
                if (IsLegalPrefix(word.Substring(0, firstQuote)))
                    return word.Substring(firstQuote + 1, word.Length - firstQuote - 1);
            }

            int firstSingleQuote = word.IndexOf('\'');
            if (firstSingleQuote == -1)
                return word;

            if (firstQuote > -1 && firstSingleQuote > firstQuote)
                return word;

            if (IsLegalPrefix(word.Substring(0, firstSingleQuote)))
                return word.Substring(firstSingleQuote + 1, word.Length - firstSingleQuote - 1);

            return word;
        }
    }
}
