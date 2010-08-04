/**************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com>							   *
 *                                                                         *
 *   Distributed under the GNU General Public License, Version 2.0.        *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation (v2).                                    *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Steet, Fifth Floor, Boston, MA  02111-1307, USA.          *
 **************************************************************************/
package hebmorph;

import hebmorph.hspell.LingInfo;

public class HebrewToken extends Token implements Comparable
{
	public HebrewToken(String _word, int _prefixLength, Integer _mask, String _lemma, float _score)
	{
		super(_word);
		prefixLength = _prefixLength;
		setMask(_mask);
		if (_lemma == null)
		{
			lemma = _word.substring(prefixLength); // Support null lemmas while still taking into account prefixes
		}
		else
		{
			lemma = _lemma;
		}
		setScore(_score);
	}

	private float score = 1.0f;
	private int prefixLength;
	private Integer mask;
	private String lemma;

	@Override
	public boolean equals(Object obj)
	{
		HebrewToken o = (HebrewToken)((obj instanceof HebrewToken) ? obj : null);
            if (o == null) return false;

		// TODO: In places where Equals returns true while being called from the sorted results list,
		// but this.Score < o.Score, we probably should somehow update the score for this object...
            return ((prefixLength == o.prefixLength)
                && (getMask() == o.getMask())
                && getText().equals(o.getText())
                && (lemma == o.lemma));
	}

	@Override
	public int hashCode()
	{
		return super.hashCode(); //TODO
	}

	@Override
	public String toString()
	{
		return String.format("\t%s (%s)", lemma, LingInfo.DMask2EnglishString(getMask()));
	}

	public final int compareTo(Object obj)
	{
		HebrewToken o = (HebrewToken)((obj instanceof HebrewToken) ? obj : null);
        if (o == null) return -1;

        if (getScore() == o.getScore())
            return 0;
        else if (getScore() > o.getScore())
            return 1;
        return -1;
	}

	public void setScore(float score)
	{
		this.score = score;
	}

	public float getScore()
	{
		return score;
	}

	public void setMask(Integer mask)
	{
		this.mask = mask;
	}

	public Integer getMask()
	{
		return mask;
	}

	public int getPrefixLength()
	{
		return prefixLength;
	}

	public String getLemma()
	{
		return lemma;
	}


}