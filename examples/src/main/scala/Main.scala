import bloomfilter.mutable.BloomFilter

import scala.util.Random

object Main extends App {
  private val random = new Random()
  val elements = 10000000
  val a = new Array[Array[Byte]](elements)

  var i = 0
  while (i < elements) {
    val key = new Array[Byte](128)
    random.nextBytes(key)
    a(i) = key
    i += 1
  }

  println("Prepeared data. Press any key")
  System.in.read()

  val bf = BloomFilter[Array[Byte]](elements, 0.1)
  var j = 0
  while (j < elements) {
    bf.add(a(j))
    j += 1
  }
}
