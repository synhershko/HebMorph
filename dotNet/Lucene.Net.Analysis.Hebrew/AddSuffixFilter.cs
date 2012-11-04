/***************************************************************************
 * HebMorph - making Hebrew properly searchable
 * 
 *   Copyright (C) 2010-2012                                               
 *      Itamar Syn-Hershko <itamar at code972 dot com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

using System;
using System.Collections.Generic;
using System.Text;

using Lucene.Net.Analysis.Tokenattributes;

namespace Lucene.Net.Analysis
{
    public class AddSuffixFilter : TokenFilter
    {
        private readonly ITermAttribute termAtt;
        private readonly ITypeAttribute typeAtt;

        public Dictionary<string, char[]> suffixByTokenType = null;

        public AddSuffixFilter(TokenStream input, Dictionary<string, char[]> _suffixByTokenType)
            : base(input)
        {
			termAtt = AddAttribute <ITermAttribute>();
			typeAtt = AddAttribute <ITypeAttribute>();
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
            if (!suffixByTokenType.TryGetValue(typeAtt.Type, out suffix))
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
