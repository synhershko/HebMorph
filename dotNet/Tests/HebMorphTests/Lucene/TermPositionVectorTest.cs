using System.Collections.Generic;
using HebMorph.Tests;
using Lucene.Net.Index;
using Lucene.Net.Store;
using Lucene.Net.Documents;
using Lucene.Net.Analysis.Hebrew;
using Lucene.Net.Analysis;
using Lucene.Net.Search;
using Lucene.Net.QueryParsers.Hebrew;
using Xunit;

namespace HebMorph.Lucene.Tests
{
    public class TermPositionVectorTest : TestBase
    {
        Analyzer analyzer;
        Directory indexDirectory;
        IndexSearcher searcher;

		[Fact]
        public void StoresPositionCorrectly()
        {
            analyzer = new MorphAnalyzer(hspellPath);
            indexDirectory = new RAMDirectory();
            
            IndexWriter writer = new IndexWriter(indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

            string str = "קשת רשת דבשת מיץ יבשת יבלת גחלת גדר אינציקלופדיה חבר";
            Document doc = new Document();
            doc.Add(new Field("Text", str, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
            writer.AddDocument(doc);
            writer.Close();

            searcher = new IndexSearcher(indexDirectory, true);

            RunQuery("\"קשת\"", 0);
            RunQuery("\"אינציקלופדיה\"", 8);
            RunQuery("\"חבר\"", 9);

            searcher.Close();
            indexDirectory.Close();
        }

        private void RunQuery(string query, int expectedPosition)
        {
            var hqp = new HebrewQueryParser(global::Lucene.Net.Util.Version.LUCENE_29, "Text", analyzer);

            Query q = hqp.Parse(query);

            TopDocs td = searcher.Search(q, 10000);

            int num = td.ScoreDocs[0].Doc;
            var tf = searcher.IndexReader.GetTermFreqVectors(num)[0];
            var tp = (TermPositionVector)tf;

	        var trms_list = new SortedSet<Term>();
            q.ExtractTerms(trms_list);
            foreach (var t in trms_list)
            {
                int[] pos = tp.GetTermPositions(tp.IndexOf(t.Text));
                TermVectorOffsetInfo[] off = tp.GetOffsets(tp.IndexOf(t.Text));
                AssertSinglePositionExists(pos, expectedPosition);

                /*
                string sPos = "";
                string sOff = "";
                foreach (int p in pos)
                {
                    sPos += " " + p;
                }
                foreach (TermVectorOffsetInfo o in off)
                {
                    sOff += " (" + o.GetStartOffset() + "," + o.GetEndOffset() + ")";
                }
                Trace.WriteLine(string.Format("Term: {0} Pos:{1} Off:{2}", t.Text(), sPos, sOff));
                */
            }
        }

        private static void AssertSinglePositionExists(int[] positions, int pos)
        {
            Assert.Equal(1, positions.Length);
            Assert.Equal(pos, positions[0]);
        }
    }
}
