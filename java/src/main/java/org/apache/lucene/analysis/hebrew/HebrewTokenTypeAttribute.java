package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.util.Attribute;

/**
 * Created by Egozy on 19/04/2015.
 */
public interface HebrewTokenTypeAttribute extends Attribute{
    public static enum HebrewType{
        Unknown,
        Hebrew,
        NonHebrew,
        Numeric,
        Construct,
        Acronym ,
        Mixed,
        Lemma;
    }

    public void setType(HebrewType type);
    public HebrewType getType();
}
