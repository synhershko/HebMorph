using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace HebMorph.Tests
{
    [TestClass()]
    public class StreamLemmatizerTest
    {
        static string hspellPath;

        [TestInitialize()]
        public void SetUp()
        {
            string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
            int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
            if (loc > -1)
            {
                path = path.Remove(loc + 1);
                hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
            }
        }

        [TestMethod()]
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
                Assert.AreEqual<int>(previousOffest, sl.StartOffset - 5);
                Assert.AreEqual<int>(4, sl.EndOffset - sl.StartOffset);
                previousOffest = sl.StartOffset;
            }
        }

        // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
        // provide (a TODO in its own), and lastly based on custom lists.
        // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
    }
}