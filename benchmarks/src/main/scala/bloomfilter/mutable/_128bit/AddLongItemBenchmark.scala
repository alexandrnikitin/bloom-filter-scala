package bloomfilter.mutable._128bit

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddLongItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  private val bf = BloomFilter[Long](itemsExpected, falsePositiveRate)

  @Benchmark
  def my() = {
    bf.add(1L)
  }


}