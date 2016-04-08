package endToEnd.bloomfilter.mutable._128bit

import bloomfilter.mutable._128bit.BloomFilter
import org.scalatest.{FreeSpec, Matchers}

class SampleUsageSpec extends FreeSpec with Matchers {
  "Create, put and check " in {
    val bloomFilter = BloomFilter[String](1000, 0.01)

    bloomFilter.add("")
    bloomFilter.add("Hello!")
    bloomFilter.add("8f16c986824e40e7885a032ddd29a7d3")

    bloomFilter.mightContain("") shouldBe true
    bloomFilter.mightContain("Hello!") shouldBe true
    bloomFilter.mightContain("8f16c986824e40e7885a032ddd29a7d3") shouldBe true

    bloomFilter.dispose()
  }
}
