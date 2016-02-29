package alternatives.breeze

import breeze.util.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class AddArrayByteItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter.optimallySized[Array[Byte]](itemsExpected.toDouble, falsePositiveRate)

  @Param(Array("1", "64", "256", "1024", "4096"))
  var length: Int = _

  private val item = new Array[Byte](length)
  random.nextBytes(item)

  @Benchmark
  def breeze(): Unit = {
    bf.+=(item)
  }

}
