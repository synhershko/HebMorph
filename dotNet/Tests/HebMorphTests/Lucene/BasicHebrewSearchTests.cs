using Lucene.Net.Analysis;
using Lucene.Net.Analysis.Hebrew;
using Lucene.Net.Documents;
using Lucene.Net.Index;
using Lucene.Net.QueryParsers;
using Lucene.Net.Search;
using Lucene.Net.Store;
using Lucene.Net.Util;
using Xunit;

namespace HebMorph.Tests.Lucene
{
	public class BasicHebrewSearchTests : TestBase
	{
		private readonly Analyzer analyzer;

		public BasicHebrewSearchTests()
		{
			analyzer = new MorphAnalyzer(HspellDict);
		}

		[Fact]
		public void CanFindStandardHebrewWords()
		{
			bool bCaught = true;

			// Warm up with exact matches...
			AssertFoundInText("בת", "בת");
			AssertFoundInText("שבתו", "שבתו");
			AssertFoundInText("אנציקלופדיה", "אנציקלופדיה");

			// Same written word, several different ways to read it. Even a human won't know which is correct
			// without Niqqud or some context.
			AssertFoundInText("שבתו", "בת"); // prefix + suffix
			AssertFoundInText("שבתו", "תו"); // prefixes
			AssertFoundInText("שבתו", "ישב"); // verb inflections
			AssertFoundInText("שבתו", "שבתנו");

			try
			{
				AssertFoundInText("שבתו", "שיבה"); // too much of a tolerance for searches...
				bCaught = false;
			}
			catch { }
			Assert.True(bCaught);

			try
			{
				AssertFoundInText("שבתו", "שביו"); // incorrect
				bCaught = false;
			}
			catch { }
			Assert.True(bCaught);

			AssertFoundInText("כלבי", "כלבי");
			AssertFoundInText("כלבי", "לב");
			AssertFoundInText("כלבי", "כלב");

			// Prefixes
			AssertFoundInText("ליונתן", "יונתן");
			AssertFoundInText("כלבי", "לכלבי");
			AssertFoundInText("לכלבי", "כלבי");
			AssertFoundInText("לכלבי", "לכלבי");

			bCaught = true;
			try
			{
				AssertFoundInText("לליונתן", "ליונתן"); // invalid prefix
				bCaught = false;
			}
			catch { }
			Assert.True(bCaught);

			// Singular -> plural, with affixes and non-standard plurals
			AssertFoundInText("דמעות", "דמעה");
			AssertFoundInText("דמעות", "דמעתי");
			AssertFoundInText("דמעות", "דמעותינו");
			AssertFoundInText("לתפילתנו", "תפילה");
			AssertFoundInText("תפילתנו", "לתפילתי");

			AssertFoundInText("אחשוורוש", "אחשורוש"); // consonant vav tolerance
			AssertFoundInText("לאחשוורוש", "אחשורוש"); // consonant vav tolerance + prefix
			AssertFoundInText("אימא", "אמא"); // yud tolerance (yep, this is the correct spelling...)
			AssertFoundInText("אמא", "אמא"); // double tolerance - both in indexing and QP

			AssertFoundInText("אצטרולב", "אצטרולב"); // OOV case, should be stored as-is
			AssertFoundInText("test", "test"); // Non hebrew, should be stored as-is
			AssertFoundInText("1234", "1234"); // Numeric, should be stored as-is
		}

		protected void AssertFoundInText(string whatToIndex, string whatToSearch)
		{
			using (var d = new RAMDirectory())
			{
				using (var writer = new IndexWriter(d, analyzer, true, new IndexWriter.MaxFieldLength(10000)))
				{
					Document doc = new Document();
					doc.Add(new Field("content", whatToIndex, Field.Store.YES, Field.Index.ANALYZED));
					writer.AddDocument(doc);
					writer.Close();
				}

				using (var searcher = new IndexSearcher(d, true))
				{
					QueryParser qp = new QueryParser(Version.LUCENE_30, "content", analyzer);
					Query query = qp.Parse(whatToSearch);
					var hits = searcher.Search(query, null, 1000).ScoreDocs;

					Assert.Equal(1, hits.Length);
				}
			}
		}
	}
}
