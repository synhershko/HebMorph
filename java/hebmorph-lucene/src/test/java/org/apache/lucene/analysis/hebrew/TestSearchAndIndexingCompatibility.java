package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.fail;

/**
 * Created by Itamar on 26-Jun-14.
 */
public class TestSearchAndIndexingCompatibility {

    @Test
    public void testBasic() throws IOException {
        indexAndSearch("אמא", "אמא");
        indexAndSearch("אימא", "אמא");
        indexAndSearch("אמא", "אימא");
        indexAndSearch("אינציקלופדיה", "אינציקלופדיה");
        indexAndSearch("אנציקלופדיה", "אנציקלופדיה");
        indexAndSearch("אנצקלופדיה", "אנציקלופדיה");
        indexAndSearch("אנציקלופדיה", "אנצקלופדיה");
    }

    private static void indexAndSearch(String indexingTerm, String searchTerm) throws IOException {
        Analyzer indexingAnalyzer = HSpellLoader.getHebrewIndexingAnalyzer();
        //Analyzer searchAnalyzer = new HebrewIndexingAnalyzer();
        Analyzer searchAnalyzer = HSpellLoader.getHebrewQueryAnalyzer();

        HashSet<String> indexedTerms = new HashSet<>();
        TokenStream ts = indexingAnalyzer.tokenStream("foo", indexingTerm);
        ts.reset();
        while (ts.incrementToken()){
            CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
            System.out.println(termAtt.toString());
            indexedTerms.add(termAtt.toString());
        }
        ts.close();

        System.out.println("Search:");

        HashSet<String> searchTerms = new HashSet<>();
        ts = searchAnalyzer.tokenStream("foo", searchTerm);
        ts.reset();
        while (ts.incrementToken()){
            CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
            System.out.println(termAtt.toString());
            searchTerms.add(termAtt.toString());
        }
        ts.close();

        for (final String term : searchTerms) {
            if (indexedTerms.contains(term))
                return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Search term ");
        sb.append(searchTerm);
        sb.append(" couldn't be found in indexed terms produced by ");
        sb.append(indexingTerm);
        sb.append("\n\tIndexed terms:\t");
        sb.append(indexedTerms);
        sb.append("\n\tSearch terms:\t");
        sb.append(searchTerms);

        fail(sb.toString());
    }
}
