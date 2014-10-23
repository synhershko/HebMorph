package com.code972.hebmorph.datastructures;

import com.code972.hebmorph.TestBase;
import com.code972.hebmorph.datastructures.DictRadix.RadixEnumerator;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class RadixTest extends TestBase {
    @Test
    public void DoesAddNodesCorrectlyWithReferenceTypes() {
        DictRadix<UuidObject> d = new DictRadix<UuidObject>();
        doAddNodesTest(d, new GuidGenerator());
        doDoubleAddTest(d, new GuidGenerator());
    }

    @Test
    public void doesAddNodesCorrectlyWithNullableTypes() {
        DictRadix<Integer> d = new DictRadix<Integer>();
        doAddNodesTest(d, new RandomGenerator());
        doDoubleAddTest(d, new RandomGenerator());
    }

    @Test
    public void doesAddNodesCorrectlyWithNativeTypes() {
        DictRadix<Integer> d = new DictRadix<Integer>();
        doAddNodesTest(d, new RandomGenerator());
        doDoubleAddTest(d, new RandomGenerator());
    }

    @Test
    public void basicTestEquals() {
        DictRadix<Integer> d1 = new DictRadix<Integer>();
        DictRadix<Integer> d2 = new DictRadix<Integer>();
        assert (d1.equals(d2));
        d1.addNode("a", 1);
        assert (!d1.equals(d2));
        d2.addNode("a", 1);
        assert (d1.equals(d2));
        d1.addNode("b", 2);
        assert (!d1.equals(d2));
        d2.addNode("b", 2);
        assert (d1.equals(d2));
        d2.addNode("c", 3);
        assert (!d1.equals(d2));
    }

    <T> void doDoubleAddTest(DictRadix<T> d, DataGeneratorFunc<T> dataGenerator) {
        d.clear();

        try {
            d.lookup("abcdef");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        d.addNode("abcdef", dataGenerator.generate());
        d.addNode("abcdef", dataGenerator.generate());

        assertEquals(1, d.getCount());
        assertNotNull(d.lookup("abcdef", false));
        assertNull(d.lookup("abcde", true));
        assertNull(d.lookup("abcd", true));
        assertNull(d.lookup("abc", true));
        assertNull(d.lookup("ab", true));
        assertNull(d.lookup("a", true));
    }

    <T> void doAddNodesTest(DictRadix<T> d, DataGeneratorFunc<T> dataGenerator) {
        d.clear();

        IntBox counter = new IntBox(0);

        try {
            d.lookup("abcdef");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            d.lookup("abcdef", true);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

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
        T abcdefgValue = dataGenerator.generate();
        addAndIncrement(d, "abcdefg", abcdefgValue, counter);
        assertEquals(d.lookup("abcdefg"), abcdefgValue);

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
        RadixEnumerator en = (RadixEnumerator) d.iterator();
        while (en.hasNext()) {
            en.next();
            assertTrue(cSharpStringCompare(nodeText, en.getCurrentKey()) < 0);
            nodeText = en.getCurrentKey();
            enCount++;
        }
        assertEquals(counter.val, enCount);

        assertEquals(d.lookup("abcdefg"), abcdefgValue);

        // Make sure looking up on non-existent key will throw
        try {
            d.lookup("z");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        // Existing or partial keys
        assertNotNull(d.lookup("c"));
        assertNull(d.lookup("cz", true));
        assertNull(d.lookup("czz", true));
        assertNotNull(d.lookup("czzzzij"));
        assertNotNull(d.lookup("czzzzija"));
        assertNotNull(d.lookup("czzzzijabcde"));
    }


    static private int cSharpStringCompare(String s1, String s2) {
        if (s1 == null)
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
        IntBox(int val) {
            this.val = val;
        }

        int val;
    }


    class UuidObject {
        public UUID _guid;

        public UuidObject() {
            _guid = UUID.randomUUID();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof UuidObject))
                return false;

            UuidObject o = (UuidObject) obj;

            return this._guid.equals(o._guid);

        }

        @Override
        public int hashCode() {
            return _guid.hashCode();
        }
    }

    <T> void addAndIncrement(DictRadix<T> d, String key, T obj, IntBox counter) {
        // Only increment counter if the key doesn't already
        boolean hasKey = true;

        T value = null;
        try {
            value = d.lookup(key);
        } catch (IllegalArgumentException e) {
        }

        if (value == null) {
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
