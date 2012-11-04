/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

namespace Lucene.Net.Analysis
{


	/**
	 * A CharFilter that wraps another Reader and attempts to strip out HTML constructs.
	 * 
	 * @version $Id: HTMLStripCharFilter.java 1065312 2011-01-30 16:08:25Z rmuir $
	 */
	public class HTMLStripCharFilter : BaseCharFilter
	{
		private readonly int readAheadLimit = DEFAULT_READ_AHEAD;
		private readonly int safeReadAheadLimit;
		private int numWhitespace = 0;
		private int numRead = 0;
		private int numEaten = 0;
		private int numReturned = 0;
		private int lastMark;
		private readonly HashSet<String> escapedTags;

		// pushback buffer
		private readonly StringBuilder pushed = new StringBuilder();
		//private readonly static int EOF = -1;
		private const int MISMATCH = -2;

		private const int MATCH = -3;
		// temporary buffer
		private readonly StringBuilder sb = new StringBuilder();
		public readonly static int DEFAULT_READ_AHEAD = 8192;

		public HTMLStripCharFilter(CharStream source)
			: base(CharReader.Get(source))
		{
			safeReadAheadLimit = readAheadLimit - 3;
			LoadEntityTable();
		}

		public HTMLStripCharFilter(CharStream source, HashSet<String> escapedTags)
			: base(CharReader.Get(source))
		{
			this.escapedTags = escapedTags;
			safeReadAheadLimit = readAheadLimit - 3;
			LoadEntityTable();
		}

		public HTMLStripCharFilter(CharStream source, HashSet<String> escapedTags, int readAheadLimit)
			: base(CharReader.Get(source))
		{
			this.escapedTags = escapedTags;
			this.readAheadLimit = readAheadLimit;
			safeReadAheadLimit = readAheadLimit - 3;
			LoadEntityTable();
		}

		public int GetReadAheadLimit()
		{
			return readAheadLimit;
		}

		private int Next()
		{
			var len = pushed.Length;
			if (len > 0)
			{
				int ch = pushed[len - 1];
				pushed.Length = len - 1;
				return ch;
			}
			numRead++;
			var c = input.Read();
			if (isBuffering) buffer.Append((char)c);
			return c;
		}

		private int NextSkipWS()
		{
			var ch = Next();
			while (IsSpace(ch)) ch = Next();
			return ch;
		}

		private new int Peek()
		{
			var len = pushed.Length;
			if (len > 0)
			{
				return pushed[len - 1];
			}
			numRead++;
			var ch = input.Read();
			Push((char)ch);
			if (isBuffering) buffer.Append((char) ch);
			return ch;
		}

		private void Push(char ch)
		{
			pushed.Append(ch);
		}

		private static bool IsSpace(int ch)
		{
			switch (ch)
			{
				case ' ':
				case '\n':
				case '\r':
				case '\t': return true;
				default: return false;
			}
		}

		private static bool IsHex(int ch)
		{
			return (ch >= '0' && ch <= '9') ||
				   (ch >= 'A' && ch <= 'Z') ||
				   (ch >= 'a' && ch <= 'z');
		}

		private static bool IsAlpha(int ch)
		{
			return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
		}

		private static bool IsDigit(int ch)
		{
			return ch >= '0' && ch <= '9';
		}

		/*** From HTML 4.0
		[4]     NameChar     ::=    Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender
		[5]     Name     ::=    (Letter | '_' | ':') (NameChar)*
		[6]     Names    ::=    Name (#x20 Name)*
		[7]     Nmtoken    ::=    (NameChar)+
		[8]     Nmtokens     ::=    Nmtoken (#x20 Nmtoken)*
		***/

		// should I include all id chars allowable by HTML/XML here?
		// including accented chars, ':', etc?
		private static bool IsIdChar(int ch)
		{
			// return Character.isUnicodeIdentifierPart(ch);
			// isUnicodeIdentiferPart doesn't include '-'... shoudl I still
			// use it and add in '-',':',etc?
			return IsAlpha(ch) || IsDigit(ch) || ch == '.' ||
					ch == '-' || ch == '_' || ch == ':'
					|| Char.IsLetter((char)ch);

		}

