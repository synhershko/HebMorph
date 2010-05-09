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

using HebMorph.DataStructures;

namespace HebMorph.HSpell
{
    public sealed class LingInfo
    {
        public static DictRadix<int> BuildPrefixTree(bool allowHeHasheela)
        {
            string[] prefixes;
            int[] masks;
            if (allowHeHasheela)
            {
                prefixes = Constants.prefixes_H;
                masks = Constants.masks_H;
            }
            else
            {
                prefixes = Constants.prefixes_noH;
                masks = Constants.masks_noH;
            }

            DictRadix<int> ret = new DictRadix<int>();
            for (int i = 0; prefixes[i] != null; i++)
                ret.AddNode(prefixes[i], masks[i]);

            return ret;
        }

        /* find the prefixes required by a word according to its details */
        public static PrefixType dmask2ps(DMask dmask)
        {
            PrefixType specifier;
            if ((dmask & DMask.D_TYPEMASK) == DMask.D_VERB)
            {
                if ((dmask & DMask.D_TENSEMASK) == DMask.D_IMPERATIVE)
                {
                    specifier = PrefixType.PS_IMPER;
                }
                else if ((dmask & DMask.D_TENSEMASK) != DMask.D_PRESENT)
                {
                    specifier = PrefixType.PS_VERB;
                }
                else if ((dmask & DMask.D_OSMICHUT) > 0 || (dmask & DMask.D_OMASK) > 0)
                {
                    specifier = PrefixType.PS_NONDEF;
                }
                else specifier = PrefixType.PS_ALL;
                /* TODO I feel that this may lead to a bug with ליפול and other infinitives that
                 * did not loose their initial lamed.  I should correct this all the way from
                 * woo.pl */
                if ((dmask & DMask.D_TENSEMASK) == DMask.D_INFINITIVE) specifier = PrefixType.PS_L;
                else if ((dmask & DMask.D_TENSEMASK) == DMask.D_BINFINITIVE) specifier = PrefixType.PS_B;
            }
            else if (((dmask & DMask.D_TYPEMASK) == DMask.D_NOUN) || ((dmask & DMask.D_TYPEMASK) == DMask.D_ADJ))
            {
                if ((dmask & DMask.D_OSMICHUT) > 0 || (dmask & DMask.D_OMASK) > 0
                    || (dmask & DMask.D_SPECNOUN) > 0)
                {
                    specifier = PrefixType.PS_NONDEF;
                }
                else
                {
                    specifier = PrefixType.PS_ALL;
                }
            }
            else specifier = PrefixType.PS_ALL;
            return specifier;
        }

        public static string DMask2HebrewString(DMask dmask)
        {
            string ret = string.Empty;

            switch (dmask & DMask.D_TYPEMASK)
            {
                case DMask.D_NOUN: ret = "ע"; break;
                case DMask.D_VERB: ret = "פ"; break;
                case DMask.D_ADJ: ret = "ת"; break;
                case 0: ret = "x"; break;
                //default: ret = "";
            }

            /* In few cases, both masculine and faminine are possible */
            if (((dmask & DMask.D_GENDERMASK) & DMask.D_MASCULINE) > 0) { ret += ",ז"; }
            if (((dmask & DMask.D_GENDERMASK) & DMask.D_FEMININE) > 0) { ret += ",נ"; }

            switch (dmask & DMask.D_GUFMASK)
            {
                case DMask.D_FIRST: ret += ",1"; break;
                case DMask.D_SECOND: ret += ",2"; break;
                case DMask.D_THIRD: ret += ",3"; break;
                //default: ret += "";
            }
            switch (dmask & DMask.D_NUMMASK)
            {
                case DMask.D_SINGULAR: ret += ",יחיד"; break;
                case DMask.D_DOUBLE: ret += ",זוגי"; break;
                case DMask.D_PLURAL: ret += ",רבים"; break;
                //default: ret += "";
            }
            switch (dmask & DMask.D_TENSEMASK)
            {
                case DMask.D_PAST: ret += ",עבר"; break;
                case DMask.D_PRESENT: ret += ",הווה"; break;
                case DMask.D_FUTURE: ret += ",עתיד"; break;
                case DMask.D_IMPERATIVE: ret += ",ציווי"; break;
                case DMask.D_INFINITIVE: ret += ",מקור"; break;
                case DMask.D_BINFINITIVE: ret += ",מקור,ב"; break;
                //default: ret += "";
            }
            if ((dmask & DMask.D_SPECNOUN) > 0) { ret += ",פרטי"; }
            if ((dmask & DMask.D_OSMICHUT) > 0) { ret += ",סמיכות"; }
            if ((dmask & DMask.D_OMASK) > 0)
            {
                ret+= ",כינוי/";
                switch (dmask & DMask.D_OGENDERMASK)
                {
                    case DMask.D_OMASCULINE: ret += "ז"; break;
                    case DMask.D_OFEMININE: ret += "נ"; break;
                    //default: ret += "";
                }
                switch (dmask & DMask.D_OGUFMASK)
                {
                    case DMask.D_OFIRST: ret += ",1"; break;
                    case DMask.D_OSECOND: ret += ",2"; break;
                    case DMask.D_OTHIRD: ret += ",3"; break;
                    //default: ret += "";
                }
                switch (dmask & DMask.D_ONUMMASK)
                {
                    case DMask.D_OSINGULAR: ret += ",יחיד"; break;
                    case DMask.D_ODOUBLE: ret += ",זוגי"; break;
                    case DMask.D_OPLURAL: ret += ",רבים"; break;
                    //default: ret += "";
                }
            }

            return ret;
        }

