package com.code972.hebmorph;

import com.code972.hebmorph.datastructures.DictRadix;
import com.code972.hebmorph.datastructures.DictRadix.RadixEnumerator;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class RadixTest
{
	@Test
    public void DoesAddNodesCorrectlyWithReferenceTypes()
    {
        DictRadix<UuidObject> d = new DictRadix<UuidObject>();
        doAddNodesTest(d, new GuidGenerator());
    }

	@Test
    public void doesAddNodesCorrectlyWithNullableTypes()
    {
        DictRadix<Integer> d = new DictRadix<Integer>();
        doAddNodesTest(d, new RandomGenerator());
    }

	@Test
    public void doesAddNodesCorrectlyWithNativeTypes()
    {
        DictRadix<Integer> d = new DictRadix<Integer>();
        doAddNodesTest(d, new RandomGenerator());
    }

    <T>void doAddNodesTest(DictRadix<T> d, DataGeneratorFunc<T> dataGenerator)
    {
        IntBox counter = new IntBox(0);

        // Try adding one node...
        addAndIncrement(d, "abcdef", dataGenerator.generate(), counter);

        // And another
        addAndIncrement(d, "azfwasf", dataGenerator.generate(), counter);

        // Adding this node will require the radix to split a leaf
        addAndIncrement(d, "abf", dataGenerator.generate(), counter);

        // Now add a leaf under that new leaf
        addAndIncrement(d, "abfeeee", dataGenerator.generate(), counter);

        // Add a new leaf under the root
        addAndIncrement(d, "bcdef", dataGenerator.generate(), counter);

        // Simple node addition
        addAndIncrement(d, "abcdefg", dataGenerator.generate(), counter);

        // Re-root operation
        addAndIncrement(d, "a", dataGenerator.generate(), counter);

        // Add a new leaf node after re-rooting
        addAndIncrement(d, "agga", dataGenerator.generate(), counter);

        // Do all that backwards - add leafs in a sequential order
        addAndIncrement(d, "c", dataGenerator.generate(), counter);
        addAndIncrement(d, "cb", dataGenerator.generate(), counter);
        addAndIncrement(d, "cbd", dataGenerator.generate(), counter);
        addAndIncrement(d, "cbdefg", dataGenerator.generate(), counter);
        addAndIncrement(d, "cbdefghij", dataGenerator.generate(), counter);
        // And break that order
        addAndIncrement(d, "czzzzij", dataGenerator.generate(), counter);
        addAndIncrement(d, "czzzzija", dataGenerator.generate(), counter);
        addAndIncrement(d, "czzzzijabcde", dataGenerator.generate(), counter);

        // Test overriding an item - value should not change
        addAndIncrement(d, "abf", dataGenerator.generate(), counter);

        // Test overriding an item with AllowValueOverride set to true
        d.setAllowValueOverride(true);
        addAndIncrement(d, "abf", dataGenerator.generate(), counter);

        // Verify the cached counter equals to the count of elements retrieved by actual enumeration,
        // and that the nodes are alphabetically sorted
        int enCount = 0;
        String nodeText = "";
        DictRadix<T>.RadixEnumerator en = (RadixEnumerator)d.iterator();
        while(en.hasNext())
        {
            en.next();
            assertTrue(cSharpStringCompare(nodeText, en.getCurrentKey()) < 0);
            nodeText = en.getCurrentKey();
            enCount++;
        }
        assertEquals(counter.val, enCount);
    }


    static private int cSharpStringCompare(String s1, String s2) {
        if(s1 == null)
            return s2 == null ? 0 : -1;
        return s2 == null ? 1 : s1.compareTo(s2);
    }


    private Random rnd = new Random();

//    delegate object DataGeneratorFunc();
    static private interface DataGeneratorFunc<T> {
        public T generate();
    }


    private class RandomGenerator implements DataGeneratorFunc<Integer> {
        public Integer generate() {
            return rnd.nextInt();
        }
    }

    private class GuidGenerator implements DataGeneratorFunc<UuidObject> {
        public UuidObject generate() {
            return new UuidObject();
        }
    }


    static private class IntBox {
        IntBox(int val) { this.val = val; }
        int val;
    }


    class UuidObject
    {
        public UUID _guid;
        public UuidObject()
        {
            _guid = UUID.randomUUID();
        }

        public @Override boolean equals(Object obj)
        {
            UuidObject o = (UuidObject)obj;

            if (o != null && this._guid.equals(o._guid))
                return true;

            return false;
        }

        public @Override int hashCode()
        {
            return _guid.hashCode();
        }
    }

    <T>void addAndIncrement(DictRadix<T> d, String key, T obj, IntBox counter)
    {
        // Only increment counter if the key doesn't already
        boolean hasKey = true;
        if (d.lookup(key) == null)
        {
            counter.val++;
            hasKey = false;
        }

        d.addNode(key, obj);

        assertEquals(counter.val, d.getCount());

        // Only check insertion if there was one
        if (d.getAllowValueOverride() || !hasKey)
            assertEquals(d.lookup(key), obj);
    }
}