		private static bool IsFirstIdChar(int ch)
		{
			// isUnicodeIdentifierStart(ch);
			return char.IsLetter((char)ch) || char.IsNumber((char)ch) || ch == '_';
		}

		private bool isBuffering;
		private readonly StringBuilder buffer = new StringBuilder();

		private void SaveState()
		{
			lastMark = numRead;
			//input.Mark(readAheadLimit);
			isBuffering = true;
			buffer.Length = 0;
		}

		private void RestoreState()
		{
			//input.Reset();
			pushed.Length = 0;
			if (!isBuffering) return;

			foreach (var c in buffer.ToString().ToCharArray())
			{
				pushed.Insert(0, c);
			}
			isBuffering = false;
		}

		private static NumberStyles GetNumberStyle(int radixBase)
		{
			if (radixBase == 16) return NumberStyles.HexNumber;
			return NumberStyles.Integer;
		}

		private int ReadNumericEntity()
		{
			// "&#" has already been read at this point
			var eaten = 2;

			// is this decimal, hex, or nothing at all.
			var ch = Next();
			var radixBase = 10;
			//bool invalid = false;
			sb.Length = 0;

			if (IsDigit(ch))
			{
				// decimal character entity
				sb.Append((char)ch);
				for (var i = 0; i < 10; i++)
				{
					ch = Next();
					if (IsDigit(ch))
					{
						sb.Append((char)ch);
					}
					else
					{
						break;
					}
				}
			}
			else if (ch == 'x')
			{
				eaten++;
				// hex character entity
				radixBase = 16;
				sb.Length = 0;
				for (var i = 0; i < 10; i++)
				{
					ch = Next();
					if (IsHex(ch))
					{
						sb.Append((char)ch);
					}
					else
					{
						break;
					}
				}
			}
			else
			{
				return MISMATCH;
			}


			// In older HTML, an entity may not have always been terminated
			// with a semicolon.  We'll also treat EOF or whitespace as terminating
			// the entity.
			try
			{
				if (ch == ';' || ch == -1)
				{
					// do not account for the eaten ";" due to the fact that we do output a char
					numWhitespace = sb.Length + eaten;
					return Int32.Parse(sb.ToString(), GetNumberStyle(radixBase));
				}

				// if whitespace terminated the entity, we need to return
				// that whitespace on the next call to read().
				if (IsSpace(ch))
				{
					Push((char)ch);
					numWhitespace = sb.Length + eaten;
					return Int32.Parse(sb.ToString(), GetNumberStyle(radixBase));
				}
			}
			catch (FormatException)
			{
				return MISMATCH;
			}

			// Not an entity...
			return MISMATCH;
		}

		private int ReadEntity()
		{
			var ch = Next();
			if (ch == '#') return ReadNumericEntity();

			//read an entity reference

			// for an entity reference, require the ';' for safety.
			// otherwise we may try and convert part of some company
			// names to an entity.  "Alpha&Beta Corp" for instance.
			//
			// TODO: perhaps I should special case some of the
			// more common ones like &amp to make the ';' optional...

			sb.Length = 0;
			sb.Append((char)ch);

			for (var i = 0; i < safeReadAheadLimit; i++)
			{
				ch = Next();
				if (Char.IsLetter((char)ch))
				{
					sb.Append((char)ch);
				}
				else
				{
					break;
				}
			}

			if (ch == ';')
			{
				var entity = sb.ToString();
				char entityChar;
				if (entityTable.TryGetValue(entity, out entityChar))
				{
					numWhitespace = entity.Length + 1;
					return entityChar;
				}
			}

			return MISMATCH;
		}

