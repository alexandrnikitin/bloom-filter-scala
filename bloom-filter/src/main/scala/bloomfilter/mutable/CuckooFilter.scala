package bloomfilter.mutable

import bloomfilter.CanGenerateHashFrom

import scala.util.Random

class CuckooFilter[T](numberOfBuckets: Long, numberOfBitsPerItem: Int, private val bits: UnsafeTable8Bit)
    (implicit canGenerateHash: CanGenerateHashFrom[T]) {

  var numberOfItems = 0L
  val random = new Random()

  def this(numberOfBuckets: Long, numberOfBitsPerItem: Int)(implicit canGenerateHash: CanGenerateHashFrom[T]) {
    this(numberOfBuckets, numberOfBitsPerItem, new UnsafeTable8Bit(numberOfBuckets, numberOfBitsPerItem))
  }

  import CuckooFilter._

  private def altIndex(index: Long, tag: Long): Long = indexHash((index ^ (tag * 0x5bd1e995)).toInt)

  def add(x: T): Unit = {
    val (index, tag) = generateIndexTagHash(x)
    var curindex = index
    var curtag = tag

    if (bits.insert(curindex, curtag)) {
      numberOfItems += 1
      return
    }

    curindex = altIndex(curindex, curtag)
    if (bits.insert(curindex, curtag)) {
      numberOfItems += 1
      return
    }

    // TODO sort out
    val r = random.nextInt() % 4
    var oldtag2 = bits.readTag(curindex, r)
    bits.writeTag(curindex, r, curtag)
    curtag = oldtag2

    var i = 0
    while (i < MaxAttempts) {
      val (success, oldtag) = bits.insertWithReplace(curindex, curtag)
      if (success) {
        numberOfItems += 1
        return
      }
      curtag = oldtag
      curindex = altIndex(curindex, curtag)
      i += 1
    }
  }

  def remove(x: T): Unit = {
    ???
  }

  def mightContain(x: T): Boolean = {
    val (index, tag) = generateIndexTagHash(x)
    if (bits.find(index, tag)) return true
    val index2 = altIndex(index, tag)
    if (bits.find(index2, tag)) return true
    assert(index == altIndex(index2, tag))
    false
  }

  def dispose(): Unit = bits.dispose()

  private def generateIndexTagHash(x: T): (Long, Long) = {
    val hash = canGenerateHash.generateHash(x)
    val index = indexHash(hash >> 32)
    val tag = tagHash(hash)
    (index, tag)
  }

  private def indexHash(hash: Long): Long = {
    hash & (numberOfBuckets - 1) // TODO bit and
  }

  private def tagHash(hash: Long): Long = {
    var tag = hash & ((1L << numberOfBitsPerItem) - 1)
    if (tag == 0) tag += 1
    tag
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


  val MaxAttempts = 500

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

}
