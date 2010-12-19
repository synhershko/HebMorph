using System;
using System.Collections.Generic;
using System.Text;

using Lucene.Net.Index;
using Lucene.Net.Store;
using Lucene.Net.Documents;

using Lucene.Net.Analysis.Hebrew;
using Lucene.Net.Analysis;

using Lucene.Net.Search;

using Lucene.Net.Search.Spans;
using Lucene.Net.QueryParsers;
using Lucene.Net.QueryParsers.Hebrew;

using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Diagnostics;

namespace HebMorph.Lucene.Tests
{
    [TestClass()]
    public class TermPositionVectorTest
    {
        static string hspellPath;

        Analyzer analyzer;
        Directory indexDirectory;
        IndexSearcher searcher;

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
        public void StoresPositionCorrectly()
        {
            analyzer = new MorphAnalyzer(hspellPath);
            indexDirectory = new RAMDirectory();
            
            IndexWriter writer = new IndexWriter(indexDirectory, analyzer, true, new IndexWriter.MaxFieldLength(int.MaxValue));

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
            HebrewQueryParser hqp =
                new HebrewQueryParser(global::Lucene.Net.Util.Version.LUCENE_29, "Text", analyzer);

            Query q = hqp.Parse(query);

            TopDocs td = searcher.Search(q, 10000);

            int num = td.scoreDocs[0].doc;
            TermFreqVector tf = searcher.GetIndexReader().GetTermFreqVectors(num)[0];
            TermPositionVector tp = (TermPositionVector)tf;

            System.Collections.Hashtable trms_list = new System.Collections.Hashtable();
            q.ExtractTerms(trms_list);
            foreach (Term t in trms_list.Values)
            {
                int[] pos = tp.GetTermPositions(tp.IndexOf(t.Text()));
                TermVectorOffsetInfo[] off = tp.GetOffsets(tp.IndexOf(t.Text()));
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

        private void AssertSinglePositionExists(int[] positions, int pos)
        {
            Assert.AreEqual<int>(1, positions.Length);
            Assert.AreEqual<int>(pos, positions[0]);
        }
    }
}
