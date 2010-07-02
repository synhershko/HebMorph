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

namespace Lucene.Net.Analysis
{
    public class AddSuffixFilter : TokenFilter
    {
        private TermAttribute termAtt;
        private TypeAttribute typeAtt;

        public Dictionary<string, char[]> suffixByTokenType = null;

        public AddSuffixFilter(TokenStream input, Dictionary<string, char[]> _suffixByTokenType)
            : base(input)
        {
            termAtt = (TermAttribute)AddAttribute(typeof(TermAttribute));
            typeAtt = (TypeAttribute)AddAttribute(typeof(TypeAttribute));
            this.suffixByTokenType = _suffixByTokenType;
        }

        public override bool IncrementToken()
        {
            if (!input.IncrementToken())
                // reached EOS -- return null
                return false;

            if (suffixByTokenType == null)
                return true;

            char[] suffix;
            if (!suffixByTokenType.TryGetValue(typeAtt.Type(), out suffix))
                return true;

            char[] buffer = termAtt.TermBuffer();
            int length = termAtt.TermLength();

            if (buffer.Length <= length)
            {
                buffer = termAtt.ResizeTermBuffer(length + suffix.Length);
            }
            
            Array.Copy(suffix, 0, buffer, length, suffix.Length);
            termAtt.SetTermLength(length + suffix.Length);

            return true;
        }
    }
}
