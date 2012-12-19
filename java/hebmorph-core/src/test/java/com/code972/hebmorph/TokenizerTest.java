package com.code972.hebmorph;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TokenizerTest {
    private Tokenizer getTokenizer(String input) throws FileNotFoundException {
        return new Tokenizer(new StringReader(input));
    }

    private void assertTokenizesTo(String stream, String token) throws IOException {
        assertTokenizesTo(stream, new String[] { token });
    }

    private void assertTokenizesTo(String stream, String[] tokens) throws IOException {
        Reference<String> test = new Reference<String>("");
        Tokenizer t = getTokenizer(stream);

        int i = 0;
        while (t.nextToken(test) > 0) {
            assertEquals(tokens[i++], test.ref);
        }
        assertEquals(tokens.length, i);
    }

    @Test
    public void tokenizesCorrectly() throws IOException {
        assertTokenizesTo("בדיקה", "בדיקה");
        assertTokenizesTo("בדיקה.", "בדיקה");
        assertTokenizesTo("בדיקה..", "בדיקה");

        assertTokenizesTo("בדיקה שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה.שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה. שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה,שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה+שניה", new String[] {"בדיקה", "שניה"});

        assertTokenizesTo("בדיקה\"", "בדיקה");

        assertTokenizesTo("\u05AAבדיקה", "בדיקה"); // ignores leading niqqud (invalid case)
        assertTokenizesTo("\u05AAבדיקה..", "בדיקה");
        assertTokenizesTo("ב\u05B0דיקה", "ב\u05B0דיקה"); // doesn't strip Niqqud
        //assertTokenizesTo("ב\u05A0דיקה", "ב\u05A0דיקה"); // ignores Taamei Mikra

        assertTokenizesTo("ץבדיקה", "בדיקה");

        assertTokenizesTo("שלומי999", "שלומי999");
        assertTokenizesTo("שלומיabc", "שלומיabc");

        //assertTokenizesTo("בלונים$", "בלונים$");

        // Gershayim unification
        assertTokenizesTo("צה\"ל", "צה\"ל");
        assertTokenizesTo("צה''ל", "צה\"ל");
        assertTokenizesTo("צה\u05F3\u05F3ל", "צה\"ל");
    }

    @Test
    public void incrementsOffsetCorrectly() throws FileNotFoundException, IOException
    {
        int[] expectedOffsets = { 0, 5, 10, 15 };
        int curPos = 0;

        Reference<String> token = new Reference<String>("");
        Tokenizer t = getTokenizer("test test test test");
        while (true)
        {
            int token_type = t.nextToken(token);
            if (token_type == 0)
                break;

            assertEquals(expectedOffsets[curPos++], t.getOffset());
            assertEquals(4, t.getLengthInSource());
        }
    }

    @Test
    public void IncrementsOffsetCorrectlyWithAnotherReader() throws IOException {
        int[] expectedOffsets = { 0, 5, 10, 15 };
        int curPos = 0;

        Tokenizer t = new Tokenizer(
                        new HTMLStripCharFilter(CharReader.get(new StringReader("test <a href=\"foo\">test</a> test test")))
        );

        Reference<String> ref = new Reference<String>("");
        while (true)
        {
            int token_type = t.nextToken(ref);
            if (token_type == 0)
                break;

            assertEquals(expectedOffsets[curPos++], t.getOffset());
            assertEquals(4, t.getLengthInSource());
        }
    }

    @Test
    public void IncrementsOffsetCorrectlyWithAnotherReader2() throws IOException {
        String input = "test1 <a href=\"foo\">testlink</a> test2 test3";

        CharFilter filter = new HTMLStripCharFilter(CharReader.get(new StringReader(input)));
        Tokenizer t = new Tokenizer(filter);

        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        t.nextToken(token);
        assertEquals(0, filter.correctOffset(t.getOffset()));
        assertEquals(5, t.getLengthInSource());

        t.nextToken(token);
        assertEquals(20, filter.correctOffset(t.getOffset()));
        assertEquals(8, t.getLengthInSource());

        t.nextToken(token);
        assertEquals(33, filter.correctOffset(t.getOffset()));
        assertEquals(5, t.getLengthInSource());

        t.nextToken(token);
        assertEquals(39, filter.correctOffset(t.getOffset()));
        assertEquals(5, t.getLengthInSource());
    }

    @Test
    public void IncrementsOffsetCorrectlyAlsoWhenBuffered() throws FileNotFoundException, IOException
    {
        Reference<String> token = new Reference<String>("");

        String input = "";
        for (int repeat = 0; repeat < 4000; repeat++)
        {
            input += "test test test test ";
        }

        Tokenizer t = getTokenizer(input);
        int previousOffest = -5;
        while (true)
        {
            int token_type = t.nextToken(token);
            if (token_type == 0)
                break;

            assertEquals(previousOffest, t.getOffset() - 5);
            assertEquals(4, t.getLengthInSource());
            previousOffest = t.getOffset();
        }
    }

    @Test
    public void DiscardsSurroundingGershayim() throws FileNotFoundException, IOException {
        Reference<String> test = new Reference<String>("");

        Tokenizer t = getTokenizer("\"צבא\"");
        t.nextToken(test);
        assertEquals("צבא", test.ref);
        assertEquals(3, t.getLengthInSource());
        assertEquals(1, t.getOffset());
    }
}
