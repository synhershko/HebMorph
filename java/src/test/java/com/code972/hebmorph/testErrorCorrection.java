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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class testErrorCorrection extends TestBase {

    private final Lemmatizer _lemmatizer;

    public testErrorCorrection() throws IOException {
        _lemmatizer = new Lemmatizer(getDictionary());
    }

    @Test
    public void SimpleAHVICases() {
        //AssertWord("שלחן", "שולחן");
        AssertWord("אמא", "אימא");
        AssertWord("אנצקלופדיה", "אנציקלופדיה");
        AssertWord("אינציקלופדיה", "אנציקלופדיה");
        //AssertWord("פינגוין", "פינגווין");
        //AssertNotCorrected("שלומי", "שלום");
        // ביבי -> בבבי
        // ביבי -> שביב
        // ביבי -> לבייב
    }

    private void AssertWord(String word, String expectedWord) {
        System.out.println("Testing " + word + " -> " + expectedWord);
        Assert.assertTrue(_lemmatizer.lemmatize(expectedWord).size() > 0); // make sure the expected word is legal
        List<HebrewToken> results = _lemmatizer.lemmatizeTolerant(word);
        Assert.assertTrue(results.size() > 0);
        assertEquals(expectedWord, results.get(0).getLemma());
    }
}
