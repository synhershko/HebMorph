using System;
using System.Collections.Generic;
using System.Text;

using Lucene.Net.Search;
using Lucene.Net.Analysis;

namespace Lucene.Net.QueryParsers.Hebrew
{
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
