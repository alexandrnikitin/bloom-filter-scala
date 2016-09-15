package tests.sandbox.bloomfilter.mutable;

import org.junit.Test;
import sandbox.bloomfilter.mutable.StableBloomFilterMath;

import static org.junit.Assert.*;

public class StableBloomFilterMathTest {

    @Test
    public void calculateFalsePositive() throws Exception {
        assertEquals(0.1, StableBloomFilterMath.calculateFalsePositive(2, 1, 4, 1000000), 0.02);
    }

    @Test
    public void calculateFalseNegative() throws Exception {
        int K = 3;
        int gap = 4000;
        int m = 20000000;
        int Max = 3;
        int P = 3;

        assertEquals(0.1, StableBloomFilterMath.calculateFalseNegative(K, gap, K / m, P / m, Max), 0.00001);
    }
}