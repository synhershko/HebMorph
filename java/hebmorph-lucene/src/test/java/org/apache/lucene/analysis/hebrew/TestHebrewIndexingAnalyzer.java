package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;

import java.io.IOException;

/**
 * Created by synhershko on 22/06/14.
 */
public class TestHebrewIndexingAnalyzer extends BaseTokenStreamTestCase {
    public void testDictionaryLoaded() throws IOException {
        HebrewAnalyzer a = HSpellLoader.getHebrewIndexingAnalyzer();
        assertEquals(HebrewAnalyzer.WordType.HEBREW, a.isRecognizedWord("אימא", false));
        assertEquals(HebrewAnalyzer.WordType.HEBREW, a.isRecognizedWord("בדיקה", false));
        assertEquals(HebrewAnalyzer.WordType.UNRECOGNIZED, a.isRecognizedWord("ץץץץץץ", false));
    }

    public void testBasics() throws IOException {
        Analyzer a = HSpellLoader.getHebrewIndexingAnalyzer();

        assertAnalyzesTo(a, "אימא", new String[]{"אימא$", "אימא"}); // recognized word, lemmatized
        assertAnalyzesTo(a, "אימא$", new String[]{"אימא$", "אימא"}); // recognized word, lemmatized
        assertAnalyzesTo(a, "בדיקהבדיקה", new String[]{"בדיקהבדיקה$", "בדיקהבדיקה"}); // OOV
        assertAnalyzesTo(a, "בדיקהבדיקה$", new String[]{"בדיקהבדיקה$", "בדיקהבדיקה"}); // OOV
        assertAnalyzesTo(a, "ץץץץץץץץץץץ", new String[]{}); // Invalid, treated as noise
        assertAnalyzesTo(a, "ץץץץץץץץץץץ$", new String[]{}); // Invalid, treated as noise

        assertAnalyzesTo(a, "אנציקלופדיה", new String[]{"אנציקלופדיה$", "אנציקלופדיה"});
        assertAnalyzesTo(a, "אנצקלופדיה", new String[]{"אנצקלופדיה$", "אנציקלופדיה"});

        assertAnalyzesTo(a, "שמלות", new String[]{"שמלות$", "שמלה", "מל"});

        // Test non-Hebrew
        assertAnalyzesTo(a, "book", new String[]{"book$", "book"});
        assertAnalyzesTo(a, "book$", new String[]{"book$", "book"});
        assertAnalyzesTo(a, "steven's", new String[]{"steven's$", "steven's"});
        assertAnalyzesTo(a, "steven\u2019s", new String[]{"steven's$", "steven's"});
        //assertAnalyzesTo(a, "steven\uFF07s", new String[]{"steven's$", "steven's"});
        checkOneTerm(a, "3", "3");
    }
}
