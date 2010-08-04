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

package org.apache.lucene.analysis;

import java.io.IOException;
import java.util.Map;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;



public class AddSuffixFilter extends TokenFilter
{
	private TermAttribute termAtt;
	private TypeAttribute typeAtt;

	public Map<String, char[]> suffixByTokenType = null;

	public AddSuffixFilter(TokenStream input, Map<String, char[]> _suffixByTokenType)
	{
		super(input);
		termAtt = (TermAttribute)addAttribute(TermAttribute.class);
		typeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
		suffixByTokenType = _suffixByTokenType;
	}

	@Override
	public boolean incrementToken() throws IOException
	{
		if (!input.incrementToken())
			// reached EOS -- return null
		{
			return false;
		}

		if (suffixByTokenType == null)
		{
			return true;
		}

		char[] suffix;
		if (!((suffix = suffixByTokenType.get(typeAtt.type())) != null))
		{
			return true;
		}

		char[] buffer = termAtt.termBuffer();
		int length = termAtt.termLength();

		if (buffer.length <= length)
		{
			buffer = termAtt.resizeTermBuffer(length + suffix.length);
		}

		System.arraycopy(suffix, 0, buffer, length, suffix.length);
		termAtt.setTermLength(length + suffix.length);

		return true;
	}
}