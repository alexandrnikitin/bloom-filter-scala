
import bloomfilter.mutable.BloomFilter

object Main extends App {
  val expectedElements = 1000
  val falsePositiveRate: Double = 0.1
  val bf = BloomFilter[String](expectedElements, falsePositiveRate)
  bf.add("some string")
  bf.mightContain("some string")
  bf.dispose()
}
