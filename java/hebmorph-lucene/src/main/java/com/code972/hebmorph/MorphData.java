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
package com.code972.hebmorph;

import java.util.Arrays;

public class MorphData {
    private Lemma[] lemmas;
    private short prefixes;


    public static class Lemma {
        private final int descFlag;
        private final String lemma;

        public Lemma(String lemma, int descFlag){
            this.lemma = lemma;
            this.descFlag = descFlag;
        }

        public int getDescFlag() {
            return descFlag;
        }

        public String getLemma() {
            return lemma;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Lemma other = (Lemma) obj;
            if (this.descFlag != (other.descFlag) || !this.lemma.equals(other.lemma)){
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 37;
            int result = 1;
            result = prime * result + descFlag;
            result = prime * result + lemma.hashCode();
            return result;
        }
    }

    public void setLemmas(Lemma[] lemmas) {
        this.lemmas = lemmas;
    }

    public Lemma[] getLemmas() {
        return lemmas;
    }

    public void setPrefixes(short prefixes) {
        this.prefixes = prefixes;
    }

    public int getPrefixes() {
        return prefixes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MorphData other = (MorphData) obj;
        if (!Arrays.equals(lemmas, other.lemmas))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(lemmas);
        return result;
    }

    @Override
    public String toString() {
        return "{ prefix=" + prefixes + " lemmas=" + Arrays.asList(lemmas) + "}";
    }
}