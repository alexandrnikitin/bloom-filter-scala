import bloomfilter.CanGenerate128HashFrom;
import bloomfilter.mutable._128bit.BloomFilter;

public class MainJava {
    public static void main(String[] args) {
        long elements = 10000000;
        double falsePositiveRate = 0.1;
        BloomFilter<byte[]> bf = BloomFilter.apply(elements, falsePositiveRate, CanGenerate128HashFrom.CanGenerate128HashFromByteArray$.MODULE$);

        byte[] element = new byte[100];
        bf.add(element);
        bf.mightContain(element);
    }
}
