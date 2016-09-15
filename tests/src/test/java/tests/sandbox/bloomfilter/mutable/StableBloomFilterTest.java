package tests.sandbox.bloomfilter.mutable;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;
import sandbox.bloomfilter.mutable.StableBloomFilter;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.*;


public class StableBloomFilterTest {

    @Test
    public void testMightContain() throws Exception {
        StableBloomFilter<CharSequence>
                sut = StableBloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 100, 0.0001);
        sut.put("1test");
        sut.put("2test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        sut.put("3test");
        assertTrue(sut.mightContain("1test"));
        assertFalse(sut.mightContain("10test"));
    }

    @Test
    public void testPut() throws Exception {
        StableBloomFilter<CharSequence> sut = StableBloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10, 0.0001);
        sut.put("1test");
        assertTrue(sut.mightContain("1test"));
        assertFalse(sut.mightContain("10test"));
    }

    @Test
    public void testDecrement() throws Exception {
        StableBloomFilter<CharSequence> sut = StableBloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10, 0.0001);
        int min = 65;
        int max = 90;
        for (int i = min; i < max; i++) {
            sut.put((char) i + "");
        }
        assertTrue(sut.mightContain((char) (max - 1) + ""));
        assertTrue(sut.mightContain((char) (max - 2) + ""));
        assertTrue(sut.mightContain((char) (max - 3) + ""));
        assertFalse(sut.mightContain("no"));

        // TODO eviction is nondeterministic, investigate MAX_VAL coefficient
        //Assert.assertFalse(sut.mightContain((char) min + ""));
    }




    /**
     * Sanity checking with many combinations of false positive rates and expected insertions
     */
    static final Funnel<Object> BAD_FUNNEL = new Funnel<Object>() {
        @Override
        public void funnel(Object object, PrimitiveSink bytePrimitiveSink) {
            bytePrimitiveSink.putInt(object.hashCode());
        }
    };

    @Test
    public void testBasic() {
        for (double fpr = 0.0000001; fpr < 0.1; fpr *= 10) {
            for (int expectedInsertions = 1; expectedInsertions <= 10000; expectedInsertions *= 10) {
                checkSanity(StableBloomFilter.create(BAD_FUNNEL, expectedInsertions, fpr));
            }
        }
    }

    @Test
    public void testPreconditions() {
        try {
            StableBloomFilter.create(Funnels.unencodedCharsFunnel(), -1, 0.03);
            fail();
        } catch (NegativeArraySizeException expected) {
        }
        try {
            StableBloomFilter.create(Funnels.unencodedCharsFunnel(), 1, 0.0);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            StableBloomFilter.create(Funnels.unencodedCharsFunnel(), 1, 1.0);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testFailureWhenMoreThan255HashFunctionsAreNeeded() {
        try {
            int n = 1000;
            double p = 0.00000000000000000000000000000000000000000000000000000000000000000000000000000001;
            StableBloomFilter.create(Funnels.unencodedCharsFunnel(), n, p);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Tests that we never get an optimal hashes number of zero.
     */
    @Test
    public void testOptimalHashes() {
        for (int n = 1; n < 1000; n++) {
            for (int m = 0; m < 1000; m++) {
                assertTrue(StableBloomFilter.optimalNumOfHashFunctions(n, m) > 0);
            }
        }
    }

    // https://code.google.com/p/guava-libraries/issues/detail?id=1781
    @Test
    public void testOptimalNumOfHashFunctionsRounding() {
        assertEquals(7, StableBloomFilter.optimalNumOfHashFunctions(319, 3072));
    }

    /**
     * Tests that we always get a non-negative optimal size.
     */
    @Test
    public void testOptimalSize() {
        for (int n = 1; n < 1000; n++) {
            for (double fpp = Double.MIN_VALUE; fpp < 1.0; fpp += 0.001) {
                assertTrue(StableBloomFilter.optimalNumOfBits(n, fpp) >= 0);
            }
        }
        // some random values
        Random random = new Random(0);
        for (int repeats = 0; repeats < 10000; repeats++) {
            assertTrue(StableBloomFilter.optimalNumOfBits(random.nextInt(1 << 16), random.nextDouble()) >= 0);
        }
        // and some crazy values (this used to be capped to Integer.MAX_VALUE, now it can go bigger
        assertEquals(3327428144502L, StableBloomFilter.optimalNumOfBits(
                Integer.MAX_VALUE, Double.MIN_VALUE));
    }

    private void checkSanity(StableBloomFilter<Object> bf) {
        assertFalse(bf.mightContain(new Object()));
        assertFalse(bf.apply(new Object()));
        for (int i = 0; i < 100; i++) {
            Object o = new Object();
            bf.put(o);
            assertTrue(bf.mightContain(o));
            assertTrue(bf.apply(o));
        }
    }

    @Test
    public void testSerializationWithCustomFunnel() {
        StableBloomFilter<Long> source = StableBloomFilter.create(new CustomFunnel(), 100, 0.01);
        StableBloomFilter<Long> copied =
                (StableBloomFilter<Long>) SerializationUtils.deserialize(SerializationUtils.serialize(source));
        Assert.assertEquals(source, copied);
    }

    private static final class CustomFunnel implements Funnel<Long> {
        @Override
        public void funnel(Long value, PrimitiveSink into) {
            into.putLong(value);
        }

        @Override
        public boolean equals(Object object) {
            return (object instanceof CustomFunnel);
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

    @Test
    public void testPutReturnValue() {
        for (int i = 0; i < 10; i++) {
            StableBloomFilter<String> bf = StableBloomFilter.create(Funnels.unencodedCharsFunnel(), 100, 0.03);
            for (int j = 0; j < 10; j++) {
                String value = new Object().toString();
                boolean mightContain = bf.mightContain(value);
                boolean put = bf.put(value);
                assertTrue(mightContain != put);
            }
        }
    }

    @Test
    public void testJavaSerialization() {
        StableBloomFilter<byte[]> bf = StableBloomFilter.create(Funnels.byteArrayFunnel(), 100, 0.03);
        for (int i = 0; i < 10; i++) {
            bf.put(Ints.toByteArray(i));
        }
        StableBloomFilter<byte[]> copy =
                (StableBloomFilter<byte[]>) SerializationUtils.deserialize(SerializationUtils.serialize(bf));
        for (int i = 0; i < 10; i++) {
            assertTrue(copy.mightContain(Ints.toByteArray(i)));
        }
        Assert.assertEquals(bf, copy);
    }
}