import bloomfilter.mutable.BloomFilter

import scala.util.Random

object Main extends App {
  private val random = new Random()

  val a = new Array[Array[Byte]](100000000)
  for (i <- a.indices) {
    val key = new Array[Byte](128)
    random.nextBytes(key)
    a(i) = key
  }

  val bf = BloomFilter[Array[Byte]](100000000, 0.1)
  for (i <- a.indices) {
    bf.add(a(i))
  }
}
