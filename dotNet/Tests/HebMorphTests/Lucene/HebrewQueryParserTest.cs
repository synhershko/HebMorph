
using Xunit;
using global::Lucene.Net.QueryParsers;
using global::Lucene.Net.QueryParsers.Hebrew;
using LuceneUtil = global::Lucene.Net.Util;

namespace HebMorph.Lucene.Tests
{
    public class HebrewQueryParserTest
    {
        [Fact]
        public void ParsesAcronymsCorrectly()
        {
            QueryParser qp = new HebrewQueryParser(LuceneUtil.Version.LUCENE_30, "f", new global::Lucene.Net.Analysis.Hebrew.SimpleAnalyzer());
            qp.Parse(@"צה""ל");
            qp.Parse(@"""צהל""");
            qp.Parse(@"כל הכבוד לצה""ל");
            qp.Parse(@"""כל הכבוד לצה""ל""");
            qp.Parse(@"""כל הכבוד"" לצה""ל");

            qp.Parse(@"מנכ""לית");

            try
            {
                qp.Parse(@"צה""""ל");
                qp.Parse(@"""צה""ל");
                Assert.True(false, "Expected exception was not thrown");
            }
            catch(ParseException) { }
        }
    }
}
