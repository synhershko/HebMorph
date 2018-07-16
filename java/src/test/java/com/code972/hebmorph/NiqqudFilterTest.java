package com.code972.hebmorph;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.TokenFilters.NiqqudFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class NiqqudFilterTest extends BaseTokenStreamTestCase {
    public class NiqqudTestAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer src = new StandardTokenizer();
            TokenStream filter = new NiqqudFilter(src);
            return new TokenStreamComponents(src, filter);
        }
    }

    @Test
    public void testRemoveNiqqud() throws IOException {
        final NiqqudTestAnalyzer a = new NiqqudTestAnalyzer();
        assertAnalyzesTo(a, "תָּכְנִית מַבְרִיקָה", new String[] {"תכנית","מבריקה"});
    }

    @Test
    public void testOffsets() throws Exception {
        // Niqqud characters occupy space and need to be taken into account
        try (Analyzer analyzer = new NiqqudTestAnalyzer();
             TokenStream stream = analyzer.tokenStream("foo", new StringReader("תָּכְנִית מַבְרִיקָה"))) {
            OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
            stream.reset();
            assertTrue(stream.incrementToken());
            assertEquals(0, offsetAtt.startOffset());
            assertEquals(9, offsetAtt.endOffset());
            assertTrue(stream.incrementToken());
            assertEquals(10, offsetAtt.startOffset());
            assertEquals(20, offsetAtt.endOffset());
            assertFalse(stream.incrementToken());
            stream.end();
        }
    }

    /**
     * blast some random strings through the test analyzer
     */
    public void testRandomStrings() throws Exception {
        Analyzer analyzer = new NiqqudTestAnalyzer();
        checkRandomData(random(), analyzer, 1000 * RANDOM_MULTIPLIER);
        analyzer.close();
    }
}