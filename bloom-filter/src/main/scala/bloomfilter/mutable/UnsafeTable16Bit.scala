package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}

trait UnsafeTable {
  def insert(index: Long, tag: Long): Boolean
  def swapAny(index: Long, tag: Long): Long
  def remove(index: Long, tag: Long): Boolean
  def find(index: Long, tag: Long): Boolean
  def dispose(): Unit
}

class UnsafeTable16Bit(val numberOfBuckets: Long) extends UnsafeTable {

  import UnsafeTable16Bit._

  private var random = 0
  private val tagsPerBucket = 4
  private val bytesPerBucket = (BitsPerItem * tagsPerBucket + 7) >> 3
  private val tagMask = (1L << BitsPerItem) - 1
  private val ptr = unsafe.allocateMemory(bytesPerBucket * numberOfBuckets)
  unsafe.setMemory(ptr, bytesPerBucket * numberOfBuckets, 0.toByte)

  def readTag(bucketIndex: Long, tagIndex: Int): Long = {
    val p = ptr + bucketIndex * bytesPerBucket + (tagIndex >> 1)
    val tag = unsafe.getShort(p)
    tag & tagMask
  }

  def writeTag(bucketIndex: Long, tagIndex: Int, tag: Long): Unit = {
    val p = ptr + bucketIndex * bytesPerBucket + (tagIndex >> 1)
    unsafe.putShort(p , (tag & tagMask).toShort)
  }

  def insert(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return true
      }
      tagIndex += 1
    }

    false
  }

  def swapAny(index: Long, tag: Long): Long = {
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return EmptyTag
      }
      tagIndex += 1
    }

    random += 1
    val r =  random & (tagsPerBucket - 1)
    val tagToSwap = readTag(index, r)
    writeTag(index, r, tag)
    tagToSwap
  }

  def remove(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == tag) {
        writeTag(index, tagIndex, EmptyTag)
        return true
      }
      tagIndex += 1
    }
    false
  }

  def find(index: Long, tag: Long): Boolean = {
    var i = 0
    while (i < tagsPerBucket) {
      val tag1 = readTag(index, i)
      if (tag1 == tag) {
        return true
      }
      i += 1
    }
    false
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)
}


object UnsafeTable16Bit {
  val EmptyTag = 0
  val BitsPerItem = 16
}
