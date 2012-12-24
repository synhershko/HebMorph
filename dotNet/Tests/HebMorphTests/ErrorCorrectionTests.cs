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

using Xunit;

namespace HebMorph.Tests
{
	public class ErrorCorrectionTests : TestBase
	{
		private readonly HebMorph.Lemmatizer _lemmatizer;

		public ErrorCorrectionTests() : base()
		{
			_lemmatizer = new Lemmatizer(HspellDict, false);
		}

		[Fact]
		public void SimpleAHVICases()
		{
			// AssertWord("פינגוין", "פינגווין"); // TODO
		}

		public void AssertWord(string word, string expectedWord)
		{
			Assert.True(_lemmatizer.Lemmatize(expectedWord).Count > 0); // make sure the expected word is legal
			var results = _lemmatizer.LemmatizeTolerant(word);
			Assert.True(results.Count > 0);
			Assert.Equal(expectedWord, results[0].Text);
		}
	}
}
