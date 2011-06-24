using System.Collections.Generic;
using System.IO;
using Lucene.Net.Analysis;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace HebMorph.Tests
{
    [TestClass()]
    public class TokenizerTest
    {
        private static Tokenizer GetTokenizer(string input)
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

		[TestMethod]
		public void IncrementsOffsetCorrectlyWithAnotherReader()
		{
			int[] expectedOffsets = { 0, 5, 10, 15 };
			int curPos = 0;

			string token = string.Empty;
			Tokenizer t =
				new Tokenizer(
					new HTMLStripCharFilter(CharReader.Get(new System.IO.StringReader(@"test<a href=""foo"">test</a>test test"))));

			while (true)
			{
				Tokenizer.TokenType token_type = t.NextToken(out token);
				if (token_type == 0)
					break;

				Assert.AreEqual<int>(expectedOffsets[curPos++], t.Offset);
				Assert.AreEqual<int>(4, t.LengthInSource);
			}
		}

		[TestMethod]
		public void IncrementsOffsetCorrectlyWithAnotherReader2()
		{
			const string input = @"test1 <a href=""foo"">testlink</a> test2 test3";

			CharFilter filter = new HTMLStripCharFilter(CharReader.Get(new StringReader(input)));
			Tokenizer t = new Tokenizer(filter);

			string token = string.Empty;
			List<Token> results = new List<Token>();

			t.NextToken(out token);
			Assert.AreEqual(0, filter.CorrectOffset(t.Offset));
			Assert.AreEqual(5, t.LengthInSource);

			t.NextToken(out token);
			Assert.AreEqual(20, filter.CorrectOffset(t.Offset));
			Assert.AreEqual(8, t.LengthInSource);

			t.NextToken(out token);
			Assert.AreEqual(33, filter.CorrectOffset(t.Offset));
			Assert.AreEqual(5, t.LengthInSource);

			t.NextToken(out token);
			Assert.AreEqual(39, filter.CorrectOffset(t.Offset));
			Assert.AreEqual(5, t.LengthInSource);
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
