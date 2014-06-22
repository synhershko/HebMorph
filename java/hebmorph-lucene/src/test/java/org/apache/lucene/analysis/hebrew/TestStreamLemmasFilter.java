package org.apache.lucene.analysis.hebrew;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.code972.hebmorph.hspell.LingInfo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by synhershko on 20/06/14.
 */
public class TestStreamLemmasFilter extends BaseTokenStreamWithDictionaryTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            Tokenizer src = null;
            try {
                src = new StreamLemmasFilter(reader, getDictionary(), LingInfo.buildPrefixTree(false));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void testLemmatization() throws IOException {
        assertAnalyzesTo(a, "בדיקה", new String[]{"בדיקה"}, new int[] {0}, new int[]{5});
        assertAnalyzesTo(a, "בדיקות", new String[]{"בדיקה"}, new int[] {0}, new int[]{6});
        assertAnalyzesTo(a, "אימא", new String[]{"אימא"}, new int[] {0}, new int[]{4});
    }
}
