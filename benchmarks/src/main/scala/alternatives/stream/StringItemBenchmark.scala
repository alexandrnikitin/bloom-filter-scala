package alternatives.stream

import com.clearspring.analytics.stream.membership.BloomFilter
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}
import org.openjdk.jmh.infra.Blackhole

import scala.util.Random

@State(Scope.Benchmark)
class StringItemBenchmark {

  private val itemsExpected = 100000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = new BloomFilter(itemsExpected.toInt, falsePositiveRate)

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
  bf.add(item)

  @Benchmark
  def streamPut(): Unit = {
    bf.add(item)
  }

  @Benchmark
  def streamGet(bh: Blackhole): Unit = {
    bh.consume(bf.isPresent(item))
  }

}
