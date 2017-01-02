package org.apache.lucene.analysis.hebrew.TokenFilters;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hebrew.HebrewTokenTypeAttribute;

import java.io.IOException;

/**
 * Created by Egozy on 21/04/2015.
 */
public class IgnoreOriginalTokenFilter extends FilteringTokenFilter {

    private final HebrewTokenTypeAttribute hebTypeAtt = addAttribute(HebrewTokenTypeAttribute.class);

    public IgnoreOriginalTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    protected boolean accept() throws IOException {
        // basically, ignore original word only if it's a hebrew word;
        return !hebTypeAtt.isHebrew() || hebTypeAtt.isExact();
    }
}
