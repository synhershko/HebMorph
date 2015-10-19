/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.lemmafilters.BasicLemmaFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BasicHebrewTest extends TestBase {
    private Analyzer analyzer;

    public BasicHebrewTest() throws IOException {
        analyzer = new TestAnalyzer();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws Exception {
        // Warm up with exact matches...
        assertFoundInText("בת", "בת");
        assertFoundInText("שבתו", "שבתו");
        assertFoundInText("אנציקלופדיה", "אנציקלופדיה");

        // Same written word, several different ways to read it. Even a human won't know which is correct
        // without Niqqud or some context.
        assertFoundInText("שבתו", "בת"); // prefix + suffix
        assertFoundInText("שבתו", "תו"); // prefixes
        assertFoundInText("שבתו", "ישב"); // verb inflections
        assertFoundInText("שבתו", "שבתנו");

        assertNotFoundInText("שבתו", "שיבה"); // too much of a tolerance for searches...
        assertNotFoundInText("שבתו", "שביו"); // incorrect

        assertFoundInText("כלבי", "כלבי");
        assertFoundInText("כלבי", "לב");
        assertFoundInText("כלבי", "כלב");

        // Prefixes
        assertFoundInText("ליונתן", "יונתן");
        assertFoundInText("כלבי", "לכלבי");
        assertFoundInText("לכלבי", "כלבי");
        assertFoundInText("לכלבי", "לכלבי");

        assertNotFoundInText("לליונתן", "ליונתן"); // invalid prefix

        // Singular -> plural, with affixes and non-standard plurals
        assertFoundInText("דמעות", "דמעה");
        assertFoundInText("דמעות", "דמעתי");
        assertFoundInText("דמעות", "דמעותינו");
        assertFoundInText("לתפילתנו", "תפילה");
        assertFoundInText("תפילתנו", "לתפילתי");

        assertFoundInText("אחשוורוש", "אחשורוש"); // consonant vav tolerance
        assertFoundInText("לאחשוורוש", "אחשורוש"); // consonant vav tolerance + prefix
        assertFoundInText("אימא", "אמא"); // yud tolerance (yep, this is the correct spelling...)
        assertFoundInText("אמא", "אמא"); // double tolerance - both in indexing and QP

        assertFoundInText("אצטרולב", "אצטרולב"); // OOV case, should be stored as-is
        assertFoundInText("test", "test"); // Non hebrew, should be stored as-is
        assertFoundInText("test sun", "sun"); // Non hebrew, multiple
        assertFoundInText("1234", "1234"); // Numeric, should be stored as-is
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    public void testLemmatization() throws Exception {
        analyzer = new TestAnalyzer();
        final TokenStream ts = analyzer.tokenStream("foo", new StringReader("מינהל"));
        ts.reset();

        Set<String> terms = new HashSet<>();
        while (ts.incrementToken()) {
            CharTermAttribute att = ts.getAttribute(CharTermAttribute.class);
            terms.add(new String(att.buffer(), 0, att.length()));
            //System.out.println(new String(att.buffer(), 0, att.length()));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Test
    public void testFinalOffset() throws Exception {
        analyzer = new TestAnalyzer();
        final TokenStream ts = analyzer.tokenStream("foo", new StringReader("מינהל"));
        OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
        }
        ts.end();
        assertEquals(5, offsetAttribute.endOffset());
    }

    protected void assertFoundInText(String whatToIndex, String whatToSearch) throws Exception {
        //System.out.println(whatToIndex);
        assertEquals(1, findInText(whatToIndex, whatToSearch));
    }

    protected void assertNotFoundInText(String whatToIndex, String whatToSearch) throws Exception {
        assertEquals(0, findInText(whatToIndex, whatToSearch));
    }

    protected int findInText(String whatToIndex, String whatToSearch) throws Exception {
        final Directory d = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer); //use of Version, need to look at this.
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(d, config);
        Document doc = new Document();
        doc.add(new TextField("content", whatToIndex, Store.YES));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(d));
        QueryParser qp = new QueryParser("content", analyzer);
        Query query = qp.parse(whatToSearch);
        ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

        writer.close();
        d.close();

        return hits.length;
    }

    private class TestAnalyzer extends Analyzer {

        public TestAnalyzer() throws IOException {
            super();
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            StreamLemmasFilter src = null;
            try {
                src = new StreamLemmasFilter(getDictionary(), null, new BasicLemmaFilter());
                src.setKeepOriginalWord(true);
                src.setSuffixForExactMatch('$');
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TokenStreamComponents(src);
        }
    }
}

