package alternatives.guava

import com.google.common.hash.{BloomFilter, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddLongItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  private val bf = BloomFilter.create[java.lang.Long](Funnels.longFunnel(), itemsExpected, falsePositiveRate)

  @Benchmark
  def guava() = {
    bf.put(1L)
  }


}