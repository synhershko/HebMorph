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
package org.apache.lucene.analysis.hebrew;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;


public class NiqqudFilter extends TokenFilter
{
	public NiqqudFilter(TokenStream input)
	{
		super(input);
		termAtt = (TermAttribute)addAttribute(TermAttribute.class);
	}

	private TermAttribute termAtt;

	@Override
	public boolean incrementToken() throws IOException
	{
		if (!input.incrementToken())
			// reached EOS -- return null
		{
			return false;
		}

		// TODO: Limit this check to Hebrew Tokens only

		char[] buffer = termAtt.termBuffer();
		int length = termAtt.termLength(), j = 0;
		for (int i = 0; i < length; i++)
		{
			if ((buffer[i] < 1455) || (buffer[i] > 1476)) // current position is not a Niqqud character
			{
				buffer[j++] = buffer[i];
			}
		}
		termAtt.setTermLength(j);
		return true;
	}
}