package tests.bloomfilter.mutable

import java.io.ObjectInputStream

import bloomfilter.mutable.BloomFilter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BloomFilterSerializationBackwardsCompatibilityTest extends AnyFlatSpec with Matchers {
  "BloomFilter" should "successfully deserialize objects serialized with a previous release" in {
    val inputStream = new ObjectInputStream(getClass.getResourceAsStream("bloomFilter-0.12.ser"))
    try {
      val bloomFilter = inputStream.readObject().asInstanceOf[BloomFilter[String]]
      bloomFilter.mightContain("Test") should be(true)
      bloomFilter.dispose()
    } finally {
      inputStream.close()
    }
  }
}
