package endToEnd.mutable

import bloomfilter.mutable._128bit.BloomFilter
import org.scalatest.{FreeSpec, Matchers}

class CreateBloomFilterSpec extends FreeSpec with Matchers {
  "BloomFilter for Long" in {
    val bloomFilter = BloomFilter[Long](1000, 0.01)
    bloomFilter.dispose()
  }

  "Big BloomFilter" in {
    val bloomFilter = BloomFilter[Long](1000000000, 0.01)
    bloomFilter.dispose()
  }

  "Many big BloomFilters" in {
    0.to(1000).foreach { _ =>
      val bloomFilter = BloomFilter[Long](1000000000, 0.01)
      bloomFilter.dispose()
    }
  }
}
