package alternatives.breeze

import breeze.util.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddLongItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  private val bf = BloomFilter.optimallySized[Long](itemsExpected.toDouble, falsePositiveRate)

  @Benchmark
  def breeze() = {
    bf.+=(1L)
  }


}