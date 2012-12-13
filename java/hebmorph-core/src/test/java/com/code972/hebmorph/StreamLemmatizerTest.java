package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Loader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StreamLemmatizerTest
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
        final String DEFAULT_HSPELL_PATH = "file:///home/synhershko/code/oscode/HebMorph/hspell-data-files";
        DictRadix<MorphData> dict = Loader.loadDictionaryFromUrl(DEFAULT_HSPELL_PATH, true);

        final String input = "יבגני";
        StreamLemmatizer sl = new StreamLemmatizer(new StringReader(input), dict , false);

        Reference<String> token = new Reference<String>("");
        List<Token> results = new ArrayList<Token>();

        assertTrue(sl.getLemmatizeNextToken(token, results) > 0);
        assertEquals(1, results.size());
    }

    // TODO: RemovesObviousStopWords: first collations, then based on morphological data hspell needs to
    // provide (a TODO in its own), and lastly based on custom lists.
    // We cannot just remove all HebrewToken.Mask == 0, since this can also mean private names and such...
}
