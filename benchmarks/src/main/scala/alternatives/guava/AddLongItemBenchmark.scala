package alternatives.guava

import bloomfilter.mutable.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddLongItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  private val bf = BloomFilter[Long](itemsExpected, falsePositiveRate)

  @Benchmark
  def guava() = {
    bf.add(1L)
  }


}