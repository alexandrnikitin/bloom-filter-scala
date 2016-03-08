package alternatives.guava

import java.nio.charset.Charset

import com.google.common.hash.{BloomFilter, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemBenchmark {

  private val itemsExpected = 100000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter.create[String](Funnels.stringFunnel(Charset.forName("UTF-8")), itemsExpected, falsePositiveRate)

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
  bf.put(item)

  @Benchmark
  def guavaPut(): Unit = {
    bf.put(item)
  }

  @Benchmark
  def guavaGet(): Unit = {
    bf.mightContain(item)
  }

}
