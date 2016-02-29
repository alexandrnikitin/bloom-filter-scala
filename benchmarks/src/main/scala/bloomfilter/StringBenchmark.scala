package bloomfilter

import bloomfilter.mutable.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class StringBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  val myBF = BloomFilter[String](itemsExpected, falsePositiveRate)

  @Benchmark
  def my() = {
    myBF.add("Some string")
  }

}
