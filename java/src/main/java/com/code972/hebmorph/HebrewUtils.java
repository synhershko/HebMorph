package com.code972.hebmorph;

/**
 * Created by Egozy on 12/04/2015.
 */
public class HebrewUtils {

    public static final char[] Geresh = {'\'', '\u05F3', '\u2018', '\u2019', '\u201B', '\uFF07'};
    public static final char[] Gershayim = {'\"', '\u05F4', '\u201C', '\u201D', '\u201F', '\u275E', '\uFF02'};
    public static final char[] Makaf = {'-', '\u2012', '\u2013', '\u2014', '\u2015', '\u05BE'};
    public static final char[] CharsFollowingPrefixes = concatenateCharArrays(Geresh, Gershayim, Makaf);
    public static final char[] LettersAcceptingGeresh = {'ז', 'ג', 'ץ', 'צ', 'ח'};

    public static boolean isOfChars(char c, char[] options) {
        for (char o : options) {
            if (c == o) return true;
        }
        return false;
    }

    public static char[] concatenateCharArrays(char[]... arrays) {
        int count = 0;
        for (char[] a : arrays) {
            count += a.length;
        }

        char[] ret = new char[count];
        int offs = 0;
        for (char[] a : arrays) {
            System.arraycopy(a, 0, ret, offs, a.length);
            offs += a.length;
        }

        return ret;
    }

    public static boolean isHebrewLetter(char c) {
        return ((c >= 1488) && (c <= 1514));
    }

    public static boolean isFinalHebrewLetter(char c) {
        return (c == 1507 || c == 1498 || c == 1501 || c == 1509 || c == 1503);
    }

    public static boolean isNiqqudChar(char c) {
        return ((c >= 1456) && (c <= 1465)) || (c == '\u05C1' || c == '\u05C2' || c == '\u05BB' || c == '\u05BC');
    }
}
