/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package com.code972.hebmorph.hspell;

public final class Constants {
    public static final int MaxWordLength = Byte.MAX_VALUE;

    public static interface DMask {
        public static final int D_NOUN = 1;
        public static final int D_VERB = 2;
        public static final int D_ADJ = 3;
        public static final int D_TYPEMASK = 3;
        public static final int D_GENDERBASE = 4;
        public static final int D_MASCULINE = 4;
        public static final int D_FEMININE = 8;
        public static final int D_GENDERMASK = 12;
        public static final int D_GUFBASE = 16;
        public static final int D_FIRST = 16;
        public static final int D_SECOND = 32;
        public static final int D_THIRD = 48;
        public static final int D_GUFMASK = 48;
        public static final int D_NUMBASE = 64;
        public static final int D_SINGULAR = 64;
        public static final int D_DOUBLE = 128;
        public static final int D_PLURAL = 192;
        public static final int D_NUMMASK = 192;
        public static final int D_TENSEBASE = 256;
        public static final int D_INFINITIVE = 256;
        public static final int D_BINFINITIVE = 1536;
        public static final int D_PAST = 512;
        public static final int D_PRESENT = 768;
        public static final int D_FUTURE = 1024;
        public static final int D_IMPERATIVE = 1280;
        public static final int D_TENSEMASK = 1792;
        public static final int D_OGENDERBASE = 2048;
        public static final int D_OMASCULINE = 2048;
        public static final int D_OFEMININE = 4096;
        public static final int D_OGENDERMASK = 6144;
        public static final int D_OGUFBASE = 8192;
        public static final int D_OFIRST = 8192;
        public static final int D_OSECOND = 16384;
        public static final int D_OTHIRD = 24576;
        public static final int D_OGUFMASK = 24576;
        public static final int D_ONUMBASE = 32768;
        public static final int D_OSINGULAR = 32768;
        public static final int D_ODOUBLE = 65536;
        public static final int D_OPLURAL = 98304;
        public static final int D_ONUMMASK = 98304;
        public static final int D_OMASK = 129024;
        public static final int D_OSMICHUT = 131072;
        public static final int D_SPECNOUN = 262144;
        public static final int D_STARTBIT = 524288;
        public static final int D_ACRONYM = 1048576;
    }

    public static interface PrefixType {
        public static final int PS_ALL = 63;
        public static final int PS_B = 1;
        public static final int PS_L = 2;
        public static final int PS_VERB = 4;
        public static final int PS_NONDEF = 8;
        public static final int PS_IMPER = 16;
        public static final int PS_MISC = 32;
    }

}