		/*** valid comments according to HTML specs
		 <!-- Hello -->
		 <!-- Hello -- -- Hello-->
		 <!---->
		 <!------ Hello -->
		 <!>
		 <!------> Hello -->

		 #comments inside of an entity decl:
		 <!ENTITY amp     CDATA "&#38;"   -- ampersand, U+0026 ISOnum -->

		 Turns out, IE & mozilla don't parse comments correctly.
		 Since this is meant to be a practical stripper, I'll just
		 try and duplicate what the browsers do.

		 <!-- (stuff_including_markup)* -->
		 <!FOO (stuff, not including markup) >
		 <! (stuff, not including markup)* >


		***/

		private int ReadBang(bool inScript)
		{
			// at this point, "<!" has been read
			var ret = ReadComment(inScript);
			if (ret == MATCH) return MATCH;

			if ((numRead - lastMark) < safeReadAheadLimit || Peek() == '>')
			{

				var ch = Next();
				if (ch == '>') return MATCH;

				// if it starts with <! and isn't a comment,
				// simply read until ">"
				//since we did readComment already, it may be the case that we are already deep into the read ahead buffer
				//so, we may need to abort sooner
				while ((numRead - lastMark) < safeReadAheadLimit)
				{
					ch = Next();
					if (ch == '>')
					{
						return MATCH;
					}
					if (ch < 0)
					{
						return MISMATCH;
					}
				}
			}
			return MISMATCH;
		}

		// tries to read comments the way browsers do, not
		// strictly by the standards.
		//
		// GRRRR.  it turns out that in the wild, a <script> can have a HTML comment
		// that contains a script that contains a quoted comment.
		// <script><!-- document.write("<!--embedded comment-->") --></script>
		//
		private int ReadComment(bool inScript)
		{
			// at this point "<!" has  been read
			var ch = (char)Next();
			if (ch != '-')
			{
				// not a comment
				Push(ch);
				return MISMATCH;
			}

			ch = (char)Next();
			if (ch != '-')
			{
				// not a comment
				Push(ch);
				Push('-');
				return MISMATCH;
			}
			/*two extra calls to Next() here, so make sure we don't read past our mark*/
			while ((numRead - lastMark) < safeReadAheadLimit - 3)
			{
				ch = (char)Next();
				if (ch < 0) return MISMATCH;
				if (ch == '-')
				{
					ch = (char)Next();
					if (ch < 0) return MISMATCH;
					if (ch != '-')
					{
						Push(ch);
						continue;
					}

					ch = (char)Next();
					if (ch < 0) return MISMATCH;
					if (ch != '>')
					{
						Push(ch);
						Push('-');
						continue;
					}

					return MATCH;
				}
				if ((ch == '\'' || ch == '"') && inScript)
				{
					Push(ch);
					ReadScriptString();
					// if this wasn't a string, there's not much we can do
					// at this point without having a stack of stream states in
					// order to "undo" just the latest.
				}
				else if (ch == '<')
				{
					EatSSI();
				}

			}
			return MISMATCH;

		}

