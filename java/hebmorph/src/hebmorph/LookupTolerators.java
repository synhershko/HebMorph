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
package hebmorph;

/**
 Contains static tolerator function driving the tolerant lookup in HebMorph's dictionary radix

*/
public final class LookupTolerators
{
	/**
	 Toleration function used in HebMorph's tolerant dictionary lookups. Returns positive number (or zero)
	 if tolerance was made, null if not.
	 A tolerator function may also increment keyPos, which is provided by reference. For cases where a tolerator
	 function returns 0, it *MUST* increment keyPos, or an endless recursion will be initiated.

	*/
//	public delegate Nullable<byte> ToleranceFuncDelegate(char[] key, ref byte keyPos, string word, ref float score, char curChar);

	/**
	 Tolerate all standard Em Kriya appearances (Yud [incl. doubling], Vav, and double consonant Vav)

	*/
//	public static final ToleranceFuncDelegate[] TolerateEmKryiaAll = { TolerateNonDoubledConsonantVav, TolerateEmKryiaVav, TolerateEmKryiaYud };

	/**
	 Current trie position is Yud, while the requested key does not contain Yud at this position, or
	 contains Yud but not twice.

	*/

	public static final ToleranceFunction[] TolerateEmKryiaAll = { new LookupTolerators.TolerateEmKryiaYud(), new LookupTolerators.TolerateEmKryiaVav(), new LookupTolerators.TolerateNonDoubledConsonantVav() };

	public interface ToleranceFunction
	{
		public Integer tolerate(char[] key, Reference<Byte> keyPos, String word, Reference<Float> score, char curChar);
	}

	public static class TolerateEmKryiaYud implements ToleranceFunction
	{
		public Integer tolerate(char[] key, Reference<Byte> keyPos, String word, Reference<Float> score, char curChar)
		{
			if (keyPos.ref == 0) // check this isn't the beginning of a word (no one misses Yud there)
			{
				return null;
			}

			// Yud shouldn't be tolerated before a Vav
			if (key[keyPos.ref] == HebrewCharacters.VAV)
			{
				return null;
			}

            if (curChar != HebrewCharacters.YOD)
			{
				// Support keys with doubled Yud, where the actual word in the dictionary isn't
				if ((key[keyPos.ref] == HebrewCharacters.YOD)
						&& (key[keyPos.ref - 1] == HebrewCharacters.YOD))
				{
					score.ref *= 0.9f;
					keyPos.ref++;
					return 0;
				}

				// Support Hirik-Haser (Niqqudless missing Yud), although it would suffer a high penalty
				// to low rate false positives when there are better options
				if (key[keyPos.ref] == HebrewCharacters.YOD)
				{
					score.ref *= 0.6f;
					keyPos.ref++;
					return 0;
				}

				return null;
			}

			// Don't initiate a toleration process if current key position has Yud already. If the correct
			// spelling requires double-Yud, we will arrive here again very soon
			if (key[keyPos.ref] == HebrewCharacters.YOD)
			{
				return null;
			}

			// We already have consumed a Yud very recently
			if (word.charAt(word.length() - 1) == HebrewCharacters.YOD)
			{
				// We allow adding another Yud only if there was one in the key originally, and the key is longer
                // than 3 letters (otherwise חיה becomes חייה, and בית becomes ביית).
				if ((key[keyPos.ref - 1] != HebrewCharacters.YOD) || ((keyPos.ref + 1 == key.length) && (key.length <= 3)))
				{
					return null;
				}

				score.ref *= 0.8f;
				return 1;
			}
			// No Yud existed before in the key, so we tolerate normally unless we consumed a Vav recently
			else if (word.charAt(word.length() - 1) != HebrewCharacters.VAV)
			{
				score.ref *= 0.8f;
				return 1;
			}

			return null;
		}
	}
	/**
	 Accepts Vav as the next in key if absent from the original key, only if:
		 * Current original key position isn't the beginning or end of a word
		 * Tolerating Vav doesn't create a sequence of Vav and Yud
		 * Previous key position isn't another Vav, so is the next in the original key (handled by TolerateNonDoubledConsonantVav)
		 * Previous key position isn't Yud (too intrusive a tolerance)

	*/
	public static class TolerateEmKryiaVav implements ToleranceFunction
	{
		public Integer tolerate(char[] key, Reference<Byte> keyPos, String word, Reference<Float> score, char curChar)
		{
            if ((curChar != HebrewCharacters.VAV) || // check current trie position
                    (keyPos.ref == 0) || (keyPos.ref + 1 == key.length) || // check this isn't the end or the beginning of a word (no one misses Vav there)
                    (key[keyPos.ref] == HebrewCharacters.YOD) || (key[keyPos.ref] == HebrewCharacters.HE) || // Vav shouldn't be tolerated before a Yud or a Heh
                    (key[keyPos.ref] == HebrewCharacters.VAV // Don't low-rank exact matches
)
                    )
                    return null;

			char prevChar = word.charAt(word.length() - 1);
            if ((key[keyPos.ref + 1] != HebrewCharacters.VAV) && (prevChar != HebrewCharacters.VAV) && // This case is handled by TolerateNonDoubledConsonantVav
                    (prevChar != HebrewCharacters.YOD)) // This is an edit too intrusive to be a possible niqqud-less spelling
			{
				score.ref *= 0.8f;
				return 1;
			}

			return null;
		}
	}

	public static class TolerateNonDoubledConsonantVav implements ToleranceFunction
	{
		public Integer tolerate(char[] key, Reference<Byte> keyPos, String word, Reference<Float> score, char curChar)
		{
			// TODO: Here we apply the Academia's "ha-ktiv hasar ha-niqqud" rule of doubling
			// a consonant waw in the middle a word, unless it's already next to a waw

            if ((curChar == HebrewCharacters.VAV) || (keyPos.ref == 0) || (keyPos.ref + 1 == key.length))
                return null;

            if ((key[keyPos.ref] == HebrewCharacters.VAV)
            		&& (word.charAt(word.length() - 1) == HebrewCharacters.VAV))
			{
				keyPos.ref++;
				score.ref *= 0.8f;
				return 0;
			}

			return null;
		}
	}
}
