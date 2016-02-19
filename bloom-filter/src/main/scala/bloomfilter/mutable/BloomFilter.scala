package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

import scala.collection.immutable.IndexedSeq

class BloomFilter[T](numberOfBits: Long, numberOfHashes: Int)(implicit canGenerateHash: CanGenerateHashFrom[T]) {

  private val bits = new UnsafeBitArray(numberOfBits)

  def add(x: T): Unit = {
    getBits(x).foreach(bits.set)
  }

  def mightContain(x: T): Boolean = {
    getBits(x).forall(bits.get)
  }

  def expectedFalsePositiveRate(): Double = {
    math.pow(bits.getBitCount.toDouble / numberOfBits, numberOfHashes.toDouble)
  }

  private def getBits(x: T): IndexedSeq[Long] = {
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

  def apply[T](numberOfItems: Long, falsePositiveRate: Double)(
      implicit canGenerateHash: CanGenerateHashFrom[T]): BloomFilter[T] = {

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