		private int ReadTag()
		{
			// at this point '<' has already been read
			var ch = Next();
			if (!IsAlpha(ch))
			{
				Push((char)ch);
				return MISMATCH;
			}

			sb.Length = 0;
			sb.Append((char)ch);
			while ((numRead - lastMark) < safeReadAheadLimit)
			{
				ch = Next();
				if (IsIdChar(ch))
				{
					sb.Append((char)ch);
				}
				else if (ch == '/')
				{
					// Hmmm, a tag can close with "/>" as well as "/ >"
					// read end tag '/>' or '/ >', etc
					return NextSkipWS() == '>' ? MATCH : MISMATCH;
				}
				else
				{
					break;
				}
			}
			if (escapedTags != null && escapedTags.Contains(sb.ToString()))
			{
				//if this is a reservedTag, then keep it
				return MISMATCH;
			}
			// After the tag id, there needs to be either whitespace or
			// '>'
			if (!(ch == '>' || IsSpace(ch)))
			{
				return MISMATCH;
			}

			if (ch != '>')
			{
				// process attributes
				while ((numRead - lastMark) < safeReadAheadLimit)
				{
					ch = Next();
					if (IsSpace(ch))
					{
						continue;
					}
					if (IsFirstIdChar(ch))
					{
						Push((char)ch);
						var ret = ReadAttr2();
						if (ret == MISMATCH) return ret;
					}
					else if (ch == '/')
					{
						// read end tag '/>' or '/ >', etc
						return NextSkipWS() == '>' ? MATCH : MISMATCH;
					}
					else if (ch == '>')
					{
						break;
					}
					else
					{
						return MISMATCH;
					}

				}
				if ((numRead - lastMark) >= safeReadAheadLimit)
				{
					return MISMATCH;//exit out if we exceeded the buffer
				}
			}

			// We only get to this point after we have read the
			// entire tag.  Now let's see if it's a special tag.
			var name = sb.ToString();
			if (name.Equals("script", StringComparison.InvariantCultureIgnoreCase) || name.Equals("style", StringComparison.InvariantCultureIgnoreCase))
			{
				// The content of script and style elements is
				//  CDATA in HTML 4 but PCDATA in XHTML.

				/* From HTML4:
				  Although the STYLE and SCRIPT elements use CDATA for their data model,
				  for these elements, CDATA must be handled differently by user agents.
				  Markup and entities must be treated as raw text and passed to the application
				  as is. The first occurrence of the character sequence "</" (end-tag open
				  delimiter) is treated as terminating the end of the element's content. In
				  valid documents, this would be the end tag for the element.
				 */

				// discard everything until endtag is hit (except
				// if it occurs in a comment.

				// reset the stream mark to here, since we know that we sucessfully matched
				// a tag, and if we can't find the end tag, this is where we will want
				// to roll back to.
				SaveState();
				pushed.Length = 0;
				return FindEndTag();
			}
			return MATCH;
		}


		// find an end tag, but beware of comments...
		// <script><!-- </script> -->foo</script>
		// beware markup in script strings: </script>...document.write("</script>")foo</script>
		// TODO: do I need to worry about CDATA sections "<![CDATA["  ?
		int FindEndTag()
		{

			while ((numRead - lastMark) < safeReadAheadLimit)
			{
				var ch = Next();
				if (ch == '<')
				{
					ch = Next();
					// skip looking for end-tag in comments
					if (ch == '!')
					{
						if (ReadBang(true) == MATCH) continue;
						// yikes... what now?  It wasn't a comment, but I can't get
						// back to the state I was at.  Just continue from where I
						// am I guess...
						continue;
					}
					// did we match "</"
					if (ch != '/')
					{
						Push((char)ch);
						continue;
					}
					var ret = ReadName(false);
					if (ret == MISMATCH) return MISMATCH;
					ch = NextSkipWS();
					if (ch != '>') return MISMATCH;
					return MATCH;
				}
				if (ch == '\'' || ch == '"')
				{
					// read javascript string to avoid a false match.
					Push((char)ch);
					var ret = ReadScriptString();
					// what to do about a non-match (non-terminated string?)
					// play it safe and index the rest of the data I guess...
					if (ret == MISMATCH) return MISMATCH;
				}
				else if (ch < 0)
				{
					return MISMATCH;
				}

			}
			return MISMATCH;
		}


		// read a string escaped by backslashes
		private int ReadScriptString()
		{
			var quoteChar = Next();
			if (quoteChar != '\'' && quoteChar != '"') return MISMATCH;

			while ((numRead - lastMark) < safeReadAheadLimit)
			{
				var ch = Next();
				if (ch == quoteChar) return MATCH;
				if (ch == '\\')
				{
					Next();
				}
				else if (ch < 0)
				{
					return MISMATCH;
				}
				else if (ch == '<')
				{
					EatSSI();
				}
			}
			return MISMATCH;
		}


