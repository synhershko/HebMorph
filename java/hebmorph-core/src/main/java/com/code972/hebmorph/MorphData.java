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
package com.code972.hebmorph;

import java.util.Arrays;

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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MorphData other = (MorphData) obj;
        if (!Arrays.equals(descFlags, other.descFlags))
            return false;
        if (!Arrays.equals(lemmas, other.lemmas))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(descFlags);
        result = prime * result + Arrays.hashCode(lemmas);
        return result;
    }

    @Override
    public String toString() {
        return "{ prefix="+prefixes+" lemmas="+Arrays.asList(lemmas)+"}";
    }
}