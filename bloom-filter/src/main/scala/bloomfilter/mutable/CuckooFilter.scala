package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

class CuckooFilter[T](numberOfBuckets: Long, numberOfBitsPerItem: Int, private val bits: UnsafeTable)
    (implicit canGenerateHash: CanGenerateHashFrom[T]) {

  def this(numberOfBuckets: Long, numberOfBitsPerItem: Int)(implicit canGenerateHash: CanGenerateHashFrom[T]) {
    this(numberOfBuckets, numberOfBitsPerItem, new UnsafeTable16Bit(numberOfBuckets))
  }

  import CuckooFilter._

  def add(x: T): Unit = {
    val hash = canGenerateHash.generateHash(x)
    val index = indexHash(hash >> 32, numberOfBuckets)
    val tag = tagHash(hash, numberOfBitsPerItem)
    if (bits.insert(index, tag)) {
      return
    }

    var curIndex = index
    var curTag = tag
    var i = 0
    while (i < MaxAddAttempts) {
      curIndex = altIndex(curIndex, curTag, numberOfBuckets)
      val swappedTag = bits.swapAny(curIndex, curTag)
      if (swappedTag == 0) {
        return
      }
      curTag = swappedTag
      i += 1
    }
  }

  def remove(x: T): Unit = {
    val hash = canGenerateHash.generateHash(x)
    val index = indexHash(hash >> 32, numberOfBuckets)
    val tag = tagHash(hash, numberOfBitsPerItem)
    if (bits.remove(index, tag)) return
    val index2 = altIndex(index, tag, numberOfBuckets)
    if (bits.remove(index2, tag)) return
  }

  def mightContain(x: T): Boolean = {
    val hash = canGenerateHash.generateHash(x)
    val index = indexHash(hash >> 32, numberOfBuckets)
    val tag = tagHash(hash, numberOfBitsPerItem)
    if (bits.find(index, tag)) return true
    val index2 = altIndex(index, tag, numberOfBuckets)
    if (bits.find(index2, tag)) return true
    false
  }

  def dispose(): Unit = bits.dispose()
}

object CuckooFilter {

  // TODO falsePositiveRate?
  def apply[T](numberOfItems: Long)(implicit canGenerateHash: CanGenerateHashFrom[T]): CuckooFilter[T] = {
    val nb = optimalNumberOfBuckets(numberOfItems)
    new CuckooFilter[T](nb, 16, new UnsafeTable16Bit(nb))
  }

  def optimalNumberOfBuckets(numberOfItems: Long): Long = {
    var numberOfBuckets = upperPowerOf2(numberOfItems / UnsafeTable16Bit.TagsPerBucket)
    val frac = numberOfItems.toDouble / numberOfBuckets / UnsafeTable16Bit.TagsPerBucket
    if (frac > 0.96) numberOfBuckets = numberOfBuckets << 1
    numberOfBuckets
  }


  val MaxAddAttempts = 500

  @inline
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

  @inline
  private def altIndex(index: Long, tag: Long, numberOfBuckets: Long): Long =
    indexHash((index ^ (tag * 0x5bd1e995)).toInt, numberOfBuckets)

  @inline
  private def indexHash(hash: Long, numberOfBuckets: Long): Long = {
    hash & (numberOfBuckets - 1)
  }

  @inline
  private def tagHash(hash: Long, numberOfBitsPerItem: Long): Long = {
    var tag = hash & ((1L << numberOfBitsPerItem) - 1)
    if (tag == 0) tag += 1
    tag
  }


}
