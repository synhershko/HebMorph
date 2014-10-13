package org.apache.lucene.queryparsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import java.util.Map;

/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class HebrewMultiFieldQueryParser extends MultiFieldQueryParser {
    HebrewMultiFieldQueryParser(Version matchVersion, String[] fields, Analyzer analyzer) {
        super(matchVersion, fields, analyzer);
    }

    HebrewMultiFieldQueryParser(Version matchVersion, String[] fields, Analyzer analyzer, Map<String, Float> boosts) {
        super(matchVersion, fields, analyzer, boosts);
    }

    /// <summary> Parses a query, searching on the fields specified. Use this if you need
    /// to specify certain fields as required, and others as prohibited.
    /// <p/>
    ///
    /// <pre>
    /// Usage:
    /// &lt;code&gt;
    /// String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
    /// BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
    /// BooleanClause.Occur.MUST,
    /// BooleanClause.Occur.MUST_NOT};
    /// MultiFieldQueryParser.parse(&quot;query&quot;, fields, flags, analyzer);
    /// &lt;/code&gt;
    /// </pre>
    /// <p/>
    /// The code above would construct a query:
    ///
    /// <pre>
    /// &lt;code&gt;
    /// (filename:query) +(contents:query) -(description:query)
    /// &lt;/code&gt;
    /// </pre>
    ///
    /// </summary>
    /// <param name="matchVersion">Lucene version to match; this is passed through to
    /// QueryParser.
    /// </param>
    /// <param name="query">Query string to parse
    /// </param>
    /// <param name="fields">Fields to search on
    /// </param>
    /// <param name="flags">Flags describing the fields
    /// </param>
    /// <param name="analyzer">Analyzer to use
    /// </param>
    /// <throws>  ParseException </throws>
    /// <summary>             if query parsing fails
    /// </summary>
    /// <throws>  IllegalArgumentException </throws>
    /// <summary>             if the length of the fields array differs from the length of
    /// the flags array
    /// </summary>
    public static Query parse(Version matchVersion, String query, String[] fields, BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
        if (fields.length > flags.length)
            throw new IllegalArgumentException("fields.length != flags.length");
        BooleanQuery bQuery = new BooleanQuery();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(matchVersion, fields[i], analyzer);
            Query q = qp.parse(query);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
                bQuery.add(q, flags[i]);
            }
        }
        return bQuery;
    }

    /// <summary> Parses a query which searches on the fields specified.
    /// <p/>
    /// If x fields are specified, this effectively constructs:
    ///
    /// <pre>
    /// &lt;code&gt;
    /// (field1:query1) (field2:query2) (field3:query3)...(fieldx:queryx)
    /// &lt;/code&gt;
    /// </pre>
    ///
    /// </summary>
    /// <param name="matchVersion">Lucene version to match; this is passed through to
    /// QueryParser.
    /// </param>
    /// <param name="queries">Queries strings to parse
    /// </param>
    /// <param name="fields">Fields to search on
    /// </param>
    /// <param name="analyzer">Analyzer to use
    /// </param>
    /// <throws>  ParseException </throws>
    /// <summary>             if query parsing fails
    /// </summary>
    /// <throws>  IllegalArgumentException </throws>
    /// <summary>             if the length of the queries array differs from the length of
    /// the fields array
    /// </summary>
    public static Query parse(Version matchVersion, String[] queries, String[] fields, Analyzer analyzer) throws ParseException {
        if (queries.length != fields.length)
            throw new IllegalArgumentException("queries.length != fields.length");
        BooleanQuery bQuery = new BooleanQuery();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(matchVersion, fields[i], analyzer);
            Query q = qp.parse(queries[i]);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
                bQuery.add(q, BooleanClause.Occur.SHOULD);
            }
        }
        return bQuery;
    }

    /// <summary> Parses a query, searching on the fields specified. Use this if you need
    /// to specify certain fields as required, and others as prohibited.
    /// <p/>
    ///
    /// <pre>
    /// Usage:
    /// &lt;code&gt;
    /// String[] query = {&quot;query1&quot;, &quot;query2&quot;, &quot;query3&quot;};
    /// String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
    /// BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
    /// BooleanClause.Occur.MUST,
    /// BooleanClause.Occur.MUST_NOT};
    /// MultiFieldQueryParser.parse(query, fields, flags, analyzer);
    /// &lt;/code&gt;
    /// </pre>
    /// <p/>
    /// The code above would construct a query:
    ///
    /// <pre>
    /// &lt;code&gt;
    /// (filename:query1) +(contents:query2) -(description:query3)
    /// &lt;/code&gt;
    /// </pre>
    ///
    /// </summary>
    /// <param name="matchVersion">Lucene version to match; this is passed through to
    /// QueryParser.
    /// </param>
    /// <param name="queries">Queries string to parse
    /// </param>
    /// <param name="fields">Fields to search on
    /// </param>
    /// <param name="flags">Flags describing the fields
    /// </param>
    /// <param name="analyzer">Analyzer to use
    /// </param>
    /// <throws>  ParseException </throws>
    /// <summary>             if query parsing fails
    /// </summary>
    /// <throws>  IllegalArgumentException </throws>
    /// <summary>             if the length of the queries, fields, and flags array differ
    /// </summary>
    public static Query parse(Version matchVersion, String[] queries, String[] fields, BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
        if (!(queries.length == fields.length && queries.length == flags.length))
            throw new IllegalArgumentException("queries, fields, and flags array have have different length");
        BooleanQuery bQuery = new BooleanQuery();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(matchVersion, fields[i], analyzer);
            Query q = qp.parse(queries[i]);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
                bQuery.add(q, flags[i]);
            }
        }
        return bQuery;
    }
}

