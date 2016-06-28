import bloomfilter.CanGenerateHashFrom;
import bloomfilter.mutable.BloomFilter;

public class MainJava {
    public static void main(String[] args) {
        long expectedElements = 10000000;
        double falsePositiveRate = 0.1;
        BloomFilter<byte[]> bf = BloomFilter.apply(
                expectedElements,
                falsePositiveRate,
                CanGenerateHashFrom.CanGenerateHashFromByteArray$.MODULE$);

        byte[] element = new byte[100];
        bf.add(element);
        bf.mightContain(element);
        bf.dispose();
    }
}
