package alternatives.breeze

import breeze.util.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemBenchmark {

  private val itemsExpected = 100000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter.optimallySized[String](itemsExpected.toDouble, falsePositiveRate)

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
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
