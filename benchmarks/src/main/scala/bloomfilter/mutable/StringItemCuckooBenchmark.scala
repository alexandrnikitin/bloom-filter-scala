package bloomfilter.mutable

import org.openjdk.jmh.annotations.{BenchmarkMode, OperationsPerInvocation, _}

import scala.util.Random

@State(Scope.Benchmark)
class StringItemCuckooBenchmark {

  private val itemsExpected = 100000000L
  private val random = new Random()

  private val bf = CuckooFilter[String](itemsExpected)

  @Param(Array("1024"))
  var length: Int = _

  private val item = random.nextString(length)
  bf.add(item)

  private val items = new Array[String](1000)

  var i = 0
  while (i < items.length) {
    items(i) = random.nextString(length)
    i += 1
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  @OperationsPerInvocation(1000)
  def myPut(): Unit = {
    var i = 0
    while (i < items.length) {
      bf.add(items(i))
      i += 1
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SingleShotTime))
  @OperationsPerInvocation(1000)
  def myGet(): Unit = {
    var i = 0
    while (i < items.length) {
      bf.mightContain(items(i))
      i += 1
    }
  }

}
