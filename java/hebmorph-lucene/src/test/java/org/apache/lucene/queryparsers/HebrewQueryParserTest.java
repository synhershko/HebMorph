package org.apache.lucene.queryparsers;

import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.hebrew.SimpleAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

public class HebrewQueryParserTest
{
    @Test
    public void ParsesAcronymsCorrectly() throws ParseException, IOException {

        QueryParser qp = new HebrewQueryParser(Version.LUCENE_46, "f", new SimpleAnalyzer(Version.LUCENE_46, HSpellLoader.readDefaultPrefixes()));
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
