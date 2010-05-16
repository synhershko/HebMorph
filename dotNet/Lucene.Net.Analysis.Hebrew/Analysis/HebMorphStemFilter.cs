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
using Lucene.Net.Index;

namespace Lucene.Net.Analysis.Hebrew
{
    public class HebMorphStemFilter : TokenFilter
    {
        public HebMorphStemFilter(TokenStream _input, HebMorph.Analyzer _hebMorphAnalyzer)
            : base(_input)
        {
            this.hebMorphAnalyzer = _hebMorphAnalyzer;

            termAtt = (TermAttribute)AddAttribute(typeof(TermAttribute));
            posIncrAtt = (PositionIncrementAttribute)AddAttribute(typeof(PositionIncrementAttribute));
            typeAtt = (TypeAttribute)AddAttribute(typeof(TypeAttribute));
            payAtt = (PayloadAttribute)AddAttribute(typeof(PayloadAttribute));
        }

        protected HebMorph.Analyzer hebMorphAnalyzer;

        protected TermAttribute termAtt;
        protected PositionIncrementAttribute posIncrAtt;
        protected TypeAttribute typeAtt;
        protected PayloadAttribute payAtt;

        private State current = null;
        private IList<HebMorph.Result> stack = null;
        private int index = 0;

        public override bool IncrementToken()
        {
            while (stack != null && index < stack.Count) // pop from stack if any
            {
                HebMorph.Result res = stack[index++];
                if (IsRelevant(res) && CreateToken(res, current))
                    return true;
            }

            index = 0;
            stack = null;
            current = null;

            if (!input.IncrementToken()) return false; // EOS; iterator exhausted

            // We only need to intervene for Hebrew tokens
            if (typeAtt.Type().Equals(HebrewTokenizer.TokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew)))
            {
                // Analyze the current term
                IList<HebMorph.Result> morphResults = hebMorphAnalyzer.CheckWordTolerant(termAtt.Term());

                if (morphResults != null && morphResults.Count > 0)
                {
                    if (morphResults.Count == 1) // We have only have one result, no need to push it to the stack
                    {
                        // TODO: Filter this token if it has "x" hspell identification (noise / stop word)
                        CreateToken(morphResults[0]);
                        posIncrAtt.SetPositionIncrement(1);
                        return true;
                    }
                    else
                    {
                        // TODO: Normalize result scores, remove irrelevant, low scored or noise words
                        stack = morphResults; // push onto stack
                        current = CaptureState();
                    }
                }
                else // No morphologic data for this word - OOV case
                {
                    // TODO: Right now we store the word as-is. Perhaps we can assume this is a Noun or a name,
                    // and try removing prefixes and suffixes based on that?
                }
            }
            
            return true;
        }

        protected virtual bool IsRelevant(HebMorph.Result morphResult)
        {
            return (morphResult.Score > 0.7f);
        }

        protected bool CreateToken(HebMorph.Result morphResult, State current)
        {
            CreateToken(morphResult);
            return true;
        }

        protected virtual bool CreateToken(HebMorph.Result morphResult)
        {
            termAtt.SetTermBuffer(morphResult.Stem);
            posIncrAtt.SetPositionIncrement(0);
            
            byte[] data = new byte[1];
            data[0] = (byte)morphResult.Mask; // TODO: Set bits selectively
            Payload payload = new Payload(data);
            payAtt.SetPayload(payload);

            return true;
        }

        public override void Reset()
        {
            base.Reset();
            stack = null;
            index = 0;
            current = null;
        }
    }
}
