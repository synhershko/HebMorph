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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MorphData {
    private ArrayList<Lemma> lemmas;
    private short prefixes;

    public static class Lemma {
        private final DescFlag descFlag;
        private final String lemma;
        private final PrefixType prefix;

        public Lemma(String lemma, DescFlag descFlag, PrefixType prefix) {
            this.lemma = lemma;
            this.descFlag = descFlag;
            this.prefix = prefix;
        }

        public DescFlag getDescFlag() {
            return descFlag;
        }

        public String getLemma() {
            return lemma;
        }

        public PrefixType getPrefix() {
            return prefix;
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
            if (this.descFlag != other.descFlag) {
                return false;
            }
            if (this.prefix != other.prefix) {
                return false;
            }
            if (this.lemma == null) {
                return other.lemma == null;
            }
            return this.lemma.equals(other.lemma);
        }

        @Override
        public int hashCode() {
            final int prime = 37;
            int result = 1;
            result = prime * result + descFlag.hashCode();
            result = prime * result + prefix.getValue();
            result = prime * result + (lemma == null ? 0 : lemma.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return lemma + ":" + descFlag + ":" + prefix;
        }
    }

    public void setLemmas(Lemma[] lemmas) {
        Arrays.sort(lemmas, new Comparator<Lemma>() {
            @Override
            public int compare(Lemma l1, Lemma l2) {
                return l1.descFlag.getVal() - l2.descFlag.getVal();
            }
        });
        this.lemmas = new ArrayList<>(Arrays.asList(lemmas));
    }

    public Lemma[] getLemmas() {
        return lemmas.toArray(new Lemma[lemmas.size()]);
    }

    public void addLemma(Lemma lemma) {
        lemmas.add(lemma);
        Collections.sort(lemmas, new Comparator<Lemma>() {
            @Override
            public int compare(Lemma l1, Lemma l2) {
                return l1.descFlag.getVal() - l2.descFlag.getVal();
            }
        });
    }

    public void clearLemmas(){
        lemmas.clear();
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
        if (lemmas == null) {
            return other.lemmas == null;
        }
        return other.lemmas != null && lemmas.equals(other.lemmas);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lemmas.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{ prefix=" + prefixes + " lemmas=" + Arrays.asList(lemmas) + "}";
    }
}

