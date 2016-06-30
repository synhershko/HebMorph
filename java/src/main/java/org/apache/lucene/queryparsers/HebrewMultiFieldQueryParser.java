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
package org.apache.lucene.queryparsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.Map;

public class HebrewMultiFieldQueryParser extends MultiFieldQueryParser {
    HebrewMultiFieldQueryParser(String[] fields, Analyzer analyzer) {
        super(fields, analyzer);
    }

    HebrewMultiFieldQueryParser(String[] fields, Analyzer analyzer, Map<String, Float> boosts) {
        super(fields, analyzer, boosts);
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
    public static Query parse(String query, String[] fields, BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
        if (fields.length > flags.length)
            throw new IllegalArgumentException("fields.length != flags.length");
        BooleanQuery.Builder bQueryBuilder = new BooleanQuery.Builder();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(fields[i], analyzer);
            Query q = qp.parse(query);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).clauses().size() > 0)) {
                bQueryBuilder.add(q,flags[i]);
            }
        }
        return bQueryBuilder.build();
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
    public static Query parse(String[] queries, String[] fields, Analyzer analyzer) throws ParseException {
        if (queries.length != fields.length)
            throw new IllegalArgumentException("queries.length != fields.length");
        BooleanQuery.Builder bQueryBuilder = new BooleanQuery.Builder();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(fields[i], analyzer);
            Query q = qp.parse(queries[i]);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).clauses().size() > 0)) {
                bQueryBuilder.add(q, BooleanClause.Occur.SHOULD);
            }
        }
        return bQueryBuilder.build();
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
    public static Query parse(String[] queries, String[] fields, BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
        if (!(queries.length == fields.length && queries.length == flags.length))
            throw new IllegalArgumentException("queries, fields, and flags array have have different length");
        BooleanQuery.Builder bQueryBuilder = new BooleanQuery.Builder();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new HebrewQueryParser(fields[i], analyzer);
            Query q = qp.parse(queries[i]);
            if (q != null && (!(q instanceof BooleanQuery) || ((BooleanQuery) q).clauses().size() > 0)) {
                bQueryBuilder.add(q, flags[i]);
            }
        }
        return bQueryBuilder.build();
    }
}

