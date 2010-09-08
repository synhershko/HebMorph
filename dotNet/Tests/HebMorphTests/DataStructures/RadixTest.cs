using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Text;
using HebMorph.DataStructures;

namespace HebMorph.DataStructures.Tests
{
    [TestClass()]
    public class RadixTest
    {
        [TestMethod()]
        public void DoesAddNodesCorrectlyWithReferenceTypes()
        {
            DictRadix<GuidObject> d = new DictRadix<GuidObject>();
            DoAddNodesTest<GuidObject>(d, new DataGeneratorFunc(delegate() { return new GuidObject(); }));
        }

        [TestMethod()]
        public void DoesAddNodesCorrectlyWithNullableTypes()
        {
            DictRadix<int?> d = new DictRadix<int?>();
            DoAddNodesTest<int?>(d, new DataGeneratorFunc(delegate() { return rnd.Next(); }));
        }

        [TestMethod()]
        public void DoesAddNodesCorrectlyWithNativeTypes()
        {
            DictRadix<int> d = new DictRadix<int>();
            DoAddNodesTest<int>(d, new DataGeneratorFunc(delegate() { return rnd.Next(); }));
        }

        void DoAddNodesTest<T>(DictRadix<T> d, DataGeneratorFunc dataGenerator)
        {
            int counter = 0;

            // Try adding one node...
            AddAndIncrement<T>(d, "abcdef", (T)dataGenerator(), ref counter);

            // And another
            AddAndIncrement<T>(d, "azfwasf", (T)dataGenerator(), ref counter);

            // Adding this node will require the radix to split a leaf
            AddAndIncrement<T>(d, "abf", (T)dataGenerator(), ref counter);

            // Now add a leaf under that new leaf
            AddAndIncrement<T>(d, "abfeeee", (T)dataGenerator(), ref counter);

            // Add a new leaf under the root
            AddAndIncrement<T>(d, "bcdef", (T)dataGenerator(), ref counter);

            // Simple node addition
            AddAndIncrement<T>(d, "abcdefg", (T)dataGenerator(), ref counter);

            // Re-root operation
            AddAndIncrement<T>(d, "a", (T)dataGenerator(), ref counter);

            // Add a new leaf node after re-rooting
            AddAndIncrement<T>(d, "agga", (T)dataGenerator(), ref counter);

            // Do all that backwards - add leafs in a sequential order
            AddAndIncrement<T>(d, "c", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "cb", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "cbd", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "cbdefg", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "cbdefghij", (T)dataGenerator(), ref counter);
            // And break that order
            AddAndIncrement<T>(d, "czzzzij", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "czzzzija", (T)dataGenerator(), ref counter);
            AddAndIncrement<T>(d, "czzzzijabcde", (T)dataGenerator(), ref counter);

            // Test overriding an item - value should not change
            AddAndIncrement<T>(d, "abf", (T)dataGenerator(), ref counter);

            // Test overriding an item with AllowValueOverride set to true
            d.AllowValueOverride = true;
            AddAndIncrement<T>(d, "abf", (T)dataGenerator(), ref counter);

            // Verify the cached counter equals to the count of elements retrieved by actual enumeration,
            // and that the nodes are alphabetically sorted
            int enCount = 0;
            string nodeText = string.Empty;
            DictRadix<T>.RadixEnumerator en = d.GetEnumerator() as DictRadix<T>.RadixEnumerator;
            while (en.MoveNext())
            {
                Assert.IsTrue(string.Compare(nodeText, en.CurrentKey, StringComparison.Ordinal) < 0);
                nodeText = en.CurrentKey;
                enCount++;
            }
            Assert.AreEqual(counter, enCount);
        }

        #region AddNodesTest internals

        delegate object DataGeneratorFunc();
        Random rnd = new Random();

        class GuidObject
        {
            public Guid _guid;
            public GuidObject()
            {
                _guid = Guid.NewGuid();
            }

            public override bool Equals(object obj)
            {
                GuidObject o = obj as GuidObject;

                if (o != null && this._guid.Equals(o._guid))
                    return true;

                return false;
            }

            public override int GetHashCode()
            {
                return _guid.GetHashCode();
            }
        }

        void AddAndIncrement<T>(DictRadix<T> d, string key, T obj, ref int counter)
        {
            // Only increment counter if the key doesn't already 
            bool hasKey = true;
            if (object.Equals(d.Lookup(key), default(T)))
            {
                counter++;
                hasKey = false;
            }
                
            d.AddNode(key, (T)obj);

            Assert.AreEqual(counter, d.Count);

            // Only check insertion if there was one
            if (d.AllowValueOverride || !hasKey)
                Assert.AreEqual(d.Lookup(key), obj);
        }
        #endregion
    }
}
