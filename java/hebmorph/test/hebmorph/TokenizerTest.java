package hebmorph;

import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenizerTest
{
    private Tokenizer getTokenizer(String input) throws FileNotFoundException
    {
        return new Tokenizer(new StringReader(input));
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
    public void UnifiesGershayimCorrectly() throws FileNotFoundException, IOException
    {
        Reference<String> test = new Reference<String>("");

        Tokenizer t = getTokenizer("צה''ל");
        t.nextToken(test);
        assertEquals("צה\"ל", test.ref);

        t = getTokenizer("צה\u05F3\u05F3ל");
        t.nextToken(test);
        assertEquals("צה\"ל", test.ref);
    }

    @Test
    public void DiscardsSurroundingGershayim() throws FileNotFoundException, IOException
    {
        Reference<String> test = new Reference<String>("");

        Tokenizer t = getTokenizer("\"צבא\"");
        t.nextToken(test);
        assertEquals("צבא", test.ref);
        assertEquals(3, t.getLengthInSource());
        assertEquals(1, t.getOffset());
    }
}
