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

public class MorphData
{
	private Integer[] descFlags;
	private String[] lemmas;
	private int prefixes;

	public void setLemmas(String[] lemmas)
	{
		this.lemmas = lemmas;
	}

	public String[] getLemmas()
	{
		return lemmas;
	}

	public void setPrefixes(int prefixes)
	{
		this.prefixes = prefixes;
	}

	public int getPrefixes()
	{
		return prefixes;
	}

	public void setDescFlags(Integer[] descFlags)
	{
		this.descFlags = descFlags;
	}

	public Integer[] getDescFlags()
	{
		return descFlags;
	}

	@Override
	public boolean equals(Object obj)
	{
		MorphData o = (MorphData)((obj instanceof MorphData) ? obj : null);
		if (o == null)
		{
			return false;
		}

		if (getDescFlags().length != o.getDescFlags().length)
		{
			return false;
		}

		for (int i = 0; i < getDescFlags().length; i++)
		{
			if ((getDescFlags()[i] != o.getDescFlags()[i]) || !getLemmas()[i].equals(o.getLemmas()[i]))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		return getDescFlags().hashCode() * getLemmas().hashCode();
	}
}