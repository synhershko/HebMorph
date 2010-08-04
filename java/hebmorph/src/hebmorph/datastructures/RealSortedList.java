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
package hebmorph.datastructures;

import java.util.Collection;
import java.util.Iterator;

public class RealSortedList<T extends Comparable> extends java.util.ArrayList<T>
{
    public enum SortOrder { Asc, Desc };

	protected SortOrder sortOrder = SortOrder.Asc;

	public RealSortedList()
	{
		super();
	}

	public RealSortedList(Collection<T> collection)
	{
		super(collection);
	}

	public RealSortedList(int capacity)
	{
		super(capacity);
	}

	public RealSortedList(SortOrder _sortOrder)
	{
		super();
		this.sortOrder = _sortOrder;
	}

	public RealSortedList(Collection<T> collection, SortOrder _sortOrder)
	{
		super(collection);
		this.sortOrder = _sortOrder;
	}

	public RealSortedList(int capacity, SortOrder _sortOrder)
	{
		super(capacity);
		this.sortOrder = _sortOrder;
	}

	/**
	 Add item only if it doesn't exist in the collection already. T has to have Equals implemented.

	 @param item Item of type T to add
	 @return true if added, false otherwise
	*/
	public boolean addUnique(T item)
	{
		Iterator<T> en = iterator();
		while (en.hasNext())
		{
			if (en.next().equals(item))
			{
				return false;
			}
		}
		this.add(item);
		return true;
	}

	@Override
	public boolean add(T item)
	{
		if (size() == 0)
		{
			super.add(item);
			return true;
		}

		int i = 0, cmp = 0;

		Iterator<T> en = iterator();
		while (en.hasNext())
		{
			cmp = en.next().compareTo(item) ;
			if (((sortOrder == SortOrder.Desc) && (cmp < 0)) || ((sortOrder == SortOrder.Asc) && (cmp > 0)))
			{
				break;
			}

			i++;
		}
		super.add(i, item);
		return true;
	}
}