package com.code972.hebmorph;

/**
 * Created by Egozy on 28/12/2014.
 */
public enum DescFlag {
    D_EMPTY,
    D_NOUN,
    D_VERB,
    D_ADJ,
    D_PROPER,
    D_ACRONYM;

    public static DescFlag create(byte val) {
        switch (val) {
            case 0:
                return D_EMPTY;
            case 1:
                return D_NOUN;
            case 2:
                return D_VERB;
            case 3:
                return D_ADJ;
            case 4:
                return D_PROPER;
            case 5:
                return D_ACRONYM;
        }
        throw new IllegalArgumentException();
    }

    public int getVal() {
        switch (this) {
            case D_EMPTY:
                return 0;
            case D_NOUN:
                return 1;
            case D_VERB:
                return 2;
            case D_ADJ:
                return 3;
            case D_PROPER:
                return 4;
            case D_ACRONYM:
                return 5;
        }
        throw new IllegalArgumentException();
    }
}
