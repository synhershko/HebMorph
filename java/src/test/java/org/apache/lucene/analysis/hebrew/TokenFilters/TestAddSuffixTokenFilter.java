package org.apache.lucene.analysis.hebrew.TokenFilters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.BaseTokenStreamWithDictionaryTestCase;
import org.apache.lucene.analysis.hebrew.HebrewTokenizer;
import org.apache.lucene.analysis.hebrew.TestBase;

import java.io.IOException;
import java.io.Reader;

public class TestAddSuffixTokenFilter extends BaseTokenStreamWithDictionaryTestCase {
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
            return new Analyzer.TokenStreamComponents(t, new AddSuffixTokenFilter(t, '$'));
        }
    };

    public void testBasicTerms() throws IOException {
        assertAnalyzesTo(a, "book", new String[]{"book$"});
        assertAnalyzesTo(a, "שלום", new String[]{"שלום$"});
        assertAnalyzesTo(a, "בי\"ס", new String[]{"בי\"ס$"});
        assertAnalyzesTo(a, "57", new String[]{"57"});
    }
}
