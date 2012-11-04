using System;
using Xunit;

namespace HebMorph.DataStructures.Tests
{
    public class RadixTest
    {
        [Fact]
        public void DoesAddNodesCorrectlyWithReferenceTypes()
        {
            DictRadix<GuidObject> d = new DictRadix<GuidObject>();
            DoAddNodesTest<GuidObject>(d, new DataGeneratorFunc(delegate() { return new GuidObject(); }));
        }

		[Fact]
        public void DoesAddNodesCorrectlyWithNullableTypes()
        {
            DictRadix<int?> d = new DictRadix<int?>();
            DoAddNodesTest<int?>(d, new DataGeneratorFunc(delegate() { return rnd.Next(); }));
        }

		[Fact]
        public void DoesAddNodesCorrectlyWithNativeTypes()
        {
            DictRadix<int> d = new DictRadix<int>();
            DoAddNodesTest<int>(d, new DataGeneratorFunc(delegate() { return rnd.Next(); }));
        }

        void DoAddNodesTest<T>(DictRadix<T> d, DataGeneratorFunc dataGenerator)
        {
            int counter = 0;

            // Try adding one node...
            AddAndIncrement(d, "abcdef", (T)dataGenerator(), ref counter);

            // And another
            AddAndIncrement(d, "azfwasf", (T)dataGenerator(), ref counter);

            // Adding this node will require the radix to split a leaf
            AddAndIncrement(d, "abf", (T)dataGenerator(), ref counter);

            // Now add a leaf under that new leaf
            AddAndIncrement(d, "abfeeee", (T)dataGenerator(), ref counter);

            // Add a new leaf under the root
            AddAndIncrement(d, "bcdef", (T)dataGenerator(), ref counter);

            // Simple node addition
            AddAndIncrement(d, "abcdefg", (T)dataGenerator(), ref counter);

            // Re-root operation
            AddAndIncrement(d, "a", (T)dataGenerator(), ref counter);

            // Add a new leaf node after re-rooting
            AddAndIncrement(d, "agga", (T)dataGenerator(), ref counter);

            // Do all that backwards - add leafs in a sequential order
            AddAndIncrement(d, "c", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "cb", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "cbd", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "cbdefg", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "cbdefghij", (T)dataGenerator(), ref counter);
            // And break that order
            AddAndIncrement(d, "czzzzij", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "czzzzija", (T)dataGenerator(), ref counter);
            AddAndIncrement(d, "czzzzijabcde", (T)dataGenerator(), ref counter);

            // Test overriding an item - value should not change
            AddAndIncrement(d, "abf", (T)dataGenerator(), ref counter);

            // Test overriding an item with AllowValueOverride set to true
            d.AllowValueOverride = true;
            AddAndIncrement(d, "abf", (T)dataGenerator(), ref counter);

            // Verify the cached counter equals to the count of elements retrieved by actual enumeration,
            // and that the nodes are alphabetically sorted
            int enCount = 0;
            string nodeText = string.Empty;
            var en = d.GetEnumerator() as DictRadix<T>.RadixEnumerator;
            while (en.MoveNext())
            {
                Assert.True(string.Compare(nodeText, en.CurrentKey, StringComparison.Ordinal) < 0);
                nodeText = en.CurrentKey;
                enCount++;
            }
            Assert.Equal(counter, enCount);
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

	    static void AddAndIncrement<T>(DictRadix<T> d, string key, T obj, ref int counter)
        {
            // Only increment counter if the key doesn't already 
            bool hasKey = true;
            if (object.Equals(d.Lookup(key), default(T)))
            {
                counter++;
                hasKey = false;
            }
                
            d.AddNode(key, obj);

            Assert.Equal(counter, d.Count);

            // Only check insertion if there was one
            if (d.AllowValueOverride || !hasKey)
                Assert.Equal(d.Lookup(key), obj);
        }
        #endregion
    }
}
