package org.apache.lucene.analysis.hebrew;

import org.apache.lucene.util.Attribute;

/**
 * This attribute is used to pass info on tokens as parsed and identified
 * by the HebMorph tokenizer
 */
public interface HebrewTokenTypeAttribute extends Attribute{
    enum HebrewType{
        Unknown,
        Hebrew,
        NonHebrew,
        Numeric,
        Construct,
        Acronym ,
        Mixed,
        Lemma;
    }

    void setType(HebrewType type);
    HebrewType getType();
    boolean isHebrew();
    boolean isExact();
    boolean isNumeric();
    void setExact(boolean isExact);
}
