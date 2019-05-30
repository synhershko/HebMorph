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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparsers.HebrewQueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TermPositionVectorTest extends TestBase {
    Analyzer analyzer;
    Directory indexDirectory;
    IndexSearcher searcher;
    FieldType fieldType;

    @Before
    public void setUp() throws Exception {
        fieldType = initFieldType();
        analyzer = new HebrewIndexingAnalyzer(getDictionary());
    }

    @After
    public void tearDown() throws Exception {
        if (analyzer != null)
            analyzer.close();
    }

    @Test
    public void storesPositionCorrectly() throws Exception {
        indexDirectory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer); //use of Version, need to look at this.
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
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setTokenized(true);
        type.setStored(false);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPositions(true);

        return type;
    }

    private void runQuery(String query, int expectedPosition) throws ParseException, IOException {
        HebrewQueryParser hqp =
                new HebrewQueryParser("Text", analyzer);

        Query q = hqp.parse(query);

        TopDocs td = searcher.search(q, searcher.getIndexReader().maxDoc());

        int num = td.scoreDocs[0].doc;
        Terms terms = searcher.getIndexReader().getTermVectors(num).terms("Text");

        Set<Term> trms_list = new HashSet<>();
        searcher.createWeight(q, ScoreMode.COMPLETE, 1.0f).extractTerms(trms_list);
//        q.extractTerms(trms_list);

        for (Term t : trms_list) {
            TermsEnum termsEnum = terms.iterator();
            boolean isFound = termsEnum.seekExact(t.bytes());
            Assert.assertTrue(isFound);

            PostingsEnum dpEnum = termsEnum.postings(null);
            assertTrue(dpEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS);
            int pos = dpEnum.nextPosition();
            //assertEquals(expectedPosition, dpEnum.startOffset());
            //assertEquals(??, dpEnum.endOffset());
            assertEquals(DocIdSetIterator.NO_MORE_DOCS, dpEnum.nextDoc());
            assertEquals(pos, expectedPosition);
        }
    }
}
