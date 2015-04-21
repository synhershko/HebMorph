package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.util.AttributeImpl;

/**
 * Created by Egozy on 19/04/2015.
 */
public class HebrewTokenTypeAttributeImpl extends AttributeImpl implements HebrewTokenTypeAttribute {
    private HebrewType type = HebrewType.Unknown;
    private boolean isExact = false;
    public void setType(HebrewType type) {
        this.type = type;
    }

    public HebrewType getType() {
        return type;
    }

    public boolean isHebrew() {
        if (type == HebrewType.Hebrew ||
                type == HebrewType.Acronym ||
                type == HebrewType.Construct) {
            return true;
        }
        return false;
    }

    public boolean isNumeric(){
        return type==HebrewType.Numeric;
    }

    public boolean isExact() {
        return isExact;
    }

    public void setExact(boolean isExact) {
        this.isExact = isExact;
    }

    public void clear() {
        type = HebrewType.Unknown;
        isExact = false;
    }

    public void copyTo(AttributeImpl target) {
        ((HebrewTokenTypeAttribute) target).setType(type);
    }
}
