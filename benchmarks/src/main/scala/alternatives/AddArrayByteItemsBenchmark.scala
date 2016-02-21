package alternatives

import bloomfilter.mutable.{BloomFilter => MyBloomFilter}
import breeze.util.{BloomFilter => BloomFilterBreeze}
import com.google.common.hash.{BloomFilter => BloomFilterGuava, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class AddArrayByteItemsBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01

  val guavaBF = BloomFilterGuava.create[Array[Byte]](Funnels.byteArrayFunnel(), itemsExpected, falsePositiveRate)
  val breezeBF = BloomFilterBreeze.optimallySized[Array[Byte]](itemsExpected.toDouble, falsePositiveRate)
  val myBF = MyBloomFilter[Array[Byte]](itemsExpected, falsePositiveRate)


  private val random = new Random()
  val key1 = new Array[Byte](32)
  val key2 = new Array[Byte](64)
  val key3 = new Array[Byte](128)
  random.nextBytes(key1)
  random.nextBytes(key2)
  random.nextBytes(key3)

  @Benchmark
  def guava(): Boolean = {
    guavaBF.put(key1)
  }

  @Benchmark
  def breeze(): BloomFilterBreeze[Array[Byte]] = {
    breezeBF.+=(key1)
  }

  @Benchmark
  def my(): Unit = {
    myBF.add(key1)
  }


}
