package org.apache.lucene.analysis.hebrew.TokenFilters;

import com.code972.hebmorph.HebrewUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hebrew.HebrewTokenTypeAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class MarkHebrewTokensFilter extends TokenFilter {
    /**
     * Construct a token stream filtering the given input.
     */
    public MarkHebrewTokensFilter(TokenStream input) {
        super(input);
    }

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final HebrewTokenTypeAttribute hebTypeAtt = addAttribute(HebrewTokenTypeAttribute.class);

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) { // reached EOS
            return false;
        }

        final char[] buffer = termAtt.buffer();
        for (int i = 0; i < termAtt.length(); i++) {
            if (!HebrewUtils.isHebrewLetter(buffer[i]) && !HebrewUtils.isNiqqudChar(buffer[i])
                    && !HebrewUtils.isOfChars(buffer[i], HebrewUtils.Gershayim)
                    && !HebrewUtils.isOfChars(buffer[i], HebrewUtils.Geresh)) {

                // This behavior is maintaining backwards compatibility with the HebrewTokenizer class,
                // and also as a byproduct enables usage of WordDelimiterTokenFilter for mixed tokens
                if (Character.isDigit(buffer[0]))
                    hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Numeric);
                else
                    hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.NonHebrew);

                return true;
            }
        }

        hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Hebrew);

        return true;
    }
}
