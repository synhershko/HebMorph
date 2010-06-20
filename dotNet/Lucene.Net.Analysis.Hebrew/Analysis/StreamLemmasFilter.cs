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
using Lucene.Net.Analysis.Tokenattributes;

namespace Lucene.Net.Analysis.Hebrew
{
    public class StreamLemmasFilter : Tokenizer
    {
        private HebMorph.StreamLemmatizer _streamLemmatizer;

        private TermAttribute termAtt;
        private OffsetAttribute offsetAtt;
        private PositionIncrementAttribute posIncrAtt;
        protected TypeAttribute typeAtt;
        //protected PayloadAttribute payAtt;

        private State current = null;
        private IList<HebMorph.Token> stack = new List<HebMorph.Token>();
        private int index = 0;
        private string previousLemma = null;

        #region Constructors
        public StreamLemmasFilter(System.IO.TextReader input, HebMorph.StreamLemmatizer _lemmatizer)
            //: base(input) <- converts to CharStream, and causes issues due to a call to ReadToEnd in ctor
        {
            Init(input, _lemmatizer);
        }

        private void Init(System.IO.TextReader input, HebMorph.StreamLemmatizer _lemmatizer)
        {
            termAtt = (TermAttribute)AddAttribute(typeof(TermAttribute));
            offsetAtt = (OffsetAttribute)AddAttribute(typeof(OffsetAttribute));
            posIncrAtt = (PositionIncrementAttribute)AddAttribute(typeof(PositionIncrementAttribute));
            typeAtt = (TypeAttribute)AddAttribute(typeof(TypeAttribute));
            //payAtt = (PayloadAttribute)AddAttribute(typeof(PayloadAttribute));

            this._streamLemmatizer = _lemmatizer;
            this._streamLemmatizer.SetStream(input);
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
            while (index < stack.Count) // pop from stack if any
            {
                HebMorph.HebrewToken res = stack[index++] as HebMorph.HebrewToken;

                if (res == null || previousLemma == res.Lemma) // Skip multiple lemmas (we will merge morph properties later)
                    continue;

                if (CreateHebrewToken(res, current))
                    return true;
            }

            ClearAttributes();

            index = 0;
            stack.Clear();
            current = null;

            string word = string.Empty;
            if (_streamLemmatizer.LemmatizeNextToken(out word, stack) == 0)
                return false; // EOS

            offsetAtt.SetOffset(_streamLemmatizer.StartOffset, _streamLemmatizer.EndOffset);

            if (stack.Count == 0)
            {
                // OOV -- for now store word as-is and return true
                SetTermText(word);
                typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Hebrew));
                return true;
            }

            if (stack.Count == 1 && !(stack[0] is HebMorph.HebrewToken))
            {
                SetTermText(word);

                HebMorph.Token tkn = stack[0];
                if (tkn.IsNumeric)
                    typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Numeric));
                else
                {
                    typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.NonHebrew));
                    
                    // Applying LowerCaseFilter for Non-Hebrew terms
                    char[] buffer = termAtt.TermBuffer();
                    int length = termAtt.TermLength();
                    for (int i = 0; i < length; i++)
                        buffer[i] = System.Char.ToLower(buffer[i]);
                }

                stack.Clear();
                return true;
            }

            HebMorph.HebrewToken hebToken = stack[0] as HebMorph.HebrewToken;
            CreateHebrewToken(hebToken);
            if (stack.Count == 1) // We have only have one result, no need to push it to the stack
            {
                if (hebToken.Lemma.Equals(word)) // ... but only if the actual word matches the lemma
                {
                    posIncrAtt.SetPositionIncrement(1);
                    stack.Clear();
                    return true;
                }
            }

            // Mark the original term to increase precision. This will get indexed in the next iteration
            hebToken.Lemma = word + "$";
            current = CaptureState();

            return true;
        }

        private void SetTermText(string token)
        {
            // Record the term string
            if (termAtt.TermLength() < token.Length)
                termAtt.SetTermBuffer(token);
            else // Perform a copy to save on memory operations
            {
                char[] buf = termAtt.TermBuffer();
                token.CopyTo(0, buf, 0, token.Length);
            }
            termAtt.SetTermLength(token.Length);
        }

        protected bool CreateHebrewToken(HebMorph.HebrewToken hebToken, State current)
        {
            CreateHebrewToken(hebToken);
            return true;
        }

        protected virtual bool CreateHebrewToken(HebMorph.HebrewToken hebToken)
        {
            SetTermText(hebToken.Lemma == null ? hebToken.Text.Substring(hebToken.PrefixLength) : hebToken.Lemma);
            posIncrAtt.SetPositionIncrement(0);

            // TODO: typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Acronym));
            typeAtt.SetType(TokenTypeSignature(TOKEN_TYPES.Hebrew));

            /*
             * Morph payload
             * 
            byte[] data = new byte[1];
            data[0] = (byte)morphResult.Mask; // TODO: Set bits selectively
            Payload payload = new Payload(data);
            payAtt.SetPayload(payload);
            */

            return true;
        }

        public override void Reset(System.IO.TextReader input)
        {
            base.Reset(input);
            stack.Clear();
            index = 0;
            current = null;
            _streamLemmatizer.SetStream(input);
        }
    }
}
