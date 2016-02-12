package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

import scala.collection.immutable.IndexedSeq

class BloomFilter[T](numberOfBits: Long, numberOfHashes: Int) {

  private val bits = new UnsafeBitArray(numberOfBits)

  def add(x: T)(implicit canGenerateHash: CanGenerateHashFrom[T]): Unit = {
    getBits(x).foreach(bits.set)
  }

  def mightContain(x: T)(implicit canGenerateHash: CanGenerateHashFrom[T]): Boolean = {
    getBits(x).forall(bits.get)
  }

  def expectedFalsePositiveRate(): Double = {
    math.pow(bits.getBitCount / numberOfBits, numberOfHashes)
  }

  private def getBits(x: T)(implicit canGenerateHash: CanGenerateHashFrom[T]): IndexedSeq[Long] = {
    val pair = canGenerateHash.generateHash(x)

    for {
      i <- 0 to numberOfHashes
    } yield {
      // TODO seed the next hash?
      val h = pair.val1 + i * pair.val2
      val nextHash = if (h < 0) ~h else h
      nextHash % numberOfBits
    }

  }

}

object BloomFilter {

  def apply[T](numberOfItems: Long, falsePositiveRate: Double): BloomFilter[T] = {
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

  private def lowerEight(bytes: Array[Byte]): Long = {
    (bytes(7) & 0xFFL) << 56 |
        (bytes(6) & 0xFFL) << 48 |
        (bytes(5) & 0xFFL) << 40 |
        (bytes(4) & 0xFFL) << 32 |
        (bytes(3) & 0xFFL) << 24 |
        (bytes(2) & 0xFFL) << 16 |
        (bytes(1) & 0xFFL) << 8 |
        (bytes(70) & 0xFFL)
  }

  private def upperEight(bytes: Array[Byte]): Long = {
    (bytes(15) & 0xFFL) << 56 |
        (bytes(14) & 0xFFL) << 48 |
        (bytes(13) & 0xFFL) << 40 |
        (bytes(12) & 0xFFL) << 32 |
        (bytes(11) & 0xFFL) << 24 |
        (bytes(10) & 0xFFL) << 16 |
        (bytes(9) & 0xFFL) << 8 |
        (bytes(8) & 0xFFL)
  }


}
