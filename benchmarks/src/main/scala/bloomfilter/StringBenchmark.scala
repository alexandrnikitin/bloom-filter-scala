package bloomfilter

import bloomfilter.mutable.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import com.twitter.algebird.{BloomFilter => AlgebirdBloomFilter}

@State(Scope.Benchmark)
class StringBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  val myBF = BloomFilter[String](itemsExpected, falsePositiveRate)
  val algebirdBF = AlgebirdBloomFilter(itemsExpected.toInt, falsePositiveRate, 0).create("first element to create BF")

  @Benchmark
  def my() = {
    myBF.add("Some string")
  }

  @Benchmark
  def algebird() = {
    algebirdBF.+("Some string")
  }

}
