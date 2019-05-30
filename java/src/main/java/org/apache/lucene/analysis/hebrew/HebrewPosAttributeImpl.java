package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.DescFlag;
import com.code972.hebmorph.HebrewToken;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

public class HebrewPosAttributeImpl extends AttributeImpl implements HebrewPosAttribute {

    private HebrewToken token;

    @Override
    public void setHebrewToken(HebrewToken hebToken) {
        this.token = hebToken;
    }

    @Override
    public PosTag getPosTag() {
        if (token != null) {
            if (token.getMask() == DescFlag.D_VERB) {
                return PosTag.Verb;
            }
            if (token.getMask() == DescFlag.D_NOUN) {
                return PosTag.Noun;
            }
            if (token.getMask() == DescFlag.D_PROPER) {
                return PosTag.ProperNoun;
            }
            if (token.getMask() == DescFlag.D_ADJ) {
                return PosTag.Adjective;
            }
        }
        return PosTag.Unknown;
    }

    @Override
    public void clear() {
        token = null;
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
        PosTag partOfSpeech = getPosTag();
        reflector.reflect(HebrewPosAttribute.class, "partOfSpeech", partOfSpeech);
    }

    @Override
    public void copyTo(AttributeImpl target) {
        HebrewPosAttribute t = (HebrewPosAttribute) target;
        t.setHebrewToken(token);
    }
}
