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

import com.code972.hebmorph.hspell.Constants;
import java.io.IOException;
import java.io.Reader;

public class Tokenizer
{

	public static class TokenType
	{
		public static int Hebrew = 1;
		public static int NonHebrew = 2;
		public static int Numeric = 4;
		public static int Construct = 8;
		public static int Acronym = 16;
	}

	public static final char[] Geresh = { '\'', '\u05F3' };
	public static final char[] Gershayim = { '\"', '\u05F4' };
    public static final char[] Makaf = { '-' };
	public static final char[] CharsFollowingPrefixes = concatenateCharArrays(Geresh, Gershayim, Makaf);
	public static final char[] LettersAcceptingGeresh = { 'ז', 'ג', 'ץ', 'צ', 'ח' };

	public static boolean isOfChars(char c, char[] options)
	{
		for (char o : options)
		{
			if (c == o) return true;
		}
		return false;
	}

    public static char[] concatenateCharArrays(char[] ... arrays)
    {
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
	public static boolean isNiqqudChar(char c)
	{
		return ((c >= 1455) && (c <= 1476));
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


	private static final int IO_BUFFER_SIZE = 4096;
	private char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private int ioBufferIndex = 0;

	private char[] wordBuffer = new char[Constants.MaxWordLength];

	public Tokenizer(Reader _input)
	{
		input = _input;
	}

	// Niqqud is not being removed by design, to allow for a future analyzer extension to take advantage of it
	// This is a job for a normalizer, anyway
	public int nextToken(Reference<String> tokenString) throws IOException
	{
		int length = 0;
        tokenOffset = -1; // invalidate
		int tokenType = 0;
		while (true)
		{
			if (ioBufferIndex >= dataLen)
			{
				inputOffset += dataLen;
				dataLen = input.read(ioBuffer, 0, ioBuffer.length);
				if (dataLen <= 0)
				{
					dataLen = 0; // so next offset += dataLen won't decrement offset
					if (length > 0)
					{
						break;
					}
					else
					{
						tokenString.ref = "";
                        tokenLengthInSource = 0;
						return 0;
					}
				}
				ioBufferIndex = 0;
			}

			char c = ioBuffer[ioBufferIndex++];
			boolean appendCurrentChar = false;

			// In case we already consumed at least one char, and started a non-Hebrew token.
			// Since tokenizing non-Hebrew characters correctly is out of scope for this implementation,
			// we will consume all non-spaces and non-panctuation and return them as-is.
			if ((length > 0) && ((tokenType & TokenType.NonHebrew) > 0))
			{
				// No such thing as mixed words; return the current word and go back
				if (((c >= 1488) && (c <= 1514)) || ((c >= 1455) && (c <= 1476))) // HEBREW || NIQQUD
				{
					--ioBufferIndex;
					break;
				}
				else if (Character.isLetterOrDigit(c)) // TODO: break to prevent mixing of non-Hebrew and digits as well?
				{
					appendCurrentChar = true;
				}
				else
				{
					break; // Tokenize on everything else
				}
			}
			else if (isHebrewLetter(c) || ((length > 0) && isNiqqudChar(c))) // HEBREW || (NIQQUD if not first char)
			{
				tokenType |= TokenType.Hebrew;
				appendCurrentChar = true;
			}
			else if (Character.isLetterOrDigit(c))
			{
				// If met while consuming a Hebrew word, we return the current word (no such thing as mixed words)
				if ((length > 0) && ((tokenType & TokenType.Hebrew) > 0))
				{
					--ioBufferIndex;
					break;
				}

				tokenType |= TokenType.NonHebrew;
				if (Character.isDigit(c)) // TODO: break to prevent mixing of non-Hebrew and digits as well?
				{
					tokenType |= TokenType.Numeric;
				}

				appendCurrentChar = true;
			}
			else if (isOfChars(c, Gershayim) && (length > 0))
			{
				// Tokenize if previous char wasn't part of a word
				if (!isHebrewLetter(wordBuffer[length - 1]) && !isNiqqudChar(wordBuffer[length - 1]))
				{
					break;
				}

				// TODO: Is it possible to support cases like שה"שםעצם in the tokenizer?
				tokenType |= TokenType.Acronym;
				appendCurrentChar = true;
			}
			else if (isOfChars(c, Geresh) && (length > 0))
			{
				// Tokenize if previous char wasn't part of a word or another Geresh (which we handle below)
				if (!isHebrewLetter(wordBuffer[length - 1]) && !isNiqqudChar(wordBuffer[length - 1])
						&& !isOfChars(wordBuffer[length - 1], Geresh))
				{
					break;
				}

				// TODO: Is it possible to handle cases which are similar to Merchaot - ה'חלל הפנוי' here?
				tokenType |= TokenType.Acronym;
				appendCurrentChar = true;
			}
			else if (length > 0)
			{
				// Flag makaf connected words as constructs
				if (isOfChars(c, Makaf)) // TODO: Normalize or support other types of dashes too
				{
					tokenType |= TokenType.Construct;
				}
				// TODO: Detect words where Makaf is used for shortening a word (א-ל, י-ם and similar), instead of tokenizing on it

				// at non-Letter w/ chars
				break; // return 'em
			}

			if (appendCurrentChar)
			{
				// Consume normally
				if (length == 0) // mark the start of a new token
				{
                    tokenOffset = inputOffset + ioBufferIndex - 1;
				}
				else if (length == wordBuffer.length)
					// buffer overflow!
				{
					break;
				}

				// Fix a common replacement of double-Geresh with Gershayim; call it Gershayim normalization if you wish
				if (isOfChars(c, Geresh))
				{
					if (wordBuffer[length - 1] == c)
					{
						wordBuffer[length - 1] = '"';
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
            tokenLengthInSource = inputOffset - tokenOffset;
        else
            tokenLengthInSource = inputOffset + ioBufferIndex - 1 - tokenOffset;

		if (isOfChars(wordBuffer[length - 1], Gershayim))
		{
			wordBuffer[--length] = '\0';
            tokenLengthInSource--; // Don't include Gershayim in the offset calculation
		}
		// Geresh trimming; only try this if it isn't one-char in length (without the Geresh)
		if ((length > 2) && isOfChars(wordBuffer[length - 1], Geresh))
		{
			// All letters which this Geresh may mean something for
			if (!isOfChars(wordBuffer[length - 2], LettersAcceptingGeresh))
			{
				wordBuffer[--length] = '\0';
                tokenLengthInSource--; // Don't include this Geresh in the offset calculation
			}
			// TODO: Support marking abbrevations (פרופ') and Hebrew's th (ת')
			// TODO: Handle ה (Hashem)
		}

		tokenString.ref = new String(wordBuffer, 0, length);
		return tokenType;
	}

	public final void reset(Reader _input)
	{
		input = _input;
		inputOffset = 0;
		dataLen = 0;
		ioBufferIndex = 0;
	}
}