		private int ReadName(bool checkEscaped)
		{
			var builder = (checkEscaped && escapedTags != null) ? new StringBuilder() : null;
			var ch = Next();
			if (builder != null) builder.Append((char)ch);
			if (!IsFirstIdChar(ch)) return MISMATCH;
			ch = Next();
			if (builder != null) builder.Append((char)ch);
			while (IsIdChar(ch))
			{
				ch = Next();
				if (builder != null) builder.Append((char)ch);
			}
			if (ch != -1)
			{
				Push((char)ch);

			}
			//strip off the trailing >
			if (builder != null && escapedTags.Contains(builder.ToString().Substring(0, builder.Length - 1)))
			{
				return MISMATCH;
			}
			return MATCH;
		}

		/***
		[10]    AttValue     ::=    '"' ([^<&"] | Reference)* '"'
			  |  "'" ([^<&'] | Reference)* "'"

		need to also handle unquoted attributes, and attributes w/o values:
		<td id=msviGlobalToolbar height="22" nowrap align=left>

		***/

		// This reads attributes and attempts to handle any
		// embedded server side includes that would otherwise
		// mess up the quote handling.
		//  <a href="a/<!--#echo "path"-->">
		private int ReadAttr2()
		{
			if ((numRead - lastMark < safeReadAheadLimit))
			{
				var ch = Next();
				if (!IsFirstIdChar(ch)) return MISMATCH;
				ch = Next();
				while (IsIdChar(ch) && ((numRead - lastMark) < safeReadAheadLimit))
				{
					ch = Next();
				}
				if (IsSpace(ch)) ch = NextSkipWS();

				// attributes may not have a value at all!
				// if (ch != '=') return MISMATCH;
				if (ch != '=')
				{
					Push((char)ch);
					return MATCH;
				}

				var quoteChar = NextSkipWS();

				if (quoteChar == '"' || quoteChar == '\'')
				{
					while ((numRead - lastMark) < safeReadAheadLimit)
					{
						ch = Next();
						if (ch < 0) return MISMATCH;
						if (ch == '<')
						{
							EatSSI();
						}
						else if (ch == quoteChar)
						{
							return MATCH;
							//} else if (ch=='<') {
							//  return MISMATCH;
						}

					}
				}
				else
				{
					// unquoted attribute
					while ((numRead - lastMark) < safeReadAheadLimit)
					{
						ch = Next();
						if (ch < 0) return MISMATCH;
						if (IsSpace(ch))
						{
							Push((char)ch);
							return MATCH;
						}
						if (ch == '>')
						{
							Push((char)ch);
							return MATCH;
						}
						if (ch == '<')
						{
							EatSSI();
						}
					}
				}
			}
			return MISMATCH;
		}

		// skip past server side include
		private int EatSSI()
		{
			// at this point, only a "<" was read.
			// on a mismatch, push back the last char so that if it was
			// a quote that closes the attribute, it will be re-read and matched.
			var ch = Next();
			if (ch != '!')
			{
				Push((char)ch);
				return MISMATCH;
			}
			ch = Next();
			if (ch != '-')
			{
				Push((char)ch);
				return MISMATCH;
			}
			ch = (char)Next();
			if (ch != '-')
			{
				Push((char)ch);
				return MISMATCH;
			}
			ch = Next();
			if (ch != '#')
			{
				Push((char)ch);
				return MISMATCH;
			}

			Push('#'); Push('-'); Push('-');
			return ReadComment(false);
		}

		private int ReadProcessingInstruction()
		{
			// "<?" has already been read
			while ((numRead - lastMark) < safeReadAheadLimit)
			{
				var ch = Next();
				if (ch == '?' && Peek() == '>')
				{
					Next();
					return MATCH;
				}
				if (ch == -1)
				{
					return MISMATCH;
				}

			}
			return MISMATCH;
		}


