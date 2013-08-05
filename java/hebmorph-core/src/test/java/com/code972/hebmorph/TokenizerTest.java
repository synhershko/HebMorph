package com.code972.hebmorph;

import org.apache.lucene.analysis.charfilter.BaseCharFilter;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer(null);

    @Before
    public void setUp() throws Exception {
        tokenizer.setSuffixForExactMatch('$');
    }

    private void assertTokenizesTo(String stream, String token) throws IOException {
        assertTokenizesTo(stream, token, 0);
    }

    private void assertTokenizesTo(String stream, String token, int tokenType) throws IOException {
        assertTokenizesTo(stream, new String[] { token }, tokenType == 0 ? null : new int[] {tokenType});
    }

    private void assertTokenizesTo(String stream, String[] tokens) throws IOException {
        assertTokenizesTo(stream, tokens, null);
    }

    private void assertTokenizesTo(String stream, String[] tokens, int[] tokenTypes) throws IOException {
        assert tokenTypes == null || tokens.length == tokenTypes.length;

        Reference<String> test = new Reference<String>("");
        tokenizer.reset(new StringReader(stream));

        int i = 0, tokenType;
        while ((tokenType = tokenizer.nextToken(test)) > 0) {
            assertEquals(tokens[i], test.ref);
            if (tokenTypes != null)
                assertEquals(tokenTypes[i], tokenType);
            i++;
        }
        assertEquals(tokens.length, i);
    }

    @Test
    public void tokenizesCorrectly() throws IOException {
        // NonHebrew
        assertTokenizesTo("test", "test");
        assertTokenizesTo("test's", "test's");
        assertTokenizesTo("tests'", "tests");
        assertTokenizesTo("test123", "test123");
        assertTokenizesTo("test two", new String[] { "test", "two" });

        // NonHebrew, non-English
        assertTokenizesTo("décimo", "décimo");
        assertTokenizesTo("traducción", "traducción");
        assertTokenizesTo("Úlcera", "Úlcera");
        assertTokenizesTo("ía", "ía");
        assertTokenizesTo("el árbol", new String[]{"el", "árbol"});

        assertTokenizesTo("בדיקה", "בדיקה");
        assertTokenizesTo("בדיקה.", "בדיקה");
        assertTokenizesTo("בדיקה..", "בדיקה");

        assertTokenizesTo("בדיקה שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה.שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה. שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה,שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה+שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה-שניה", new String[] {"בדיקה", "שניה"});
        assertTokenizesTo("בדיקה\u05BEשניה", new String[] {"בדיקה", "שניה"});

        assertTokenizesTo(" (\"דייט בחשיכה\",פרק 5) ", new String[] {"דייט", "בחשיכה", "פרק", "5"});

        assertTokenizesTo("בדיקה\"", "בדיקה");

        assertTokenizesTo("\u05AAבדיקה", "בדיקה"); // ignores leading niqqud (invalid case)
        assertTokenizesTo("\u05AAבדיקה..", "בדיקה");
        assertTokenizesTo("ב\u05B0דיקה", "ב\u05B0דיקה"); // doesn't strip Niqqud
        //assertTokenizesTo("ב\u05A0דיקה", "ב\u05A0דיקה"); // ignores Taamei Mikra

        assertTokenizesTo("ץבדיקה", "בדיקה");

        assertTokenizesTo("שלומי999", "שלומי999");
        assertTokenizesTo("שלומיabc", "שלומיabc");
        assertTokenizesTo("אימג’בנק", "אימג'בנק");

        assertTokenizesTo("בלונים$", "בלונים", Tokenizer.TokenType.Hebrew | Tokenizer.TokenType.Exact);
        assertTokenizesTo("test$", "test", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Exact );
        assertTokenizesTo("123$", "123", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Numeric | Tokenizer.TokenType.Exact );

        // Gershayim unification
        assertTokenizesTo("צה\"ל", "צה\"ל");
        assertTokenizesTo("צה''ל", "צה\"ל");
        assertTokenizesTo("צה\u05F3\u05F3ל", "צה\"ל");
        assertTokenizesTo("צה\u201Cל", "צה\"ל");

        // Geresh
        assertTokenizesTo("ד'אור", "ד'אור");
        assertTokenizesTo("אורנג'", "אורנג'");
        assertTokenizesTo("אורנג\u05F3", "אורנג'");
        assertTokenizesTo("אורנג' שלום",  new String[] {"אורנג'", "שלום"});
        assertTokenizesTo("סמית'", "סמית");

        assertTokenizesTo("ומש\"א$", "ומש\"א$");
    }

    @Test
    public void tokenizesWithExceptions() throws IOException {
        tokenizesCorrectly();

        assertTokenizesTo("C++", "C");
        assertTokenizesTo("C++ ", "C");
        tokenizer.addSpecialCase("C++");
        assertTokenizesTo("C++", "C++", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Custom);
        assertTokenizesTo("c++", "c++", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Custom);
        assertTokenizesTo("C++ ", "C++", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Custom);
        assertTokenizesTo("c++ ", "c++", Tokenizer.TokenType.NonHebrew | Tokenizer.TokenType.Custom);
        assertTokenizesTo("בC++", "בC++");
        assertTokenizesTo("בC++ ", "בC++");
        assertTokenizesTo("C++x0", new String[] { "C", "x0" });
        assertTokenizesTo("C++x0 ", new String[] { "C", "x0" });

        assertTokenizesTo("B+++", "B");
        tokenizer.addSpecialCase("B+++");
        assertTokenizesTo("B+++", "B+++");
        assertTokenizesTo("B+++x0", new String[] { "B", "x0" });


        assertTokenizesTo("שלום+", "שלום");
        tokenizer.addSpecialCase("שלום+");
        assertTokenizesTo("שלום+", "שלום+");
        assertTokenizesTo("שלום", "שלום");
        assertTokenizesTo("שלום+בדיקה", new String[] { "שלום", "בדיקה" });

        tokenizesCorrectly();
    }

    @Test
    public void incrementsOffsetCorrectly() throws FileNotFoundException, IOException
    {
        int[] expectedOffsets = { 0, 5, 10, 15 };
        int curPos = 0;

        Reference<String> token = new Reference<String>("");
        tokenizer.reset(new StringReader("test test test test"));
        while (true)
        {
            int token_type = tokenizer.nextToken(token);
            if (token_type == 0)
                break;

            assertEquals(expectedOffsets[curPos++], tokenizer.getOffset());
            assertEquals(4, tokenizer.getLengthInSource());
        }
    }

    @Test
    public void IncrementsOffsetCorrectlyWithAnotherReader() throws IOException {
        int[] expectedOffsets = { 0, 5, 10, 15 };
        int curPos = 0;

        Tokenizer t = new Tokenizer(
                        new HTMLStripCharFilter(new StringReader("test <a href=\"foo\">test</a> test test"))
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

        BaseCharFilter filter = new HTMLStripCharFilter(new StringReader(input));
        Tokenizer t = new Tokenizer(filter);

        Reference<String> token = new Reference<String>("");

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
    public void IncrementsOffsetCorrectlyAlsoWhenBuffered() throws IOException
    {
        Reference<String> token = new Reference<String>("");

        String input = "";
        for (int repeat = 0; repeat < 4000; repeat++)
        {
            input += "test test test test ";
        }

        tokenizer.reset(new StringReader(input));
        int previousOffest = -5;
        while (true)
        {
            int token_type = tokenizer.nextToken(token);
            if (token_type == 0)
                break;

            assertEquals(previousOffest, tokenizer.getOffset() - 5);
            assertEquals(4, tokenizer.getLengthInSource());
            previousOffest = tokenizer.getOffset();
        }
    }

    @Test
    public void IncrementsOffsetCorrectlyWithTerminatingGeresh() throws IOException {
        final String input = "מ'מפלגות המרכז' מפגש'";

        final Reference<String> test = new Reference<String>("");
        tokenizer.reset(new StringReader(input));
        tokenizer.nextToken(test);
        assertEquals("מ'מפלגות", test.ref);
        assertEquals(0, tokenizer.getOffset());
        assertEquals(8, tokenizer.getLengthInSource());

        tokenizer.nextToken(test);
        assertEquals("המרכז'", test.ref);
        assertEquals(9, tokenizer.getOffset());
        assertEquals(6, tokenizer.getLengthInSource());

        tokenizer.nextToken(test);
        assertEquals("מפגש", test.ref);
        assertEquals(16, tokenizer.getOffset());
        assertEquals(4, tokenizer.getLengthInSource());
    }

    @Test
    public void DiscardsSurroundingGershayim() throws FileNotFoundException, IOException {
        final Reference<String> test = new Reference<String>("");

        tokenizer.reset(new StringReader("\"צבא\""));
        tokenizer.nextToken(test);
        assertEquals("צבא", test.ref);
        assertEquals(3, tokenizer.getLengthInSource());
        assertEquals(1, tokenizer.getOffset());
    }

    @Test
    public void longTokenTest() throws IOException {
        String text = "רפאלולדןהואפרופסורלרפואהישראלימלמדבאוניברסיטתתלאביבסגןמנהלביתהחוליםשיבאופעילחברתימתמחהבכירוגיהכלליתובכלידם"+
      						"ולדןנולדבצרפתועלהלישראלבגילהואשימשבביתהחוליםשיבאכמנהלהאגףכירורגיהומנהלהיחידהלכלידם"+
      						"ולדןפעילוחברבהנהלהבעמותתרופאיםלזכויותאדםוכמוכןחברבהנהלתארגוןלתתולדןזכהבאותלגיוןהכבודהצרפתישלממשלתצרפתבזכותעלפעילותובמסגרתרופאיםלזכויותאדםלקידוםשיתוףהפעולהביןפלסטיניםלישראליםהאותהוענקלועלידישרהחוץשלצרפתרנארקושנרבטקסבשגרירותצרפתבתלאביב"+
      						"נשוילבלשניתצביהולדןבתושלשמעוןפרסוהואמשמשכרופאוהאישישלפרס.";


        Tokenizer tokenizer = new Tokenizer(null);
        Reference<String> test = new Reference<String>("");
        tokenizer.reset(new StringReader(text));

        while (tokenizer.nextToken(test) > 0) {
        }

        assertTrue("Arrived here without throwing", true);
    }

}
