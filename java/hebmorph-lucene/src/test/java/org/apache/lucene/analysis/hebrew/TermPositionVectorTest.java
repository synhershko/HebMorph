
package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryparsers.HebrewQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TermPositionVectorTest extends TestBase {
    Analyzer analyzer;
    Directory indexDirectory;
    IndexSearcher searcher;

	@Before
	public void setUp() throws Exception {
		analyzer = new MorphAnalyzer(Version.LUCENE_36, getDictionary(), null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void storesPositionCorrectly() throws Exception {
        indexDirectory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);

        String str = "קשת רשת דבשת מיץ יבשת יבלת גחלת גדר אינציקלופדיה חבר";
        Document doc = new Document();
        doc.add(new Field("Text", str, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        writer.addDocument(doc);
        writer.close();

        final IndexReader reader = IndexReader.open(indexDirectory);
        searcher = new IndexSearcher(reader);

        runQuery("\"קשת\"", 0);
        runQuery("\"אינציקלופדיה\"", 8);
        runQuery("\"חבר\"", 9);

        searcher.close();
        indexDirectory.close();
    }

    private void runQuery(String query, int expectedPosition) throws ParseException, IOException
    {
        HebrewQueryParser hqp =
            new HebrewQueryParser(Version.LUCENE_36, "Text", analyzer);

        Query q = hqp.parse(query);

        TopDocs td = searcher.search(q, 10000);

        int num = td.scoreDocs[0].doc;
        TermFreqVector tf = searcher.getIndexReader().getTermFreqVectors(num)[0];
        TermPositionVector tp = (TermPositionVector)tf;

        Set<Term> trms_list = new HashSet<Term>();
        q.extractTerms(trms_list);
        for (Term t : trms_list)
        {
            int[] pos = tp.getTermPositions(tp.indexOf(t.text()));
            TermVectorOffsetInfo[] off = tp.getOffsets(tp.indexOf(t.text()));
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
        Assert.assertEquals(1, positions.length);
        Assert.assertEquals(pos, positions[0]);
    }
}
