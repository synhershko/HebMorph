/***************************************************************************
 *   Copyright (C) 2010-2013 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com> (initial Java port)           *
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
package com.code972.hebmorph.lemmafilters;

import com.code972.hebmorph.HebrewToken;
import com.code972.hebmorph.Token;
import com.code972.hebmorph.hspell.Constants.DMask;

import java.util.List;


/**
 * BasicLemmaFilter will only filter collections with more than one lemma. For them, any lemma
 * scored below 0.7 is probably a result of some heavy toleration, and will be ignored.
 */
public class BasicLemmaFilter extends LemmaFilterBase {

    @Override
    public List<Token> filterCollection(final String word, final List<Token> collection, final List<Token> preallocatedOut) {
        if (collection.size() > 1) {
            final List<Token> ret = super.filterCollection(word, collection, preallocatedOut);
            if (ret != null && ret.size() > 0) {
                return ret;
            }
        }
        return null;
    }

    @Override
    public boolean isValidToken(final Token t) {
        if (t instanceof HebrewToken) {
            final HebrewToken ht = (HebrewToken) t;

            // Pose a minimum score limit for words
            if (ht.getScore() < 0.7f) {
                return false;
            }

            // Pose a higher threshold to verbs (easier to get irrelevant verbs from toleration)
            if (((ht.getMask() & DMask.D_TYPEMASK) == DMask.D_VERB) && (ht.getScore() < 0.85f)) {
                return false;
            }
        }
        return true;
    }
}