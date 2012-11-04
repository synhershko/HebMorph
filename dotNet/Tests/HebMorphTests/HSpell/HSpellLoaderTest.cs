using System;
using HebMorph.DataStructures;
using Xunit;

namespace HebMorph.HSpell.Tests
{
    public class HSpellLoaderTest
    {
        static string hspellPath;

	    public HSpellLoaderTest()
	    {
			string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
            int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
            if (loc > -1)
            {
                path = path.Remove(loc + 1);
                hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
            }
        }

        [Fact]
        public void VerifyAllWordsAreLoaded()
        {
            int WordsCount = HSpell.Loader.GetWordCountInHSpellFolder(hspellPath);
            DictRadix<MorphData> d = HSpell.Loader.LoadDictionaryFromHSpellFolder(hspellPath, true);
            Assert.Equal(WordsCount, d.Count); // Compare expected words count with the cached counter

            // Verify the cached counter equals to the count of elements retrieved by actual enumeration,
            // and that the nodes are alphabetically sorted
            int enCount = 0;
            string nodeText = string.Empty;
            DictRadix<MorphData>.RadixEnumerator en = d.GetEnumerator() as DictRadix<MorphData>.RadixEnumerator;
            while (en.MoveNext())
            {
                Assert.True(string.Compare(nodeText, en.CurrentKey, StringComparison.Ordinal) < 0);
                nodeText = en.CurrentKey;
                enCount++;
            }
            Assert.Equal(WordsCount, enCount); // Compare expected words count with count yielded by iteration
        }
    }
}
