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

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.Map;



public final class AddSuffixFilter extends TokenFilter
{
    private final TermAttribute termAtt = addAttribute(TermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

	private final Map<String, char[]> suffixByTokenType;
    private final boolean keepOrigin;

    public AddSuffixFilter(final TokenStream input, final Map<String, char[]> _suffixByTokenType) {
        this(input, _suffixByTokenType, false);
    }

	public AddSuffixFilter(final TokenStream input, final Map<String, char[]> _suffixByTokenType, boolean keepOrigin) {
		super(input);
		suffixByTokenType = _suffixByTokenType;
        this.keepOrigin = keepOrigin;
	}

    private char[] tokenBuffer = new char[Byte.MAX_VALUE];
    private int tokenLen = 0;

	@Override
	public final boolean incrementToken() throws IOException {
        if (tokenLen > 0) {
            termAtt.setTermBuffer(tokenBuffer, 0, tokenLen);
            tokenLen = 0;
            posIncAtt.setPositionIncrement(0); // since we are just putting the original now
            return true;
        }

		if (!input.incrementToken()) { // reached EOS -- return null
			return false;
		}

		if (suffixByTokenType == null) { // this practically means the filter is disabled
			return true;
		}

		final char[] suffix;
		if (!((suffix = suffixByTokenType.get(typeAtt.type())) != null)) {
			return true;
		}

		char[] buffer = termAtt.termBuffer();
		final int length = termAtt.termLength();
		if (buffer.length <= length) {
			buffer = termAtt.resizeTermBuffer(length + suffix.length);
		}

		System.arraycopy(suffix, 0, buffer, length, suffix.length);
		termAtt.setTermLength(length + suffix.length);

        if (keepOrigin) {
            if (tokenBuffer == null || tokenBuffer.length < length)
                tokenBuffer = buffer.clone();
            else
                System.arraycopy(buffer, 0, tokenBuffer, 0, length);
            tokenLen = length;
        }

		return true;
	}
}