		public override int Read()
		{
			// TODO: Do we ever want to preserve CDATA sections?
			// where do we have to worry about them?
			// <![ CDATA [ unescaped markup ]]>
			if (numWhitespace > 0)
			{
				numEaten += numWhitespace;
				AddOffCorrectMap(numReturned, numEaten);
				numWhitespace = 0;
			}
			numReturned++;
			//do not limit this one by the READAHEAD
			while (true)
			{
				var lastNumRead = numRead;
				var ch = Next();

				switch ((char)ch)
				{
					case '&':
						SaveState();
						ch = ReadEntity();
						if (ch >= 0) return ch;
						if (ch == MISMATCH)
						{
							RestoreState();

							return '&';
						}
						break;

					case '<':
						SaveState();
						ch = Next();
						var ret = MISMATCH;
						if (ch == '!')
						{
							ret = ReadBang(false);
						}
						else if (ch == '/')
						{
							ret = ReadName(true);
							if (ret == MATCH)
							{
								ch = NextSkipWS();
								ret = ch == '>' ? MATCH : MISMATCH;
							}
						}
						else if (IsAlpha(ch))
						{
							Push((char)ch);
							ret = ReadTag();
						}
						else if (ch == '?')
						{
							ret = ReadProcessingInstruction();
						}

						// matched something to be discarded, so break
						// from this case and continue in the loop
						if (ret == MATCH)
						{
							//break;//was
							//return whitespace from
							numWhitespace = (numRead - lastNumRead) - 1;//tack on the -1 since we are returning a space right now
							return ' ';
						}

						// didn't match any HTML constructs, so roll back
						// the stream state and just return '<'
						RestoreState();
						return '<';

					default: return ch;
				}

			}


		}

		public override int Read(char[] cbuf, int off, int len)
		{
			int i;
			for (i = 0; i < len; i++)
			{
				var ch = Read();
				if (ch == -1) break;
				cbuf[off++] = (char)ch;
			}
			if (i == 0)
			{
				if (len == 0) return 0;
				return -1;
			}
			return i;
		}

		public override void Close()
		{
			input.Close();
		}


