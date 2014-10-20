
package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.hspell.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparsers.HebrewQueryParser;
import org.apache.lucene.search.DocIdSetIterator;
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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TermPositionVectorTest extends TestBase {
    Analyzer analyzer;
    Directory indexDirectory;
    IndexSearcher searcher;
    FieldType fieldType;

	@Before
	public void setUp() throws Exception {
		fieldType = initFieldType();
		analyzer = new MorphAnalyzer(Version.LUCENE_46, getDictionary(), FileUtils.getPrefixes(false));
	}

	@After
	public void tearDown() throws Exception {
		if (analyzer != null)
			analyzer.close();
	}

	@Test
	public void storesPositionCorrectly() throws Exception {
        indexDirectory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(indexDirectory, config);

        String str = "קשת רשת דבשת מיץ יבשת יבלת גחלת גדר אינציקלופדיה חבר";
        Document doc = new Document();

        doc.add(new Field("Text", str, fieldType));
        writer.addDocument(doc);
        writer.close();

        searcher = new IndexSearcher(DirectoryReader.open(indexDirectory));

        runQuery("\"קשת\"", 0);
        runQuery("\"אינציקלופדיה\"", 8);
        runQuery("\"חבר\"", 9);

        indexDirectory.close();
    }
	
	private FieldType initFieldType() {
		FieldType type = new FieldType();
		type.setIndexed(true);
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setTokenized(true);
		type.setStored(false);
		type.setStoreTermVectors(true);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPositions(true);
        
        return type;
	}

    private void runQuery(String query, int expectedPosition) throws ParseException, IOException
    {
        HebrewQueryParser hqp =
            new HebrewQueryParser(Version.LUCENE_46, "Text", analyzer);

        Query q = hqp.parse(query);

        TopDocs td = searcher.search(q, searcher.getIndexReader().maxDoc());

        int num = td.scoreDocs[0].doc;
        Terms terms = searcher.getIndexReader().getTermVectors(num).terms("Text");
        
        Set<Term> trms_list = new HashSet<Term>();
        q.extractTerms(trms_list);

        for (Term t : trms_list) {
        	TermsEnum termsEnum = terms.iterator(TermsEnum.EMPTY);
        	boolean isFound = termsEnum.seekExact(t.bytes());
        	Assert.assertTrue(isFound);

            DocsAndPositionsEnum dpEnum = termsEnum.docsAndPositions(null, null);
            assertTrue(dpEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS);
            int pos = dpEnum.nextPosition();
            //assertEquals(expectedPosition, dpEnum.startOffset());
            //assertEquals(??, dpEnum.endOffset());
            assertEquals(DocIdSetIterator.NO_MORE_DOCS, dpEnum.nextDoc());

        	Assert.assertEquals(pos, expectedPosition);
        }
    }
}
