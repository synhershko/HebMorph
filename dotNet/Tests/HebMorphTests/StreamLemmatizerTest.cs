using System.Collections.Generic;
using System.IO;
using Xunit;

namespace HebMorph.Tests
{
    public class StreamLemmatizerTest : TestBase
    {
		[Fact]
        public void IncrementsOffsetCorrectly()
        {
            string input = string.Empty;
            for (int repeat = 0; repeat < 4000; repeat++)
            {
                input += "test test test test ";
            }
            StreamLemmatizer sl = new StreamLemmatizer(new StringReader(input));

            string token = string.Empty;
            List<Token> results = new List<Token>();
            int previousOffest = -5;
            while (sl.LemmatizeNextToken(out token, results) > 0)
            {
                Assert.Equal(previousOffest, sl.StartOffset - 5);
                Assert.Equal(4, sl.EndOffset - sl.StartOffset);
                previousOffest = sl.StartOffset;
            }
        }

        // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
        // provide (a TODO in its own), and lastly based on custom lists.
        // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
    }
}