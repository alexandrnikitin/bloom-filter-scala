package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

class CuckooFilter[T](numberOfBuckets: Long, numberOfBitsPerItem: Int, private val bits: UnsafeTable8Bit)
    (implicit canGenerateHash: CanGenerateHashFrom[T]) {

  var numberOfItems = 0L

  def this(numberOfBuckets: Long, numberOfBitsPerItem: Int)(implicit canGenerateHash: CanGenerateHashFrom[T]) {
    this(numberOfBuckets, numberOfBitsPerItem, new UnsafeTable8Bit(numberOfBuckets))
  }

  import CuckooFilter._

  def add(x: T): Unit = {
    val (index, tag) = generateIndexTagHash(x)
    if (bits.insert(index, tag)) {
      numberOfItems += 1
      return
    }

    var curindex = index
    var curtag = tag

    var i = 0
    while (i < MaxAddAttempts) {
      curindex = altIndex(curindex, curtag, numberOfBuckets)
      val swappedTag = bits.swapAny(curindex, curtag)
      if (swappedTag == 0) {
        numberOfItems += 1
        return
      }
      curtag = swappedTag
      i += 1
    }
  }

  def remove(x: T): Unit = {
    val (index, tag) = generateIndexTagHash(x)
    if (bits.remove(index, tag)) {
      numberOfItems -= 1
      return
    }
    val index2 = altIndex(index, tag, numberOfBuckets)
    if(bits.remove(index2, tag)) {
      numberOfItems -= 1
      return
    }
  }

  def mightContain(x: T): Boolean = {
    val (index, tag) = generateIndexTagHash(x)
    if (bits.find(index, tag)) return true
    val index2 = altIndex(index, tag, numberOfBuckets)
    if (bits.find(index2, tag)) return true
    assert(index == altIndex(index2, tag, numberOfBuckets))
    false
  }

  def dispose(): Unit = bits.dispose()

  // TODO tuple
  //@inline
  private def generateIndexTagHash(x: T): (Long, Long) = {
    val hash = canGenerateHash.generateHash(x)
    val index = indexHash(hash >> 32, numberOfBuckets)
    val tag = tagHash(hash, numberOfBitsPerItem)
    (index, tag)
  }


}

object CuckooFilter {

  // TODO falsePositiveRate?
  def apply[T](numberOfItems: Long)(implicit canGenerateHash: CanGenerateHashFrom[T]): CuckooFilter[T] = {
    val nb = optimalNumberOfBuckets(numberOfItems)
    new CuckooFilter[T](nb, 8)
  }

  def optimalNumberOfBuckets(numberOfItems: Long): Long = {
    val assoc = 4 // TODO WTF?
    var num_buckets: Long = upperPowerOf2(numberOfItems / assoc)
    val frac = numberOfItems.toDouble / num_buckets / assoc
    if (frac > 0.96) {
      num_buckets = num_buckets << 1
    }
    num_buckets
  }


  val MaxAddAttempts = 500

  private def upperPowerOf2(l: Long): Long = {
    var x = l - 1
    x |= x >> 1
    x |= x >> 2
    x |= x >> 4
    x |= x >> 8
    x |= x >> 16
    x |= x >> 32
    x += 1
    x
  }

  private def altIndex(index: Long, tag: Long, numberOfBuckets: Long): Long = indexHash((index ^ (tag * 0x5bd1e995)).toInt, numberOfBuckets)

  private def indexHash(hash: Long, numberOfBuckets: Long): Long = {
    hash & (numberOfBuckets - 1)
  }

  private def tagHash(hash: Long, numberOfBitsPerItem: Long): Long = {
    var tag = hash & ((1L << numberOfBitsPerItem) - 1)
    if (tag == 0) tag += 1
    tag
  }


}
