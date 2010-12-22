package hebmorph;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamLemmatizerTest
{
    private static final String DEFAULT_HSPELL_PATH = "../../hspell-data-files";
    static String hspellPath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		hspellPath = System.getProperty("hspellPath",DEFAULT_HSPELL_PATH);
	}

    @Before
    public void SetUp()
    {
        if(hspellPath == null)
            hspellPath = DEFAULT_HSPELL_PATH;
    }

    @Test
    public void IncrementsOffsetCorrectly() throws IOException
    {
        String input = "";
        for (int repeat = 0; repeat < 4000; repeat++)
        {
            input += "test test test test ";
        }
        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(input));

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

    // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
    // provide (a TODO in its own), and lastly based on custom lists.
    // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
}
