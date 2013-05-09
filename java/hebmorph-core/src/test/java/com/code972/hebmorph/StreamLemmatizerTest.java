package com.code972.hebmorph;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StreamLemmatizerTest extends TestBase
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

    @Before
    public void SetUp()
    {
    }

    @Test
    public void IncrementsOffsetCorrectly() throws IOException
    {
        String input = "";
        for (int repeat = 0; repeat < 4000; repeat++)
        {
            input += "test test test test ";
        }
        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(input),
                                                   // dict is not used in this test
                                                   null , true);

        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();
        int previousOffest = -5;
        while (sl.getLemmatizeNextToken(token, results) > 0)
        {
            assertEquals(previousOffest, sl.getStartOffset() - 5);
            assertEquals(4, sl.getEndOffset() - sl.getStartOffset());
            previousOffest = sl.getStartOffset();
        }
    }

    @Test
    public void testHebrewWords() throws IOException
    {
        final String input = "שלום";
        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(input), getDictionary(), false);

        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        assertTrue(sl.getLemmatizeNextToken(token, results) > 0);
        assertEquals(3, results.size());
        assertEquals(0, sl.getStartOffset());
        assertEquals(4, sl.getEndOffset());
        results.clear();

        sl = new StreamLemmatizer(new StringReader("בבבי"), getDictionary(), false);
        assertTrue(sl.getLemmatizeNextToken(token, results) > 0);
        assertEquals(0, sl.getStartOffset());
        assertEquals(4, sl.getEndOffset());
        results.clear();
    }

    @Test
    public void testAutoStripMixed() throws IOException {
        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        testAutoStripMixedImpl("בcellcom", "cellcom", Tokenizer.TokenType.NonHebrew);
        testAutoStripMixedImpl("והcellcom", "cellcom", Tokenizer.TokenType.NonHebrew);
        testAutoStripMixedImpl("תחcellcom", "תחcellcom", Tokenizer.TokenType.Mixed | Tokenizer.TokenType.Hebrew);
        testAutoStripMixedImpl("הcellcomג", "הcellcomג", Tokenizer.TokenType.Mixed | Tokenizer.TokenType.Hebrew);
        testAutoStripMixedImpl("cellcom", "cellcom", Tokenizer.TokenType.NonHebrew);
    }

    private void testAutoStripMixedImpl(String word, String expected, int expectedType) throws IOException {
        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(word), getDictionary() , false);
        int tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expectedType, tokenType);
        assertEquals(expected, token.ref);

        sl = new StreamLemmatizer(new StringReader(word + " בדיקה"), getDictionary() , true);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expectedType, tokenType);
        assertEquals(expected, token.ref);

        sl = new StreamLemmatizer(new StringReader("בדיקה " + word), getDictionary() , true);
        sl.getLemmatizeNextToken(token, results);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expectedType, tokenType);
        assertEquals(expected, token.ref);
    }

    @Test
    public void testRespectsExactOperator() throws IOException {
        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        StreamLemmatizer sl = new StreamLemmatizer(new StringReader("בדיקה$"), getDictionary() , false);
        sl.setSuffixForExactMatch('$');
        int tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact, tokenType);
        assertEquals("בדיקה", token.ref);
        assertEquals(0, sl.getLemmatizeNextToken(token, results));
    }

    @Test
    public void testPreservesAcronyms() throws IOException {
        Reference<String> token = new Reference<>("");
        List<Token> results = new ArrayList<>();

        StreamLemmatizer sl = new StreamLemmatizer(new StringReader("מב\"ל"), getDictionary(), false);
        int tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Acronym | Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("מב\"ל", token.ref);

        sl = new StreamLemmatizer(new StringReader("ה\"מכונית"), getDictionary(), false);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("מכונית", token.ref);
    }



    // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
    // provide (a TODO in its own), and lastly based on custom lists.
    // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
}
