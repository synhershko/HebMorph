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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;

public class HebrewQueryParser extends QueryParser {

    protected static float SuffixedTermBoost = 2.0F;

    public HebrewQueryParser(Version matchVersion, String f, Analyzer a) {
        super(matchVersion, f, a);
    }

    @Override
    public org.apache.lucene.search.Query parse(String query) throws ParseException {
        String q = "";
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '"' && i + 1 < query.length() && !Character.isWhitespace(query.charAt(i + 1))) {
                if (i > 0 && !Character.isWhitespace(query.charAt(i - 1))) {
                    q += '\\';
                }
            }
            q += query.charAt(i);
        }
        return super.parse(q);
    }
}
