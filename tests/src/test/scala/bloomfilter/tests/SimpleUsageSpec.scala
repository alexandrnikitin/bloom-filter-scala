package bloomfilter.tests

import bloomfilter.BloomFilter
import org.scalatest.{FreeSpec, Matchers}

class SimpleUsageSpec extends FreeSpec with Matchers {
  "Scenario: Simple add and get" in {
    val bloomFilter = BloomFilter[Long](1000, 0.01)
    bloomFilter.add(1)
    bloomFilter.mightContain(1) shouldBe true
  }
  "Scenario: Simple add and get 2" in {
    val bloomFilter = BloomFilter[Boolean](1000, 0.01)
    bloomFilter.add(true)
    bloomFilter.mightContain(true) shouldBe true
  }
  "Scenario: Simple add and get 3" in {
    val bloomFilter = BloomFilter[Array[Byte]](1000, 0.01)
    bloomFilter.add(Array[Byte](1, 2, 3))
    bloomFilter.mightContain(Array[Byte](1, 2, 3)) shouldBe true
  }

}
