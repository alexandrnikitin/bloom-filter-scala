package bloomfilter.mutable._128bit

import bloomfilter.CanGenerate128HashFrom
import bloomfilter.mutable.UnsafeBitArray

class BloomFilter[T](numberOfBits: Long, numberOfHashes: Int)(implicit canGenerateHash: CanGenerate128HashFrom[T]) {

  private val bits = new UnsafeBitArray(numberOfBits)

  def add(x: T): Unit = {
    val pair = canGenerateHash.generateHash(x)

    var i = 0
    while (i < numberOfHashes) {
      val h = pair._1 + i * pair._2
      val nextHash = if (h < 0) ~h else h
      bits.set(nextHash % numberOfBits)
      i += 1
    }
  }

  def mightContain(x: T): Boolean = {
    val pair = canGenerateHash.generateHash(x)

    var i = 0
    while (i < numberOfHashes) {
      val h = pair._1 + i * pair._2
      val nextHash = if (h < 0) ~h else h
      if (!bits.get(nextHash % numberOfBits))
        return false
      i += 1
    }
    true
  }

  def expectedFalsePositiveRate(): Double = {
    math.pow(bits.getBitCount.toDouble / numberOfBits, numberOfHashes.toDouble)
  }

  def dispose(): Unit = bits.dispose()

}

object BloomFilter {

  def apply[T](numberOfItems: Long, falsePositiveRate: Double)(
      implicit canGenerateHash: CanGenerate128HashFrom[T]): BloomFilter[T] = {

    val nb = optimalNumberOfBits(numberOfItems, falsePositiveRate)
    val nh = optimalNumberOfHashes(numberOfItems, nb)
    new BloomFilter[T](nb, nh)
  }

  def optimalNumberOfBits(numberOfItems: Long, falsePositiveRate: Double): Long = {
    math.ceil(-1 * numberOfItems * math.log(falsePositiveRate) / math.log(2) / math.log(2)).toLong
  }

  def optimalNumberOfHashes(numberOfItems: Long, numberOfBits: Long): Int = {
    math.ceil(numberOfBits / numberOfItems * math.log(2)).toInt
  }

}
