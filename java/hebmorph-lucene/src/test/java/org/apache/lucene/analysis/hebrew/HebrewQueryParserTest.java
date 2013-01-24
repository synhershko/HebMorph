package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparsers.HebrewQueryParser;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

public class HebrewQueryParserTest
{
    @Test
    public void ParsesAcronymsCorrectly() throws ParseException, IOException {
        QueryParser qp = new HebrewQueryParser(Version.LUCENE_41, "f", new SimpleAnalyzer(Version.LUCENE_41));
        qp.parse("צה\"ל");
        qp.parse("\"צהל\"");
        qp.parse("כל הכבוד לצה\"ל");
        qp.parse("\"כל הכבוד לצה\"ל\"");
        qp.parse("\"כל הכבוד\" לצה\"ל");

        qp.parse("מנכ\"לית");

        try
        {
            qp.parse("צה\"\"ל");
            qp.parse("\"צה\"ל");
            fail("Expected exception was not thrown");
        }
        catch(ParseException ex) { }
    }
}
