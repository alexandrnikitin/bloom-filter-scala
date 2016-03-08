package bloomfilter.mutable._128bit

import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemBenchmark {

  private val itemsExpected = 100000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter[String](itemsExpected, falsePositiveRate)

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
  bf.add(item)

  @Benchmark
  def myPut(): Unit = {
    bf.add(item)
  }

  @Benchmark
  def myGet(): Unit = {
    bf.mightContain(item)
  }

}
