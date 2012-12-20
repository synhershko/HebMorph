/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
 *                                                                         *
 *   Distributed under the GNU General Public License, Version 2.0.        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation (v2).                                    *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Steet, Fifth Floor, Boston, MA  02111-1307, USA.          *
 **************************************************************************/
package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class BasicHebrewTests extends TestBase {
	private Analyzer analyzer;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception {
		analyzer = new MorphAnalyzer(Version.LUCENE_36, getDictionary());
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

		//assertFoundInText("אחשוורוש", "אחשורוש"); // consonant vav tolerance
		//assertFoundInText("לאחשוורוש", "אחשורוש"); // consonant vav tolerance + prefix
		//assertFoundInText("אימא", "אמא"); // yud tolerance (yep, this is the correct spelling...)
		assertFoundInText("אמא", "אמא"); // double tolerance - both in indexing and QP

		assertFoundInText("אצטרולב", "אצטרולב"); // OOV case, should be stored as-is
		assertFoundInText("test", "test"); // Non hebrew, should be stored as-is
		assertFoundInText("1234", "1234"); // Numeric, should be stored as-is

	}


	protected void assertFoundInText(String whatToIndex, String whatToSearch) throws Exception
	{
		System.out.println(whatToIndex);
		assertEquals(1, findInText(whatToIndex, whatToSearch));
	}

	protected void assertNotFoundInText(String whatToIndex, String whatToSearch) throws Exception
	{
		assertEquals(0, findInText(whatToIndex, whatToSearch));
	}

	protected int findInText(String whatToIndex, String whatToSearch) throws Exception
	{
		final Directory d = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(d, config);
		Document doc = new Document();
		doc.add(new Field("content", whatToIndex, Field.Store.YES, Field.Index.ANALYZED));
		writer.addDocument(doc);
		writer.close();

		IndexSearcher searcher = new IndexSearcher(IndexReader.open(d));
		QueryParser qp = new QueryParser(Version.LUCENE_36, "content", analyzer);
		Query query = qp.parse(whatToSearch);
		ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;


		searcher.close();

		d.close();
		return hits.length;
	}

}