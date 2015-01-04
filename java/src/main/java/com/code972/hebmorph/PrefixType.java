package com.code972.hebmorph;

public enum PrefixType{
    PS_EMPTY ((byte)0),
    PS_B((byte)1),
    PS_L((byte)2),
    PS_VERB((byte)4),
    PS_NONDEF((byte)8),
    PS_IMPER((byte)16),
    PS_MISC((byte)32),
    PS_ALL((byte)63);
    private final byte val;
    PrefixType(byte val){
        this.val = val;
    }

    public byte getValue(){
        return this.val;
    }

    public static PrefixType create(byte val) {
        switch (val) {
            case 0: return PS_EMPTY;
            case 1: return PS_B;
            case 2: return PS_L;
            case 4: return PS_VERB;
            case 8: return PS_NONDEF;
            case 16: return PS_IMPER;
            case 32: return PS_MISC;
            case 63: return PS_ALL;
            default: throw new IllegalArgumentException(); // default, guess this doesn't really have to make sense
        }
    }
}
