package sandbox.bloomfilter.mutable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Stable Bloom Filter.
 * Similar interface to Google Guava's {@link com.google.common.hash.BloomFilter}.
 * Based on the document linked below, implementation leverages some of the Guava's code
 * from {@link com.google.common.hash.BloomFilter} and {@link com.google.common.hash.BloomFilterStrategies}.
 *
 * @see <a href="http://www.cs.ualberta.ca/~drafiei/papers/DupDet06Sigmod.pdf">
 *      Approximately Detecting Duplicates for Streaming Data using Stable Bloom Filters, by
 *      Fan Deng and Davood Rafiei, University of Alberta</a>
 */
public class StableBloomFilter<T> implements Predicate<T>, Serializable {
    private static final long serialVersionUID = 2145436436453789436L;

    // TODO investigate the MAX_VAL coefficient
    private static final int MAX_VAL = 10;//Integer.MAX_VALUE;
    private final int[] cells;
    private final int numHashFunctions;
    private final Funnel<? super T> funnel;
    private final BloomFilterStrategy strategy;
    private final int numDecrementCells;

    public static <T> StableBloomFilter<T> create(Funnel<? super T> funnel, long elementsExpected, double falsePositiveRate) {
        Preconditions.checkArgument(falsePositiveRate < 1.0);
        int cells = Math.toIntExact(optimalNumOfBits(elementsExpected, falsePositiveRate));
        int hashes = optimalNumOfHashFunctions(elementsExpected, cells);
        int numDecrementCells = hashes * 2;
        return new StableBloomFilter<>(cells, hashes, numDecrementCells, funnel);
    }

    private StableBloomFilter(int numCells, int numHashFunctions, int numDecrementCells, Funnel<? super T> funnel) {
        checkArgument(numHashFunctions > 0, "numHashFunctions (%s) must be > 0", numHashFunctions);
        checkArgument(
                numHashFunctions <= 255, "numHashFunctions (%s) must be <= 255", numHashFunctions);
        this.numDecrementCells = numDecrementCells;
        this.cells = new int[numCells];
        this.numHashFunctions = numHashFunctions;
        this.funnel = funnel;
        strategy = BloomFilterStrategy.Murmur128_Mitz_32;
    }

    public boolean mightContain(T object) {
        return strategy.mightContain(object, funnel, numHashFunctions, cells);
    }

    public boolean put(T object) {
        decrementCells();
        return strategy.put(object, funnel, numHashFunctions, cells);
    }

    private void decrementCells() {
        int min = 0;
        int max = cells.length - 1;
        int decrementPos = min + (int) (Math.random() * ((max - min) + 1));
        for (int i = 0; i < numDecrementCells; i++) {
            if (decrementPos >= cells.length) {
                decrementPos = 0;
            }
            if (cells[decrementPos] > 0) {
                cells[decrementPos] = cells[decrementPos] - 1;
            }
            decrementPos++;
        }
    }

    @Override
    public boolean apply(T input) {
        return this.mightContain(input);
    }

    enum BloomFilterStrategy implements Serializable {
        Murmur128_Mitz_32() {

            public <T> boolean mightContain(T object, Funnel<? super T> funnel,
                                            int numHashFunctions, int[] cells) {
                long hash64 = Hashing.murmur3_128().newHasher().putObject(object, funnel).hash().asLong();
                int hash1 = (int) hash64;
                int hash2 = (int) (hash64 >>> 32);
                for (int i = 1; i <= numHashFunctions; i++) {
                    int nextHash = hash1 + i * hash2;
                    if (nextHash < 0) {
                        nextHash = ~nextHash;
                    }
                    int pos = nextHash % cells.length;
                    if (cells[pos] == 0) {
                        return false;
                    }
                }
                return true;
            }
        };
        public static <T> boolean put(T object, Funnel<? super T> funnel,
                                      int numHashFunctions, int[] cells) {
            // TODO(user): when the murmur's shortcuts are implemented, update this code
            long hash64 = Hashing.murmur3_128().newHasher().putObject(object, funnel).hash().asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            boolean bitsChanged = false;
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                int pos = nextHash % cells.length;
                bitsChanged |= (cells[pos] != MAX_VAL);
                cells[pos] = MAX_VAL;
            }
            return bitsChanged;
        }
        public abstract <T> boolean mightContain(T object, Funnel<? super T> funnel,
                                                 int numHashFunctions, int[] cells);
    }

    public static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    public static int optimalNumOfHashFunctions(long n, long m) {
        // (m / n) * log(2), but avoid truncation due to division!
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StableBloomFilter<?> that = (StableBloomFilter<?>) o;
        return numHashFunctions == that.numHashFunctions &&
               numDecrementCells == that.numDecrementCells &&
               Arrays.equals(cells, that.cells) &&
               Objects.equals(funnel, that.funnel) &&
               Objects.equals(strategy, that.strategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells, numHashFunctions, funnel, strategy, numDecrementCells);
    }

    @Override
    public String toString() {
        return "StableBloomFilter{" +
               "cells.length=" + cells.length +
               ", numHashFunctions=" + numHashFunctions +
               ", funnel=" + funnel +
               ", strategy=" + strategy +
               ", numDecrementCells=" + numDecrementCells +
               '}';
    }



}