/***************************************************************************
 *   Copyright (C) 2010 by                                                 *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
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
 ***************************************************************************/

using System;
using System.Collections.Generic;
using System.Text;

namespace HebMorph
{
    public class MorphData
    {
        public HSpell.DMask[] DescFlags;
        public string[] Lemmas;
        public byte Prefixes;

        public override bool Equals(object obj)
        {
            MorphData o = obj as MorphData;
            if (o == null) return false;

            if (DescFlags.Length != o.DescFlags.Length)
                return false;

            for (int i = 0; i < DescFlags.Length; i++)
            {
                if (DescFlags[i] != o.DescFlags[i] || !Lemmas[i].Equals(o.Lemmas[i]))
                    return false;
            }
            return true;
        }

        public override int GetHashCode()
        {
            return DescFlags.GetHashCode() * Lemmas.GetHashCode();
        }
    }
}
