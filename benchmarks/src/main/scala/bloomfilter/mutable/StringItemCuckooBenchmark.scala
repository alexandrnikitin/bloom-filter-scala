package bloomfilter.mutable

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{BenchmarkMode, OperationsPerInvocation, OutputTimeUnit, _}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemCuckooBenchmark {

  private val itemsExpected = 100000000L
  private val random = new Random()

  private var bf: CuckooFilter[String] = _

  @Param(Array("1024"))
  var length: Int = _

  private val items = new Array[String](10000)
  var i = 0
  while (i < items.length) {
    items(i) = random.nextString(length)
    i += 1
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    bf = CuckooFilter[String](itemsExpected)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(10000)
  def myPut(): Unit = {
    var i = 0
    while (i < items.length) {
      bf.add(items(i))
      i += 1
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OperationsPerInvocation(10000)
  def myGet(): Unit = {
    var i = 0
    while (i < items.length) {
      bf.mightContain(items(i))
      i += 1
    }
  }

}
