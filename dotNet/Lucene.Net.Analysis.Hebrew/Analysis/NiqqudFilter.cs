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

using Lucene.Net.Analysis.Tokenattributes;

namespace Lucene.Net.Analysis.Hebrew
{
    public class NiqqudFilter : TokenFilter
    {
        public NiqqudFilter(TokenStream input)
            : base(input)
        {
			termAtt = AddAttribute<ITermAttribute>();
        }

        private readonly ITermAttribute termAtt;

        public override bool IncrementToken()
        {
            if (!input.IncrementToken())
                // reached EOS -- return null
                return false;

            // TODO: Limit this check to Hebrew Tokens only

            char[] buffer = termAtt.TermBuffer();
            int length = termAtt.TermLength(), j = 0;
            for (int i = 0; i < length; i++)
            {
                if (buffer[i] < 1455 || buffer[i] > 1476) // current position is not a Niqqud character
                    buffer[j++] = buffer[i];
            }
            termAtt.SetTermLength(j);
            return true;
        }
    }
}
