/***************************************************************************
 *   Copyright (C) 2010-2013 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *		Ofer Fort <oferiko at gmail dot com> (initial Java port)           *
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
package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.hspell.Constants;
import com.code972.hebmorph.hspell.LingInfo;

import java.io.IOException;
import java.io.Reader;

public class Tokenizer {

    public static class TokenType {
		public static int Hebrew = 1;
		public static int NonHebrew = 2;
		public static int Numeric = 4;
        public static int Mixed = 8;
		public static int Construct = 16;
		public static int Acronym = 32;
        public static int Exact = 64;
        public static int Custom = 128;
	}

	public static final char[] Geresh = { '\'', '\u05F3', '\u2018', '\u2019', '\u201B' };
	public static final char[] Gershayim = { '\"', '\u05F4', '\u201C', '\u201D', '\u201F', '\u275E', '\uFF02' };
    public static final char[] Makaf = { '-', '\u2012', '\u2013', '\u2014', '\u2015', '\u05BE' };
	public static final char[] CharsFollowingPrefixes = concatenateCharArrays(Geresh, Gershayim, Makaf);
	public static final char[] LettersAcceptingGeresh = { 'ז', 'ג', 'ץ', 'צ', 'ח' };

	public static boolean isOfChars(char c, char[] options)
	{
		for (char o : options) {
			if (c == o) return true;
		}
		return false;
	}

    public static char[] concatenateCharArrays(char[] ... arrays) {
        int count = 0;
        for(char[] a : arrays) {
            count += a.length;
        }

        char[] ret = new char[count];
        int offs = 0;
        for (char[] a : arrays) {
            System.arraycopy(a, 0, ret, offs, a.length);
            offs += a.length;
        }

        return ret;
    }

	public static boolean isHebrewLetter(char c)
	{
		return ((c >= 1488) && (c <= 1514));
	}
    public static boolean isFinalHebrewLetter(char c)
    {
        return (c == 1507 || c == 1498 || c == 1501 || c == 1509 || c == 1503);
    }
	public static boolean isNiqqudChar(char c)
	{
		return ((c >= 1456) && (c <= 1465)) || (c == '\u05C1' || c == '\u05C2' || c == '\u05BB' || c == '\u05BC');
	}

	private Reader input;
	private int dataLen = 0, inputOffset = 0;

    /// Both are necessary since the tokenizer does some normalization when necessary, and therefore
    /// it isn't always possible to get correct end-offset by looking at the length of the returned token
    /// string
    private int tokenOffset = 0, tokenLengthInSource = 0;
	public final int getOffset()
	{
		return tokenOffset;
	}
	public void setOffset(int offset)
	{
		tokenOffset = offset;
	}

    public int getLengthInSource() {
        return tokenLengthInSource;
    }
    public void setLengthInSource(int length) {
        tokenLengthInSource = length;
    }

    private Character suffixForExactMatch = null;
    public Character getSuffixForExactMatch() {
        return suffixForExactMatch;
    }
    public void setSuffixForExactMatch(final Character suffixForExactMatch) {
        this.suffixForExactMatch = suffixForExactMatch;
    }

    private final DictRadix<Integer> hebrewPrefixes;
    private final DictRadix<Byte> specialCases;
    private static final Byte dummyData = new Byte((byte)0);
    public void addSpecialCase(final String token) {
        specialCases.addNode(token, dummyData);
    }

    public static boolean isLegalPrefix(final char[] prefix, int length, final DictRadix<Integer> prefixesTree) {
        try {
            prefixesTree.lookup(prefix, 0, length, false);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static final int IO_BUFFER_SIZE = 4096;
	private char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private int ioBufferIndex = 0;

	private final char[] wordBuffer = new char[Constants.MaxWordLength];

	public Tokenizer(final Reader input) {
		this(input, null);
	}

    public Tokenizer(final Reader input, final DictRadix<Byte> specialCases) {
        this.input = input;
        this.specialCases = specialCases != null ? specialCases : new DictRadix<Byte>(false);
        hebrewPrefixes = LingInfo.buildPrefixTree(false);
    }

    private boolean isRecognizedException(char[] prefix, byte length, char c) {
        char[] tmp = new char[length + 1];
        System.arraycopy(prefix, 0, tmp, 0, length);
        tmp[length] = c;
        return isRecognizedException(tmp, (byte)(length + 1));
    }

    private boolean isRecognizedException(char[] prefix, byte length) {
        int i = 0;
        while (isHebrewLetter(prefix[i])) {
            if (i >= prefix.length || !isLegalPrefix(prefix, i + 1, hebrewPrefixes)) {
                i = 0;
                break;
            }
            i++;
        }

        try {
            specialCases.lookup(prefix, i, length - i, true);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

	// Niqqud is not being removed by design, to allow for a future analyzer extension to take advantage of it
	// This is a job for a normalizer, anyway
	public int nextToken(final Reference<String> tokenString) throws IOException {
		byte length = 0;
        tokenOffset = 0; // invalidate
		int tokenType = 0;
        byte startedDoingCustomToken = -1;
		while (true) {
			if (ioBufferIndex >= dataLen) {
				inputOffset += dataLen;
				dataLen = input.read(ioBuffer, 0, ioBuffer.length);
				if (dataLen <= 0) {
					dataLen = 0; // so next offset += dataLen won't decrement offset
					if (length > 0) {
                        if ((tokenType & TokenType.Custom) > 0) {
                            if (!isRecognizedException(wordBuffer, length)) {
                                tokenString.ref = "";
                                tokenLengthInSource = 0;
                                tokenOffset = inputOffset;
                                return 0;
                            }
                        }
						break;
					} else {
						tokenString.ref = "";
                        tokenLengthInSource = 0;
                        tokenOffset = inputOffset;
						return 0;
					}
				}
				ioBufferIndex = 0;
			}

			char c = ioBuffer[ioBufferIndex++];
			boolean appendCurrentChar = false;

            if (length == 0) { // first char, figure out what it is
                if (isHebrewLetter(c)) {
                    if (!isFinalHebrewLetter(c)) {
                        tokenType |= TokenType.Hebrew;
                        appendCurrentChar = true;
                    }
                } else if (Character.isLetterOrDigit(c)) {
                    tokenType |= TokenType.NonHebrew;
                    if (Character.isDigit(c))
                        tokenType |= TokenType.Numeric;

                    appendCurrentChar = true;
                }
                // Everything else will be ignored
            } else { // we should consume every letter or digit, and tokenize on everything else
                if ((tokenType & TokenType.Custom) > 0 && !Character.isSpaceChar(c)) {
                    wordBuffer[length] = c;
                    if (!isRecognizedException(wordBuffer, (byte)(length + 1))) {
                        tokenType &= ~TokenType.Custom;
                        length = startedDoingCustomToken;
                        ioBufferIndex--;
                        break;
                    }
                    appendCurrentChar = true;
                } else if (isHebrewLetter(c) || isNiqqudChar(c)) {
                    appendCurrentChar = true;
                } else if (Character.isLetterOrDigit(c)) {
                    if (tokenType == TokenType.Hebrew)
                        tokenType |= TokenType.Mixed;
                    appendCurrentChar = true;
                } else if (isOfChars(c, Gershayim)) {
                    c = '"';
                    // Tokenize if previous char wasn't part of a word
                    if (!isHebrewLetter(wordBuffer[length - 1]) && !isNiqqudChar(wordBuffer[length - 1]))
                        break;

                    // TODO: Is it possible to support cases like שה"שםעצם in the tokenizer?
                    tokenType |= TokenType.Acronym;
                    appendCurrentChar = true;
                } else if (isOfChars(c, Geresh)) {
                    c = '\'';
                    // Tokenize if previous char wasn't part of a word or another Geresh (which we handle below)
                    // and only do this for Hebrew tokens
                    if ((tokenType & TokenType.Hebrew) > 0) {
                        // TODO: Is it possible to handle cases which are similar to Merchaot - ה'חלל הפנוי' here?
                        if (!isHebrewLetter(wordBuffer[length - 1]) && !isNiqqudChar(wordBuffer[length - 1])
                                && !isOfChars(wordBuffer[length - 1], Geresh))
                            break;
                    }

                    appendCurrentChar = true;
                } else if (!Character.isSpaceChar(c) && isRecognizedException(wordBuffer, length, c)) {
                    startedDoingCustomToken = length;
                    tokenType |= TokenType.Custom;
                    appendCurrentChar = true;
                } else {
                    // Flag makaf connected words as constructs
                    if (isOfChars(c, Makaf)) {
                        tokenType |= TokenType.Construct;
                        c = '-';
                        // TODO: Detect words where Makaf is used for shortening a word (א-ל, י-ם and similar), instead of tokenizing on it
                    } else if (suffixForExactMatch != null && suffixForExactMatch.equals(c)) {
                        tokenType |= TokenType.Exact;
                    }

                    // at non-Letter w/ chars
                    break; // return 'em
                }
            }

			if (appendCurrentChar) {
                // Consume normally
                if (length == 0) { // mark the start of a new token
                    tokenOffset = inputOffset + ioBufferIndex - 1;
                } else if (length == wordBuffer.length - 1) { // clip lengthy tokens
                    continue;
                }
                // Note that tokens larger than 128 chars will get clipped.

                // Fix a common replacement of double-Geresh with Gershayim; call it Gershayim normalization if you wish
                if (isOfChars(c, Geresh))
                {
                    if (wordBuffer[length - 1] == c)
                    {
                        wordBuffer[length - 1] = '"';
                        tokenType |= TokenType.Acronym;
                    }
                    //					else if (isOfChars(wordBuffer[length - 1], LettersAcceptingGeresh))
                    //					{
                    //						wordBuffer[length++] = c;
                    //					}
                    else
                        wordBuffer[length++] = c;
                }
                else
                {
                    wordBuffer[length++] = c; // TODO: Normalize c
                }
            }
        }

        // Store token's actual length in source (regardless of misc normalizations)
        if (dataLen <= 0)
            tokenLengthInSource = Math.max(inputOffset - tokenOffset, 0);
        else {
            tokenLengthInSource = Math.max(inputOffset + ioBufferIndex - 1 - tokenOffset, 0);
        }

		if (isOfChars(wordBuffer[length - 1], Gershayim))
		{
			wordBuffer[--length] = '\0';
            tokenLengthInSource = Math.max(tokenLengthInSource - 1, 0); // Don't include Gershayim in the offset calculation
		}
		// Geresh trimming; only try this if it isn't one-char in length (without the Geresh)
		if ((length > 2) && wordBuffer[length - 1] == '\'')
		{
			// All letters which this Geresh may mean something for
			if (((tokenType & TokenType.Hebrew) == 0) || !isOfChars(wordBuffer[length - 2], LettersAcceptingGeresh))
			{
				wordBuffer[--length] = '\0';
                tokenLengthInSource = Math.max(tokenLengthInSource - 1, 0); // Don't include this Geresh in the offset calculation
			}
			// TODO: Support marking abbrevations (פרופ') and Hebrew's th (ת')
			// TODO: Handle ה (Hashem)
		}

		tokenString.ref = new String(wordBuffer, 0, length);
		return tokenType;
	}

    public final void reset(final Reader _input) {
		input = _input;
		inputOffset = 0;
		dataLen = 0;
		ioBufferIndex = 0;
        tokenOffset = 0;
        tokenLengthInSource = 0;
	}
}
