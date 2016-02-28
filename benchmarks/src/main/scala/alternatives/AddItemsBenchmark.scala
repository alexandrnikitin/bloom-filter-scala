package alternatives

import bloomfilter.mutable._128bit.{BloomFilter => MyBloomFilter}
import breeze.util.{BloomFilter => BloomFilterBreeze}
import com.google.common.hash.{BloomFilter => BloomFilterGuava, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class AddItemsBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  val guavaBF = BloomFilterGuava.create[java.lang.Long](Funnels.longFunnel(), itemsExpected, falsePositiveRate)
  val breezeBF = BloomFilterBreeze.optimallySized[Long](itemsExpected.toDouble, falsePositiveRate)
  val myBF = MyBloomFilter[Long](itemsExpected, falsePositiveRate)

  @Benchmark
  def guava() = {
    guavaBF.put(1L)
  }

  @Benchmark
  def breeze() = {
    breezeBF.+=(1L)
  }

  @Benchmark
  def my() = {
    myBF.add(1L)
  }


}