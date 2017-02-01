import java.text.NumberFormat

import bloomfilter.mutable.{CuckooFilter, UnsafeTable8Bit}
import com.google.monitoring.runtime.instrumentation.{AllocationRecorder, Sampler}
import com.twitter.algebird.{BloomFilter => AlgebirdBloomFilter}

import scala.util.Random

object SandboxApp {

  def checkMemory(): Unit = {
    val runtime = Runtime.getRuntime

    val format = NumberFormat.getInstance()

    val sb = new StringBuilder()
    val maxMemory = runtime.maxMemory()
    val allocatedMemory = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()

    sb.append("free memory: " + format.format(freeMemory / 1024) + "\n")
    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n")
    sb.append("max memory: " + format.format(maxMemory / 1024) + "\n")
    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n")
    System.out.println(sb.toString())
  }


  def main(args: Array[String]): Unit = {

    val sut = CuckooFilter[Long](1000)
    sut.add(8)
    assert(sut.mightContain(8))
    sut.add(10)
    assert(sut.mightContain(10))
    sut.add(8)
    assert(sut.mightContain(8))
    sut.add(10000)
    assert(sut.mightContain(10000))

  }

  def compareAlgebirdFPR(): Unit = {

    val random: Random = new Random()

    val itemsExpected = 10000L
    val falsePositiveRate = 0.1
    var bf = AlgebirdBloomFilter(itemsExpected.toInt, falsePositiveRate, 0).create("")
    val bf2 = bloomfilter.mutable.BloomFilter[String](itemsExpected, falsePositiveRate)

    var i = 0
    while (i < itemsExpected) {
      val str: String = random.nextString(1000)
      bf = bf.+(str)
      bf2.add(str)
      i += 1
    }

    i = 0
    var in, in2 = 0
    while (true) {
      val str = random.nextString(1000)
      if (bf.contains(str).isTrue) {
        in += 1
      }
      if (bf2.mightContain(str)) {
        in2 += 1
      }

      if (i % 1000 == 0) {
        println(s"in: $in; in2: $in2")
      }
    }


  }

  def checkAllocations(): Unit = {
    val sampler: Sampler = new Sampler() {
      def sampleAllocation(count: Int, desc: String, newObj: Object, size: Long) {
        System.out.println("I just allocated the object " + newObj +
          " of type " + desc + " whose size is " + size)
        if (count != -1) {
          System.out.println("It's an array of size " + count)
        }
      }
    }

    AllocationRecorder.addSampler(sampler)

    AllocationRecorder.removeSampler(sampler)

  }
}