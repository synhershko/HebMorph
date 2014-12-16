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
package com.code972.hebmorph.datastructures;

import java.util.Collection;
import java.util.Iterator;

public class RealSortedList<T extends Comparable<? super T>> extends java.util.ArrayList<T> {
    private static final long serialVersionUID = 9216388435253045978L;

    public enum SortOrder {Asc, Desc}

    protected SortOrder sortOrder = SortOrder.Asc;

    public RealSortedList() {
        super();
    }

    public RealSortedList(Collection<T> collection) {
        super(collection);
    }

    public RealSortedList(int capacity) {
        super(capacity);
    }

    public RealSortedList(SortOrder _sortOrder) {
        super();
        this.sortOrder = _sortOrder;
    }

    public RealSortedList(Collection<T> collection, SortOrder _sortOrder) {
        super(collection);
        this.sortOrder = _sortOrder;
    }

    public RealSortedList(int capacity, SortOrder _sortOrder) {
        super(capacity);
        this.sortOrder = _sortOrder;
    }

    /**
     * Add item only if it doesn't exist in the collection already. T has to have Equals implemented.
     *
     * @param item Item of type T to add
     * @return true if added, false otherwise
     */
    public boolean addUnique(T item) {
        Iterator<T> en = iterator();
        while (en.hasNext()) {
            if (en.next().equals(item)) {
                return false;
            }
        }
        this.add(item);
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(T item) {
        if (size() == 0) {
            super.add(item);
            return true;
        }

        int i = 0, cmp = 0;

        Iterator<T> en = iterator();
        while (en.hasNext()) {
            cmp = en.next().compareTo(item);
            if (((sortOrder == SortOrder.Desc) && (cmp < 0)) || ((sortOrder == SortOrder.Asc) && (cmp > 0))) {
                break;
            }

            i++;
        }
        super.add(i, item);
        return true;
    }
}