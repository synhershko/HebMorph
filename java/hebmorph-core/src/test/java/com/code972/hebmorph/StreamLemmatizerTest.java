package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
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

        DictRadix<Byte> specialTokenizationCases = new DictRadix<>(false);
        specialTokenizationCases.addNode("C++", new Byte((byte)0));
        testAutoStripMixedImpl("בc++", "c++", Tokenizer.TokenType.NonHebrew, specialTokenizationCases);
        testAutoStripMixedImpl("בc++ ", "c++", Tokenizer.TokenType.NonHebrew, specialTokenizationCases);
        testAutoStripMixedImpl(" בc++", "c++", Tokenizer.TokenType.NonHebrew, specialTokenizationCases);
    }

    private void testAutoStripMixedImpl(String word, String expected, int expectedType) throws IOException {
        testAutoStripMixedImpl(word, expected, expectedType, null);
    }

    private void testAutoStripMixedImpl(String word, String expected, int expectedType, DictRadix<Byte> specialTokenizationCases) throws IOException {
        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(word), getDictionary() , false, specialTokenizationCases);
        int tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expected, token.ref);
        assertEquals(expectedType, tokenType);

        sl = new StreamLemmatizer(new StringReader(word + " בדיקה"), getDictionary() , true, specialTokenizationCases);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expected, token.ref);
        assertEquals(expectedType, tokenType);

        sl = new StreamLemmatizer(new StringReader("בדיקה " + word), getDictionary() , true, specialTokenizationCases);
        sl.getLemmatizeNextToken(token, results);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(expected, token.ref);
        assertEquals(expectedType, tokenType);
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

        sl = new StreamLemmatizer(new StringReader("בדיקות$"), getDictionary() , false);
        sl.setSuffixForExactMatch('$');
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact, tokenType);
        assertEquals("בדיקות", token.ref);
        assertEquals(0, sl.getLemmatizeNextToken(token, results));

        sl = new StreamLemmatizer(new StringReader("\"בין$ תחומי$\""), getDictionary() , false);
        sl.setSuffixForExactMatch('$');
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact, tokenType);
        assertEquals("בין", token.ref);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact, tokenType);
        assertEquals("תחומי", token.ref);

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

        sl = new StreamLemmatizer(new StringReader("מב\"ל"), getDictionary(), false);
        sl.setSuffixForExactMatch('$');
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Acronym | Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("מב\"ל", token.ref);

        sl = new StreamLemmatizer(new StringReader("ומש\"א"), getDictionary(), false);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Acronym | Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("ומש\"א", token.ref);

        sl = new StreamLemmatizer(new StringReader("ומש\"א"), getDictionary(), false);
        sl.setSuffixForExactMatch('$');
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Acronym | Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("ומש\"א", token.ref);

        sl = new StreamLemmatizer(new StringReader("ומש\"א$"), getDictionary(), false);
        sl.setSuffixForExactMatch('$');
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals("ומש\"א", token.ref);
        assertEquals(Tokenizer.TokenType.Acronym | Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact, tokenType);

        sl = new StreamLemmatizer(new StringReader("ה\"מכונית"), getDictionary(), false);
        tokenType = sl.getLemmatizeNextToken(token, results);
        assertEquals(Tokenizer.TokenType.Hebrew, tokenType);
        assertEquals("מכונית", token.ref);
    }



    // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
    // provide (a TODO in its own), and lastly based on custom lists.
    // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
}