        public static string DMask2EnglishString(DMask dmask)
        {
            string ret = string.Empty;

            switch (dmask & DMask.D_TYPEMASK)
            {
                case DMask.D_NOUN: ret = "Noun"; break;
                case DMask.D_VERB: ret = "Verb"; break;
                case DMask.D_ADJ: ret = "Adj"; break;
                case 0: ret = "x"; break;
                //default: ret = "";
            }

            /* In few cases, both masculine and faminine are possible */
            if (((dmask & DMask.D_GENDERMASK) & DMask.D_MASCULINE) > 0) { ret += ",Masculine"; }
            if (((dmask & DMask.D_GENDERMASK) & DMask.D_FEMININE) > 0) { ret += ",Feminine"; }

            switch (dmask & DMask.D_GUFMASK)
            {
                case DMask.D_FIRST: ret += ",1st"; break;
                case DMask.D_SECOND: ret += ",2nd"; break;
                case DMask.D_THIRD: ret += ",3rd"; break;
                //default: ret += "";
            }
            switch (dmask & DMask.D_NUMMASK)
            {
                case DMask.D_SINGULAR: ret += ",Singular"; break;
                case DMask.D_DOUBLE: ret += ",Dual"; break;
                case DMask.D_PLURAL: ret += ",Plural"; break;
                //default: ret += "";
            }
            switch (dmask & DMask.D_TENSEMASK)
            {
                case DMask.D_PAST: ret += ",Past"; break;
                case DMask.D_PRESENT: ret += ",Present"; break;
                case DMask.D_FUTURE: ret += ",Future"; break;
                case DMask.D_IMPERATIVE: ret += ",Imperative"; break;
                case DMask.D_INFINITIVE: ret += ",Infinitive"; break;
                case DMask.D_BINFINITIVE: ret += ",B,Infinitive"; break;
                //default: ret += "";
            }
            if ((dmask & DMask.D_SPECNOUN) > 0) { ret += ",Proper"; }
            if ((dmask & DMask.D_OSMICHUT) > 0) { ret += ",Construct"; }
            if ((dmask & DMask.D_OMASK) > 0)
            {
                ret += ",Pronominal/";
                switch (dmask & DMask.D_OGENDERMASK)
                {
                    case DMask.D_OMASCULINE: ret += "Masculine"; break;
                    case DMask.D_OFEMININE: ret += "Feminine"; break;
                    //default: ret += "";
                }
                switch (dmask & DMask.D_OGUFMASK)
                {
                    case DMask.D_OFIRST: ret += ",1st"; break;
                    case DMask.D_OSECOND: ret += ",2nd"; break;
                    case DMask.D_OTHIRD: ret += ",3rd"; break;
                    //default: ret += "";
                }
                switch (dmask & DMask.D_ONUMMASK)
                {
                    case DMask.D_OSINGULAR: ret += ",Singular"; break;
                    case DMask.D_ODOUBLE: ret += ",Dual"; break;
                    case DMask.D_OPLURAL: ret += ",Plural"; break;
                    //default: ret += "";
                }
            }

            return ret;
        }
    }
}
