using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph.Tests
{
    [TestClass()]
    public class TokenizerTest
    {
        private Tokenizer GetTokenizer(string input)
        {
            return new Tokenizer(new System.IO.StringReader(input));
        }

        [TestMethod()]
        public void IncrementsOffsetCorrectly()
        {
            int[] expectedOffsets = { 0, 5, 10, 15 };
            int curPos = 0;

            string token = string.Empty;
            Tokenizer t = GetTokenizer("test test test test");
            while (true)
            {
                HebMorph.Tokenizer.TokenType token_type = t.NextToken(out token);
                if (token_type == 0)
                    break;

                Assert.AreEqual<int>(expectedOffsets[curPos++], t.Offset);
                Assert.AreEqual<int>(4, t.LengthInSource);
            }
        }

        [TestMethod()]
        public void IncrementsOffsetCorrectlyAlsoWhenBuffered()
        {
            string token = string.Empty;

            string input =string.Empty;
            for (int repeat = 0; repeat < 4000; repeat++)
            {
                input += "test test test test ";
            }

            Tokenizer t = GetTokenizer(input);
            int previousOffest = -5;
            while (true)
            {
                HebMorph.Tokenizer.TokenType token_type = t.NextToken(out token);
                if (token_type == 0)
                    break;

                Assert.AreEqual<int>(previousOffest, t.Offset - 5);
                Assert.AreEqual<int>(4, t.LengthInSource);
                previousOffest = t.Offset;
            }
        }

        [TestMethod()]
        public void UnifiesGershayimCorrectly()
        {
            string test;

            Tokenizer t = GetTokenizer("צה''ל");
            t.NextToken(out test);
            Assert.AreEqual<string>("צה\"ל", test);

            t = GetTokenizer("צה\u05F3\u05F3ל");
            t.NextToken(out test);
            Assert.AreEqual<string>("צה\"ל", test);
        }

        [TestMethod()]
        public void DiscardsSurroundingGershayim()
        {
            string test;

            Tokenizer t = GetTokenizer(@"""צבא""");
            t.NextToken(out test);
            Assert.AreEqual<string>("צבא", test);
            Assert.AreEqual<int>(3, t.LengthInSource);
            Assert.AreEqual<int>(1, t.Offset);
        }
    }
}
