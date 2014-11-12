package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;

public abstract class AddSuffixFilter extends TokenFilter {
    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    protected final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
    protected final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final Character suffix;
    private int latestStartOffset, latestEndOffset;
    private String latestType;

    public AddSuffixFilter(final TokenStream input, final Character suffixToAdd) {
        super(input);
        this.suffix = suffixToAdd;
    }

    private char[] tokenBuffer = new char[Byte.MAX_VALUE];
    private int tokenLen = 0;

    @Override
    public final boolean incrementToken() throws IOException {
        if (tokenLen > 0) {
            clearAttributes();

            termAtt.resizeBuffer(tokenLen);
            System.arraycopy(tokenBuffer, 0, termAtt.buffer(), 0, tokenLen);
            termAtt.setLength(tokenLen);
            tokenLen = 0;

            posIncAtt.setPositionIncrement(0); // since we are just putting the original now
            offsetAtt.setOffset(latestStartOffset, latestEndOffset);
            typeAtt.setType(latestType);
            return true;
        }

        if (!input.incrementToken()) { // reached EOS -- return null
            return false;
        }

        // If the filter is disabled or this token was created by a previous filter, skip
        if (suffix == null) {
            return true;
        }

        handleCurrentToken();

        return true;
    }

    protected abstract void handleCurrentToken();

    protected void suffixCurrent() {
        termAtt.append(suffix);
        keywordAtt.setKeyword(true);
    }

    protected final void duplicateCurrentToken() {
        tokenLen = termAtt.length();
        if (tokenBuffer == null || tokenBuffer.length < tokenLen)
            tokenBuffer = termAtt.buffer().clone();
        else
            System.arraycopy(termAtt.buffer(), 0, tokenBuffer, 0, tokenLen);
        latestEndOffset = offsetAtt.endOffset();
        latestStartOffset = offsetAtt.startOffset();
        latestType = typeAtt.type();
    }
}
