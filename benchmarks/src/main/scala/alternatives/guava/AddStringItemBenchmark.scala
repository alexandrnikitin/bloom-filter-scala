package alternatives.guava

import java.nio.charset.Charset

import com.google.common.hash.{BloomFilter, Funnels}
import org.openjdk.jmh.annotations.{Benchmark, Param, Scope, State}

import scala.util.Random

@State(Scope.Benchmark)
class AddStringItemBenchmark {

  private val itemsExpected = 1000000L
  private val falsePositiveRate = 0.01
  private val random = new Random()

  private val bf = BloomFilter.create[String](Funnels.stringFunnel(Charset.forName("UTF-8")), itemsExpected, falsePositiveRate)

  @Param(Array("1", "64", "256", "1024", "4096"))
  var length: Int = _

  private val item = random.nextString(length)

  @Benchmark
  def guava(): Unit = {
    bf.put(item)
  }

}
