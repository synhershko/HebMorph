package org.apache.lucene.analysis.hebrew.TokenFilters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.BaseTokenStreamWithDictionaryTestCase;
import org.apache.lucene.analysis.hebrew.HebrewTokenizer;
import org.apache.lucene.analysis.hebrew.TestBase;

import java.io.IOException;
import java.io.Reader;

public class TestIgnoreOriginalTokenFilter extends BaseTokenStreamWithDictionaryTestCase {
    Analyzer a = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = null;
            try {
                t = new HebrewTokenizer(reader, getDictionary().getPref());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TokenStreamComponents(t, new IgnoreOriginalTokenFilter(t));
        }
    };

    public void testBasicTerms() throws IOException {
        assertAnalyzesTo(a, "book", new String[]{"book"});
        assertAnalyzesTo(a, "שלום", new String[]{});
        assertAnalyzesTo(a, "בי\"ס", new String[]{});
        assertAnalyzesTo(a, "57", new String[]{"57"});
    }

    Analyzer a2 = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName,
                                                         Reader reader) {
            Tokenizer t = null;
            TokenStream tok = null;
            try {
                t = new HebrewTokenizer(reader, getDictionary().getPref());
                tok = new IgnoreOriginalTokenFilter(new HebrewLemmatizerTokenFilter(t,getDictionary()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new TokenStreamComponents(t,tok);

        }
    };

    public void testBasicStream() throws IOException {
        assertAnalyzesTo(a2, "book", new String[]{"book", "book"});
        assertAnalyzesTo(a2, "שלום", new String[]{"שלום", "שלום"});
        assertAnalyzesTo(a2, "בי\"ס", new String[]{"בי\"ס"});
        assertAnalyzesTo(a2, "57", new String[]{"57"});
        assertAnalyzesTo(a2, "בדיקה אחת שתיים שולחן", new String[]{"בדיקה","אחד","שניים", "שולחן", "שולח"});
    }
}
