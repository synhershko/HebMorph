package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TermVectorsTest {
    static String text = "הוראה זו משקפת איזון בין זכות הנושה לגבות את חובו לבין זכותו של החייב ליהנות מקורת גג לו ולבני משפחתו בצה\"ל 2342 23דג asdשגכ דגכ234 שדגasd";

    class Data {
        public String term;
        public int startOffset, endOffset;
        public int positionIncGap, positionLength;
        public String tokenType;
        public boolean isKeyword = false;
    }

    @Test
    public void testTermVectors() throws IOException {
        Analyzer analyzer1 = TestBase.getHebrewIndexingAnalyzer();
        Analyzer analyzer2 = TestBase.getHebrewIndexingAnalyzerWithStandardTokenizer();

        ArrayList<Data> results1 = analyze(analyzer1);
        ArrayList<Data> results2 = analyze(analyzer2);

        for (int i = 0; i < Math.min(results1.size(), results2.size()); i++) {
            // assertEquals("token type mismatch in position " + i, results1.get(i).tokenType, results2.get(i).tokenType);
            assertEquals("term mismatch in position " + i, results1.get(i).term, results2.get(i).term);
            assertEquals("offset mismatch in position " + i, results1.get(i).startOffset, results2.get(i).startOffset);
            assertEquals("offset mismatch in position " + i, results1.get(i).endOffset, results2.get(i).endOffset);
            assertEquals("pos inc mismatch in position " + i, results1.get(i).positionIncGap, results2.get(i).positionIncGap);
            assertEquals("pos len mismatch in position " + i, results1.get(i).positionLength, results2.get(i).positionLength);
            assertEquals("keyword mismatch in position " + i, results1.get(i).isKeyword, results2.get(i).isKeyword);
        }

        assertEquals(results1.size(), results2.size());
    }

    private ArrayList<Data> analyze(Analyzer analyzer1) throws IOException {
        ArrayList<Data> results = new ArrayList<>(50);
        TokenStream ts = analyzer1.tokenStream("foo", text);
        ts.reset();
        while (ts.incrementToken()) {
            Data data = new Data();
            OffsetAttribute offsetAttribute = ts.getAttribute(OffsetAttribute.class);
            data.startOffset = offsetAttribute.startOffset();
            data.endOffset = offsetAttribute.endOffset();
            data.positionLength = ts.getAttribute(PositionLengthAttribute.class).getPositionLength();
            data.positionIncGap = ts.getAttribute(PositionIncrementAttribute.class).getPositionIncrement();
            data.tokenType = ts.getAttribute(HebrewTokenTypeAttribute.class).getType().toString();
            data.term = ts.getAttribute(CharTermAttribute.class).toString();

            if (ts.getAttribute(KeywordAttribute.class) != null)
                data.isKeyword = ts.getAttribute(KeywordAttribute.class).isKeyword();
            // System.out.println(data.term + " " + data.tokenType);
            results.add(data);
        }
        ts.close();

        return results;
    }
}
