/*
 * Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.apache.lucene.analysis;

import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;

/*
 * TODO: Consider implementing https://issues.apache.org/jira/browse/LUCENE-1688 changes to stop list and associated constructors
 */

/**
 * Construct bigrams for frequently occurring terms while indexing. Single terms
 * are still indexed too, with bigrams overlaid. This is achieved through the
 * use of {@link PositionIncrementAttribute#setPositionIncrement(int)}. Bigrams have a type
 * of {@link #GRAM_TYPE} Example:
 * <ul>
 * <li>input:"the quick brown fox"</li>
 * <li>output:|"the","the-quick"|"brown"|"fox"|</li>
 * <li>"the-quick" has a position increment of 0 so it is in the same position
 * as "the" "the-quick" has a term.type() of "gram"</li>
 * <p/>
 * </ul>
 */

/*
 * Constructors and makeCommonSet based on similar code in StopFilter
 */
public final class CommonGramsFilter extends TokenFilter {

    public static final String GRAM_TYPE = "gram";
    private static final char SEPARATOR = '_';

    private final CharArraySet commonWords;
    private final boolean keepOrigin;

    private final StringBuilder buffer = new StringBuilder();

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
    private final PositionLengthAttribute posLenAttribute = addAttribute(PositionLengthAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    private int lastStartOffset;
    private boolean lastWasCommon;
    private State savedState;

    /**
     * Construct a token stream filtering the given input using a Set of common
     * words to create bigrams. Outputs both unigrams with position increment and
     * bigrams with position increment 0 type=gram where one or both of the words
     * in a potential bigram are in the set of common words .
     *
     * @param input       TokenStream input in filter chain
     * @param commonWords The set of common words.
     * @deprecated use the one with keepOrigin
     */
    @Deprecated
    public CommonGramsFilter(Version matchVersion, TokenStream input, CharArraySet commonWords) {
        this(matchVersion, input, commonWords, true);
    }

    /**
     * Construct a token stream filtering the given input using a Set of common
     * words to create bigrams. Outputs both unigrams with position increment and
     * bigrams with position increment 0 type=gram where one or both of the words
     * in a potential bigram are in the set of common words .
     *
     * @param input       TokenStream input in filter chain
     * @param commonWords The set of common words.
     * @param keepOrigin  Whether to keep the original common word as a unigram or not.
     */
    public CommonGramsFilter(Version matchVersion, TokenStream input, CharArraySet commonWords, boolean keepOrigin) {
        super(input);

        if (commonWords == null || commonWords.size() == 0)
            throw new IllegalArgumentException("CommonWords list has to contain something; if it doesn't,"
                    + " don't initialize this filter in your analyzer");

        this.commonWords = commonWords;
        this.keepOrigin = keepOrigin;
    }

    /**
     * Inserts bigrams for common words into a token stream. For each input token,
     * output the token. If the token and/or the following token are in the list
     * of common words also output a bigram with position increment 0 and
     * type="gram"
     * <p/>
     * TODO:Consider adding an option to not emit unigram stopwords
     * as in CDL XTF BigramStopFilter, CommonGramsQueryFilter would need to be
     * changed to work with this.
     * <p/>
     * TODO: Consider optimizing for the case of three
     * commongrams i.e "man of the year" normally produces 3 bigrams: "man-of",
     * "of-the", "the-year" but with proper management of positions we could
     * eliminate the middle bigram "of-the"and save a disk seek and a whole set of
     * position lookups.
     */
    @Override
    public final boolean incrementToken() throws IOException {
        // if we have a token from a previous iteration, return it now
        if (restoreMaintainedToken()) {
            saveTermBuffer();
            if (!isCommon())
                return true;
        } else if (savedState != null) { // only relevant if we are keeping originals
            restoreState(savedState);
            savedState = null;
            saveTermBuffer();
            lastWasCommon = isCommon();
            return true;
        }

        // get the next piece of input
        if (!input.incrementToken()) {
            return false;
        }

        // Skip tokens with 0 position increment, so we don't common-gram synonyms etc
        if (posIncAttribute.getPositionIncrement() == 0)
            return true;

    /* We build n-grams before and after stopwords.
     * When valid, the buffer always contains at least the separator.
     * If its empty, there is nothing before this stopword.
     */
        boolean isCommon = isCommon();
        if (keepOrigin) {
            if (lastWasCommon || (isCommon && buffer.length() > 0)) {
                savedState = captureState();
                gramToken();
                return true;
            }
            lastWasCommon = isCommon;
        } else {
            if (!lastWasCommon && isCommon && buffer.length() == 0) {
                lastWasCommon = true;
                saveTermBuffer();
                if (!input.incrementToken())
                    return false;
                isCommon = isCommon();
            }

            if (lastWasCommon || (isCommon && buffer.length() > 0)) {
                lastWasCommon = isCommon;
                rememberCurrentToken();
                gramToken();
                return true;
            }
        }

        saveTermBuffer();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        lastWasCommon = false;
        savedState = null;
        buffer.setLength(0);
        maintainedToken = false;
    }

    // ================================================= Helper Methods ================================================

    /**
     * Determines if the current token is a common term
     *
     * @return {@code true} if the current token is a common term, {@code false} otherwise
     */
    private boolean isCommon() {
        return commonWords.contains(termAttribute.buffer(), 0, termAttribute.length());
    }

    /**
     * Saves this information to form the left part of a gram
     */
    private void saveTermBuffer() {
        buffer.setLength(0);
        buffer.append(termAttribute.buffer(), 0, termAttribute.length());
        buffer.append(SEPARATOR);
        lastStartOffset = offsetAttribute.startOffset();
    }

    /**
     * Constructs a compound token.
     */
    private void gramToken() {
        buffer.append(termAttribute.buffer(), 0, termAttribute.length());
        int endOffset = offsetAttribute.endOffset();

        clearAttributes();

        int length = buffer.length();
        char termText[] = termAttribute.buffer();
        if (length > termText.length) {
            termText = termAttribute.resizeBuffer(length);
        }

        buffer.getChars(0, length, termText, 0);
        termAttribute.setLength(length);
        posIncAttribute.setPositionIncrement(keepOrigin ? 0 : 1);
        posLenAttribute.setPositionLength(2); // bigram
        offsetAttribute.setOffset(lastStartOffset, endOffset);
        typeAttribute.setType(GRAM_TYPE);
        buffer.setLength(0);
    }

    boolean maintainedToken = false;
    int maintainedTokenTextLen, maintainedTokenPosInc, maintainedTokenPosLen;
    int maintainedTokenStartOffset, maintainedTokenEndOffset;
    boolean maintainedTokenIsKeyword;
    String maintainedTokenType;
    char maintainedTokenText[] = new char[Byte.MAX_VALUE];

    private void rememberCurrentToken() {
        maintainedTokenStartOffset = offsetAttribute.startOffset();
        maintainedTokenEndOffset = offsetAttribute.endOffset();
        maintainedTokenPosInc = posIncAttribute.getPositionIncrement();
        maintainedTokenPosLen = posLenAttribute.getPositionLength();
        maintainedTokenType = typeAttribute.type();
        maintainedTokenIsKeyword = keywordAttribute.isKeyword();

        if (maintainedTokenText.length < termAttribute.length())
            maintainedTokenText = new char[termAttribute.length()];
        System.arraycopy(termAttribute.buffer(), 0, maintainedTokenText, 0, termAttribute.length());
        maintainedTokenTextLen = termAttribute.length();
        maintainedToken = true;
    }

    private boolean restoreMaintainedToken() {
        if (!maintainedToken)
            return false;

        clearAttributes();

        char termText[] = termAttribute.buffer();
        if (maintainedTokenTextLen > termText.length) {
            termText = termAttribute.resizeBuffer(maintainedTokenTextLen);
        }

        System.arraycopy(maintainedTokenText, 0, termText, 0, maintainedTokenTextLen);
        termAttribute.setLength(maintainedTokenTextLen);
        posIncAttribute.setPositionIncrement(maintainedTokenPosInc);
        posLenAttribute.setPositionLength(maintainedTokenPosLen);
        offsetAttribute.setOffset(maintainedTokenStartOffset, maintainedTokenEndOffset);
        typeAttribute.setType(maintainedTokenType);
        keywordAttribute.setKeyword(maintainedToken);
        buffer.setLength(0);

        maintainedToken = false;
        return true;
    }
}
