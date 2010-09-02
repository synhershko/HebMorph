using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Text;
using HebMorph.DataStructures;

namespace HebMorph.Tests
{
    [TestClass()]
    public class HSpellLoaderTest
    {
        static string hspellPath;

        [TestInitialize()]
        public void SetUp()
        {
            string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
            int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
            if (loc > -1)
            {
                path = path.Remove(loc + 1);
                hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
            }
        }

        [TestMethod()]
        public void VerifyAllWordsAreLoaded()
        {
            int WordsCount = HSpell.Loader.GetWordCountInHSpellFolder(hspellPath);
            DictRadix<MorphData> d = HSpell.Loader.LoadDictionaryFromHSpellFolder(hspellPath, true);
            Assert.AreEqual<int>(WordsCount, d.Count); // Compare expected words count with the cached counter

            // Verify the cached counter equals to the count of elements retrieved by actual enumeration,
            // and that the nodes are alphabetically sorted
            int enCount = 0;
            string nodeText = string.Empty;
            DictRadix<MorphData>.RadixEnumerator en = d.GetEnumerator() as DictRadix<MorphData>.RadixEnumerator;
            while (en.MoveNext())
            {
                Assert.IsTrue(string.Compare(nodeText, en.CurrentKey, StringComparison.Ordinal) < 0);
                nodeText = en.CurrentKey;
                enCount++;
            }
            Assert.AreEqual<int>(WordsCount, enCount); // Compare expected words count with count yielded by iteration
        }
    }
}
