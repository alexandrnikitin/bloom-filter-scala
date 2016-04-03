package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

class BloomFilter[T](numberOfBits: Long, numberOfHashes: Int)
    (implicit canGenerateHash: CanGenerateHashFrom[T]) {

  private val bits = new UnsafeBitArray(numberOfBits)

  def add(x: T): Unit = {
    val hash = canGenerateHash.generateHash(x)
    val hash1 = hash >>> 32
    val hash2 = (hash << 32) >> 32

    var i = 0
    while (i < numberOfHashes) {
      val h = hash1 + i * hash2
      val nextHash = if (h < 0) ~h else h
      bits.set(nextHash % numberOfBits)
      i += 1
    }
  }

  def mightContain(x: T): Boolean = {
    val hash = canGenerateHash.generateHash(x)
    val hash1 = hash >>> 32
    val hash2 = (hash << 32) >> 32
    var i = 0
    while (i < numberOfHashes) {
      val h = hash1 + i * hash2
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

  def apply[T](numberOfItems: Long, falsePositiveRate: Double)
      (implicit canGenerateHash: CanGenerateHashFrom[T]): BloomFilter[T] = {

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
