using System;
using HebMorph.DataStructures;
using HebMorph.Tests;
using Lucene.Net.Analysis;
using Lucene.Net.Analysis.Hebrew;
using Lucene.Net.Analysis.Standard;
using Lucene.Net.Documents;
using Lucene.Net.Index;
using Lucene.Net.Search;
using Lucene.Net.Store;
using Xunit;
using Version = Lucene.Net.Util.Version;

namespace HebMorph.Lucene.Tests
{
	public class HtmlStandardAnalyzer : StandardAnalyzer
	{
		public HtmlStandardAnalyzer()
			: base(Version.LUCENE_30)
		{
		}

		public override TokenStream TokenStream(string fieldName, System.IO.TextReader reader)
		{
			var htmlCharFilter = new HTMLStripCharFilter(CharReader.Get(reader));
			return base.TokenStream(fieldName, htmlCharFilter);
		}
	}

	public class HtmlMorphAnalyzer : MorphAnalyzer
	{
		public HtmlMorphAnalyzer(MorphAnalyzer other)
			: base(other)
		{
		}

		public HtmlMorphAnalyzer(StreamLemmatizer hml)
			: base(hml)
		{
		}

		public HtmlMorphAnalyzer(string HSpellDataFilesPath)
			: base(HSpellDataFilesPath)
		{
		}

		public HtmlMorphAnalyzer(DictRadix<MorphData> dictRadix) : base(dictRadix)
		{
		}

		public override TokenStream TokenStream(string fieldName, System.IO.TextReader reader)
		{
			var filter = new HTMLStripCharFilter(CharReader.Get(reader));
			return base.TokenStream(fieldName, filter);
		}
	}

	public class MorphAnalyzerTests : TestBase
	{
		[Fact]
		public void CompareTokenization()
		{
			const string str = @"test1 testlink test2 test3";

			PerFieldAnalyzerWrapper pfaw = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_30));
			pfaw.AddAnalyzer("Morph", new MorphAnalyzer(HspellDict));
			Directory indexDirectory = new RAMDirectory();
			IndexWriter writer = new IndexWriter(indexDirectory, pfaw, true, IndexWriter.MaxFieldLength.UNLIMITED);

			Document doc = new Document();
			doc.Add(new Field("Simple", str, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
			doc.Add(new Field("Morph", str, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
			writer.AddDocument(doc);
			writer.Close();

			CompareTermData(indexDirectory, str);
		}

		[Fact]
		public void CompareHtmlTokenization()
		{
			const string str = @"test1 <a href=""foo"">testlink</a> test2 test3";

			PerFieldAnalyzerWrapper pfaw = new PerFieldAnalyzerWrapper(new HtmlStandardAnalyzer());
			pfaw.AddAnalyzer("Morph", new HtmlMorphAnalyzer(HspellDict));
			Directory indexDirectory = new RAMDirectory();
			IndexWriter writer = new IndexWriter(indexDirectory, pfaw, true, IndexWriter.MaxFieldLength.UNLIMITED);

			Document doc = new Document();
			doc.Add(new Field("Simple", str, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
			doc.Add(new Field("Morph", str, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
			writer.AddDocument(doc);
			writer.Close();

			CompareTermData(indexDirectory, str);
		}

		private static void CompareTermData(Directory dir, string str)
		{
			IndexSearcher searcher = new IndexSearcher(dir, true);
			var tf = searcher.IndexReader.GetTermFreqVectors(0);

			TermPositionVector tpMorph = (TermPositionVector)tf[0];
			TermPositionVector tpSimple = (TermPositionVector)tf[1];

			for (int i = 0; i < 4; i++)
			{
				int[] posMorph = tpMorph.GetTermPositions(i);
				int[] posSimple = tpSimple.GetTermPositions(i);
				for (int j = 0; j < posSimple.Length; j++)
					Assert.Equal(posSimple[j], posMorph[j]);

				TermVectorOffsetInfo[] offMorph = tpMorph.GetOffsets(i);
				TermVectorOffsetInfo[] offSimple = tpSimple.GetOffsets(i);
				for (int j = 0; j < offSimple.Length; j++)
				{
					Console.WriteLine(str.Substring(offSimple[j].StartOffset, offSimple[j].EndOffset - offSimple[j].StartOffset));
					Assert.Equal(offSimple[j].StartOffset, offMorph[j].StartOffset);
					Assert.Equal(offSimple[j].EndOffset, offMorph[j].EndOffset);
				}
			}
		}
	}
}
