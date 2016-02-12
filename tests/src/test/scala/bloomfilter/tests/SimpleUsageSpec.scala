package bloomfilter.tests

import bloomfilter.BloomFilter
import org.scalatest.{FreeSpec, Matchers}

class SimpleUsageSpec extends FreeSpec with Matchers {
  "Scenario: Simple add and get" in {
    val bloomFilter = BloomFilter.apply[Long](1000, 0.01)
    bloomFilter.add(1)
    bloomFilter.mightContain(1) shouldBe true
  }

}
