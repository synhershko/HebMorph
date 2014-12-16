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

public class HebrewToken extends Token implements Comparable<Token> {
    private static final long serialVersionUID = -5809495040446607703L;

    public HebrewToken(String _word, byte _prefixLength, MorphData.Lemma lemma, float _score) {
        this(_word, _prefixLength, lemma.getDescFlag(), lemma.getLemma(), _score);
    }

    public HebrewToken(String _word, byte _prefixLength, Integer _mask, String _lemma, float _score) {
        super(_word);
        prefixLength = _prefixLength;
        setMask(_mask);
        if (_lemma == null) {
            lemma = _word.substring(prefixLength); // Support null lemmas while still taking into account prefixes
        } else {
            lemma = _lemma;
        }
        setScore(_score);
    }

    private float score = 1.0f;
    private byte prefixLength;
    private Integer mask;
    private String lemma;

    /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof HebrewToken)) {
            return false;
        }
        HebrewToken other = (HebrewToken) obj;
        if (lemma == null) {
            if (other.lemma != null) {
                return false;
            }
        } else if (!lemma.equals(other.lemma)) {
            return false;
        }
        if (mask == null) {
            if (other.mask != null) {
                return false;
            }
        } else if (!mask.equals(other.mask)) {
            return false;
        }
        if (prefixLength != other.prefixLength) {
            return false;
        }
        if (Float.floatToIntBits(score) != Float.floatToIntBits(other.score)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
        result = prime * result + ((mask == null) ? 0 : mask.hashCode());
        result = prime * result + prefixLength;
        result = prime * result + Float.floatToIntBits(score);
        return result;
    }

    @Override
    public String toString() {
//        return String.format("%s (%s)", lemma, LingInfo.DMask2EnglishString(getMask()));
        return String.format("%s", lemma);
    }

    public final int compareTo(Token token) {
        HebrewToken other = (HebrewToken) ((token instanceof HebrewToken) ? token : null);
        if (other == null) return -1;

        return ((Float) getScore()).compareTo(other.getScore());
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    public Integer getMask() {
        return mask;
    }

    public byte getPrefixLength() {
        return prefixLength;
    }

    public String getLemma() {
        return lemma;
    }
}