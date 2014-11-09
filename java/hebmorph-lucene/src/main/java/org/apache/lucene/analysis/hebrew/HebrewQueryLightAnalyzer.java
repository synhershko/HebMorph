package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.MorphData;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.HSpellLoader;
import org.apache.lucene.analysis.CommonGramsFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Created by synhershko on 12/25/13.
 */
public class HebrewQueryLightAnalyzer extends HebrewAnalyzer {
    public HebrewQueryLightAnalyzer(DictHebMorph dict, DictRadix<MorphData> customWords) throws IOException {
        super(dict, customWords);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        // on query - if marked as keyword don't keep origin, else only lemmatized (don't suffix)
        // if word termintates with $ will output word$, else will output all lemmas or word$ if OOV
        final StreamLemmasFilter src = new StreamLemmasFilter(reader, dict, SPECIAL_TOKENIZATION_CASES, commonWords, lemmaFilter);
        src.setCustomWords(customWords);
        src.setKeepOriginalWord(false);
        src.setSuffixForExactMatch(originalTermSuffix);

        TokenStream tok = new ASCIIFoldingFilter(src);
        //tok = new SuffixKeywordFilter(tok, '$');
        tok = new AddSuffixFilter(tok, '$') {
            @Override
            protected void handleCurrentToken() {
                if (HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew).equals(typeAtt.type())) {
                    if (keywordAtt.isKeyword())
                        suffixCurrent();
                    return;
                }

                if (HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew).equals(typeAtt.type())) {
                    if (keywordAtt.isKeyword()) {
                        suffixCurrent();
                        return;
                    }
                }

                if (CommonGramsFilter.GRAM_TYPE.equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric).equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Mixed).equals(typeAtt.type())) {
                    keywordAtt.setKeyword(true);
                    return;
                }

                duplicateCurrentToken();
                suffixCurrent();
            }
        };
        return new TokenStreamComponents(src, tok);
    }


}