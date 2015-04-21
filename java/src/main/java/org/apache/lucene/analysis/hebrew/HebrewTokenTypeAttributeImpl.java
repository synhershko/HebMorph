package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.util.AttributeImpl;

/**
 * Created by Egozy on 19/04/2015.
 */
public class HebrewTokenTypeAttributeImpl extends AttributeImpl implements HebrewTokenTypeAttribute{
    private HebrewType type = HebrewType.Unknown;

    @Override
    public void setType(HebrewType type) {
        this.type = type;
    }

    @Override
    public HebrewType getType() {
        return type;
    }

    @Override
    public void clear() {
        type = HebrewType.Unknown;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        ((HebrewTokenTypeAttribute) target).setType(type);
    }
}
