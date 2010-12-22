package org.apache.lucene.queryparsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
