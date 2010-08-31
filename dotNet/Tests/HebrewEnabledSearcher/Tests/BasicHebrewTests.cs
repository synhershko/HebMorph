using System;
using System.Collections.Generic;
using System.Text;

using Lucene.Net.Analysis;
using Lucene.Net.Store;
using Lucene.Net.Index;
using Lucene.Net.Documents;
using Lucene.Net.Search;
using Lucene.Net.QueryParsers;

namespace HebrewEnabledSearcher.Tests
{
    public class BasicHebrewTests
    {
        Analyzer analyzer;

        public BasicHebrewTests(Analyzer a)
        {
            analyzer = a;
        }

        public void Run()
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
            Assert(bCaught);

            try
            {
                AssertFoundInText("שבתו", "שביו"); // incorrect
                bCaught = false;
            }
            catch { }
            Assert(bCaught);

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
            Assert(bCaught);

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

            System.Windows.Forms.MessageBox.Show("All tests pass!");
        }

        protected void Assert(bool statement)
        {
            if (!statement)
                throw new Exception("Assertion failed");
        }

        protected void AssertFoundInText(string whatToIndex, string whatToSearch)
        {
            Directory d = new RAMDirectory();

            IndexWriter writer = new IndexWriter(d, analyzer, true, new IndexWriter.MaxFieldLength(10000));
            Document doc = new Document();
            doc.Add(new Field("content", whatToIndex, Field.Store.YES, Field.Index.ANALYZED));
            writer.AddDocument(doc);
            writer.Close();
            writer = null;

            IndexSearcher searcher = new IndexSearcher(d, true); // read-only=true
            QueryParser qp = new QueryParser(Lucene.Net.Util.Version.LUCENE_29, "content", analyzer);
            Query query = qp.Parse(whatToSearch);
            ScoreDoc[] hits = searcher.Search(query, null, 1000).scoreDocs;

            Assert(hits.Length == 1);

            searcher.Close();

            d.Close();
        }
    }
}
