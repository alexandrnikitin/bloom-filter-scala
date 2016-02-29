package alternatives.algebird

import com.twitter.algebird.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class AddStringItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter(itemsExpected.toInt, falsePositiveRate, 0).create("")

  @Param(Array("1", "64", "256", "1024", "4096"))
  var length: Int = _

  private val item = random.nextString(length)

  @Benchmark
  def algebird(): Unit = {
    bf.+(item)
  }

}
