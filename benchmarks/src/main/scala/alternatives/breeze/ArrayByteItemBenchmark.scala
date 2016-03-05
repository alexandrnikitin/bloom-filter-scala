package alternatives.breeze

import breeze.util.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class ArrayByteItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter.optimallySized[Array[Byte]](itemsExpected.toDouble, falsePositiveRate)

  @Param(Array("1024"))
  var length: Int = _

  private val item = new Array[Byte](length)
  random.nextBytes(item)
  bf.+=(item)

  @Benchmark
  def breezePut(): Unit = {
    bf.+=(item)
  }

  @Benchmark
  def breezeGet(): Unit = {
    bf.contains(item)
  }

}
