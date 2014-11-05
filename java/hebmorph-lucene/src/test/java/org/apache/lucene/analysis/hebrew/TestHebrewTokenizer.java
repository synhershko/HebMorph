package org.apache.lucene.analysis.hebrew;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by synhershko on 19/06/14.
 */
public class TestHebrewTokenizer extends BaseTokenStreamTestCase {

    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            final HebrewTokenizer src = new HebrewTokenizer(reader, HSpellLoader.readDefaultPrefixes());
            return new Analyzer.TokenStreamComponents(src);
        }
    };

    /** blast some random strings through the analyzer */
    public void testRandomStrings() throws Exception {
        checkRandomData(random(), a, 1000*RANDOM_MULTIPLIER);
    }

    /** test stopwords and stemming */
    @Repeat(iterations = 100)
    public void testBasics() throws IOException {
        checkOneTerm(a, "books", "books");
        checkOneTerm(a, "book", "book");
        checkOneTerm(a, "steven's", "steven's");
        checkOneTerm(a, "steven\u2019s", "steven's");
        //checkOneTerm(a, "steven\uFF07s", "steven's");

        checkOneTerm(a, "בדיקה", "בדיקה");
        checkOneTerm(a, "צה\"ל", "צה\"ל");
        checkOneTerm(a, "צה''ל", "צה\"ל");

        checkAnalysisConsistency(random(), a, true, "בדיקה אחת שתיים", true);
    }

    public void testHyphen() throws Exception {
        assertTokenStreamContents(tokenStream("some-dashed-phrase"),
                new String[] { "some", "dashed", "phrase" });
    }

    TokenStream tokenStream(String text) throws IOException {
        Reader reader = new StringReader(text);
        return a.tokenStream("foo", reader);
    }
}
