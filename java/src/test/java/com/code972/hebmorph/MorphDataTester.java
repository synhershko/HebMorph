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
package com.code972.hebmorph;

import org.junit.Test;

public class MorphDataTester {
    @Test
    public void lemmaTestEquals() {
        MorphData.Lemma lemma1 = new MorphData.Lemma("asd", 15), lemma2 = new MorphData.Lemma("asd", 15);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma("asd", 5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma("asd", 5);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma("a", 5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma("a", 5);
        assert (lemma1.equals(lemma2));
        lemma1 = new MorphData.Lemma(null, 5);
        assert (!lemma1.equals(lemma2));
        lemma2 = new MorphData.Lemma(null, 5);
        assert (lemma1.equals(lemma2));
    }
}