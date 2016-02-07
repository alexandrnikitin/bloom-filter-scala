class BloomFilter[T](numberOfBits: Long, numberOfHashes: Int) {

  def add(x: T): Unit = {
  }

  def checkAndAdd(x: T): Boolean = {
    false
  }

  def mightContain(x: T): Boolean = {
    false
  }

  def expectedFalsePositiveRate(): Double = {
    0
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
}
