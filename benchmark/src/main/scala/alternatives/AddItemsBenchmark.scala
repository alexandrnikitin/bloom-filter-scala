package alternatives

import bloomfilter.mutable.{BloomFilter => MyBloomFilter}
import breeze.util.{BloomFilter => BloomFilterBreeze}
import com.google.common.hash.{BloomFilter => BloomFilterGuava, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddItemsBenchmark {

  private val iterations = 10000L
  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  val guavaBF = BloomFilterGuava.create[java.lang.Long](Funnels.longFunnel(), itemsExpected, falsePositiveRate)
  val breezeBF = BloomFilterBreeze.optimallySized[Long](itemsExpected.toDouble, falsePositiveRate)
  val myBF = MyBloomFilter[Long](itemsExpected, falsePositiveRate)

  @Benchmark
  def guava() = {
    var i = 0L
    while (i < iterations) {
      guavaBF.put(i)
      i += 1
    }
  }

  @Benchmark
  def breeze() = {
    var i = 0L
    while (i < iterations) {
      breezeBF.+=(i)
      i += 1
    }
  }

  @Benchmark
  def my() = {
    var i = 0L
    while (i < iterations) {
      myBF.add(i)
      i += 1
    }
  }


}
