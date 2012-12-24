using System;
using HebMorph.DataStructures;
using HebMorph.HSpell;

namespace HebMorph.Tests
{
	public abstract class TestBase
	{
		private static DictRadix<MorphData> dict;

		protected DictRadix<MorphData> HspellDict
		{
			get
			{
				if (dict == null)
				lock (this)
				{
					if (dict == null)
					{
						String hspellPath = null;
						string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
						int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
						if (loc > -1)
						{
							path = path.Remove(loc + 1);
							hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
						}

						if (hspellPath == null)
							throw new ArgumentException("path to hspell data folder couldn't be found");

						dict = Loader.LoadDictionaryFromHSpellFolder(hspellPath, true);
					}
				}
				return dict;
			}
		}
	}
}
