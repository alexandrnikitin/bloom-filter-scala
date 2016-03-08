package alternatives.algebird

import com.twitter.algebird.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemBenchmark {

  private val itemsExpected = 100000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private var bf = BloomFilter(itemsExpected.toInt, falsePositiveRate, 0).create("")

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
  bf = bf.+(item)

  @Benchmark
  def algebirdPut(): Unit = {
    bf.+(item)
  }

  @Benchmark
  def algebirdGet(): Unit = {
    bf.contains(item)
  }

}