		private static readonly string[] entityName = new[] { "zwnj", "aring", "gt", "yen", "ograve", "Chi", "delta", "rang", "sup", "trade", "Ntilde", "xi", "upsih", "nbsp", "Atilde", "radic", "otimes", "aelig", "oelig", "equiv", "ni", "infin", "Psi", "auml", "cup", "Epsilon", "otilde", "lt", "Icirc", "Eacute", "Lambda", "sbquo", "Prime", "prime", "psi", "Kappa", "rsaquo", "Tau", "uacute", "ocirc", "lrm", "zwj", "cedil", "Alpha", "not", "amp", "AElig", "oslash", "acute", "lceil", "alefsym", "laquo", "shy", "loz", "ge", "Igrave", "nu", "Ograve", "lsaquo", "sube", "euro", "rarr", "sdot", "rdquo", "Yacute", "lfloor", "lArr", "Auml", "Dagger", "brvbar", "Otilde", "szlig", "clubs", "diams", "agrave", "Ocirc", "Iota", "Theta", "Pi", "zeta", "Scaron", "frac14", "egrave", "sub", "iexcl", "frac12", "ordf", "sum", "prop", "Uuml", "ntilde", "atilde", "asymp", "uml", "prod", "nsub", "reg", "rArr", "Oslash", "emsp", "THORN", "yuml", "aacute", "Mu", "hArr", "le", "thinsp", "dArr", "ecirc", "bdquo", "Sigma", "Aring", "tilde", "nabla", "mdash", "uarr", "times", "Ugrave", "Eta", "Agrave", "chi", "real", "circ", "eth", "rceil", "iuml", "gamma", "lambda", "harr", "Egrave", "frac34", "dagger", "divide", "Ouml", "image", "ndash", "hellip", "igrave", "Yuml", "ang", "alpha", "frasl", "ETH", "lowast", "Nu", "plusmn", "bull", "sup1", "sup2", "sup3", "Aacute", "cent", "oline", "Beta", "perp", "Delta", "there4", "pi", "iota", "empty", "euml", "notin", "iacute", "para", "epsilon", "weierp", "OElig", "uuml", "larr", "icirc", "Upsilon", "omicron", "upsilon", "copy", "Iuml", "Oacute", "Xi", "kappa", "ccedil", "Ucirc", "cap", "mu", "scaron", "lsquo", "isin", "Zeta", "minus", "deg", "and", "tau", "pound", "curren", "int", "ucirc", "rfloor", "ensp", "crarr", "ugrave", "exist", "cong", "theta", "oplus", "permil", "Acirc", "piv", "Euml", "Phi", "Iacute", "quot", "Uacute", "Omicron", "ne", "iquest", "eta", "rsquo", "yacute", "Rho", "darr", "Ecirc", "Omega", "acirc", "sim", "phi", "sigmaf", "macr", "thetasym", "Ccedil", "ordm", "uArr", "forall", "beta", "fnof", "rho", "micro", "eacute", "omega", "middot", "Gamma", "rlm", "lang", "spades", "supe", "thorn", "ouml", "or", "raquo", "part", "sect", "ldquo", "hearts", "sigma", "oacute" };
		private static readonly int[] entityVal = new[] { 8204, 229, 62, 165, 242, 935, 948, 9002, 8835, 8482, 209, 958, 978, 160, 195, 8730, 8855, 230, 339, 8801, 8715, 8734, 936, 228, 8746, 917, 245, 60, 206, 201, 923, 8218, 8243, 8242, 968, 922, 8250, 932, 250, 244, 8206, 8205, 184, 913, 172, 38, 198, 248, 180, 8968, 8501, 171, 173, 9674, 8805, 204, 957, 210, 8249, 8838, 8364, 8594, 8901, 8221, 221, 8970, 8656, 196, 8225, 166, 213, 223, 9827, 9830, 224, 212, 921, 920, 928, 950, 352, 188, 232, 8834, 161, 189, 170, 8721, 8733, 220, 241, 227, 8776, 168, 8719, 8836, 174, 8658, 216, 8195, 222, 255, 225, 924, 8660, 8804, 8201, 8659, 234, 8222, 931, 197, 732, 8711, 8212, 8593, 215, 217, 919, 192, 967, 8476, 710, 240, 8969, 239, 947, 955, 8596, 200, 190, 8224, 247, 214, 8465, 8211, 8230, 236, 376, 8736, 945, 8260, 208, 8727, 925, 177, 8226, 185, 178, 179, 193, 162, 8254, 914, 8869, 916, 8756, 960, 953, 8709, 235, 8713, 237, 182, 949, 8472, 338, 252, 8592, 238, 933, 959, 965, 169, 207, 211, 926, 954, 231, 219, 8745, 956, 353, 8216, 8712, 918, 8722, 176, 8743, 964, 163, 164, 8747, 251, 8971, 8194, 8629, 249, 8707, 8773, 952, 8853, 8240, 194, 982, 203, 934, 205, 34, 218, 927, 8800, 191, 951, 8217, 253, 929, 8595, 202, 937, 226, 8764, 966, 962, 175, 977, 199, 186, 8657, 8704, 946, 402, 961, 181, 233, 969, 183, 915, 8207, 9001, 9824, 8839, 254, 246, 8744, 187, 8706, 167, 8220, 9829, 963, 243 };
		private static readonly Dictionary<string, char> entityTable = new Dictionary<string, char>();
		private static void LoadEntityTable()
		{
			if (entityTable.Count > 0)
				return;

			// entityName and entityVal generated from the python script
			// included in comments at the end of this file.
			for (var i = 0; i < entityName.Length; i++)
			{
				entityTable.Add(entityName[i], (char)entityVal[i]);
			}
			// special-case nbsp to a simple space instead of 0xa0
			entityTable["nbsp"] = ' ';
		}

	}

	/********************* htmlentity.py **********************
	# a simple python script to generate an HTML entity table
	# from text taken from http://www.w3.org/TR/REC-html40/sgml/entities.html

	For bravity, the script has been removed and can be found in Solr's HTMLStripCharFilter.java

	********************** end htmlentity.py ********************/
}
