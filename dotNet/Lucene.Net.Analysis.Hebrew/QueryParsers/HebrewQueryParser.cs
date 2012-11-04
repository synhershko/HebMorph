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

namespace Lucene.Net.QueryParsers.Hebrew
{
    using System;
    using Lucene.Net.Search;
    using Lucene.Net.Analysis;

    public class HebrewQueryParser : QueryParser
    {
        protected static float SuffixedTermBoost = 2.0f;

        public HebrewQueryParser(Lucene.Net.Util.Version matchVersion, string f, Analyzer a)
            : base(matchVersion, f, a)
        {
        }

        public override Query Parse(string query)
        {
            string q = string.Empty;

            for (int i = 0; i < query.Length; i++)
            {
                if (query[i] == '"' && i + 1 < query.Length && !char.IsWhiteSpace(query[i + 1]))
                    if (i > 0 && !char.IsWhiteSpace(query[i - 1]))
                        q += '\\';
                q += query[i];
            }

            return base.Parse(q);
        }
    }
}
