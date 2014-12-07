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

import com.code972.hebmorph.hspell.Constants.DMask;
import com.code972.hebmorph.hspell.Constants.PrefixType;

public class LingInfo {

    // find the prefixes required by a word according to its details
    public static Integer DMask2ps(Integer dmask) {
        Integer specifier;
        if ((dmask & DMask.D_TYPEMASK) == DMask.D_VERB) {
            if ((dmask & DMask.D_TENSEMASK) == DMask.D_IMPERATIVE) {
                specifier = PrefixType.PS_IMPER;
            } else if ((dmask & DMask.D_TENSEMASK) != DMask.D_PRESENT) {
                specifier = PrefixType.PS_VERB;
            } else if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0)) {
                specifier = PrefixType.PS_NONDEF;
            } else {
                specifier = PrefixType.PS_ALL;
            }
            /*TODO I feel that this may lead to a bug with ליפול and other infinitives that
             * did not loose their initial lamed.  I should correct this all the way from
             * woo.pl*/
            if ((dmask & DMask.D_TENSEMASK) == DMask.D_INFINITIVE) {
                specifier = PrefixType.PS_L;
            } else if ((dmask & DMask.D_TENSEMASK) == DMask.D_BINFINITIVE) {
                specifier = PrefixType.PS_B;
            }
        } else if (((dmask & DMask.D_TYPEMASK) == DMask.D_NOUN) || ((dmask & DMask.D_TYPEMASK) == DMask.D_ADJ)) {
            if (((dmask & DMask.D_OSMICHUT) > 0) || ((dmask & DMask.D_OMASK) > 0) || ((dmask & DMask.D_SPECNOUN) > 0)) {
                specifier = PrefixType.PS_NONDEF;
            } else {
                specifier = PrefixType.PS_ALL;
            }
        } else {
            specifier = PrefixType.PS_ALL;
        }
        return specifier;
    }
}