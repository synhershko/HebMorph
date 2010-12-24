
package org.apache.lucene.analysis.hebrew;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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

/**
 *
 * @author kirillkh
 */
public class TermPositionVectorTest {
    static private final String DEFAULT_HSPELL_PATH = "../../hspell-data-files";
    static String hspellPath;

    Analyzer analyzer;
    Directory indexDirectory;
    IndexSearcher searcher;


	@Before
	public void setUp() throws Exception
	{
        hspellPath = DEFAULT_HSPELL_PATH;
		analyzer = new MorphAnalyzer(hspellPath);
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void storesPositionCorrectly() throws Exception
	{
        analyzer = new MorphAnalyzer(hspellPath);
        indexDirectory = new RAMDirectory();

        IndexWriter writer = new IndexWriter(indexDirectory, analyzer, true, new IndexWriter.MaxFieldLength(Integer.MAX_VALUE));

        String str = "קשת רשת דבשת מיץ יבשת יבלת גחלת גדר אינציקלופדיה חבר";
        Document doc = new Document();
        doc.add(new Field("Text", str, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        writer.addDocument(doc);
        writer.close();

        searcher = new IndexSearcher(indexDirectory, true);

        runQuery("\"קשת\"", 0);
        runQuery("\"אינציקלופדיה\"", 8);
        runQuery("\"חבר\"", 9);

        searcher.close();
        indexDirectory.close();
    }

    private void runQuery(String query, int expectedPosition) throws ParseException, IOException
    {
        HebrewQueryParser hqp =
            new HebrewQueryParser(Version.LUCENE_29, "Text", analyzer);

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


    static class HebrewQueryParser extends QueryParser {
        protected static float SuffixedTermBoost = 2.0f;

        public HebrewQueryParser(Version matchVersion, String f, Analyzer a)
        {
            super(matchVersion, f, a);
        }

        public @Override Query parse(String query) throws ParseException
        {
            String q = "";

            for (int i = 0; i < query.length(); i++)
            {
                if (query.charAt(i) == '"' && i + 1 < query.length() && !Character.isWhitespace(query.charAt(i + 1)))
                    if (i > 0 && !Character.isWhitespace(query.charAt(i - 1)))
                        q += '\\';
                q += query.charAt(i);
            }

            return super.parse(q);
        }
    }
}
