
import bloomfilter.mutable.BloomFilter

object Main extends App {
  val expectedElements = 1000000
  val falsePositiveRate = 0.1
  val bf = BloomFilter[String](expectedElements, falsePositiveRate)

  // Put an element
  bf.add("lee")

  // Check whether an element in a set
  println(bf.mightContain("lee")) // return false

  // Dispose the instance
  bf.dispose()}
