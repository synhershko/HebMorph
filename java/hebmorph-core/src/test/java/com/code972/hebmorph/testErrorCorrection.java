package com.code972.hebmorph;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class testErrorCorrection extends TestBase {

    private final Lemmatizer _lemmatizer;

    public testErrorCorrection() throws IOException {
        _lemmatizer = new Lemmatizer(getDictionary(), false);
    }

    @Test
    public void SimpleAHVICases()
    {
        // AssertWord("פינגוין", "פינגווין"); // TODO
    }

    private void AssertWord(String word, String expectedWord)
    {
        assertTrue(_lemmatizer.lemmatize(expectedWord).size() > 0); // make sure the expected word is legal
        List<HebrewToken> results = _lemmatizer.lemmatizeTolerant(word);
        assertTrue(results.size() > 0);
        assertEquals(expectedWord, results.get(0).getText());
    }
}
