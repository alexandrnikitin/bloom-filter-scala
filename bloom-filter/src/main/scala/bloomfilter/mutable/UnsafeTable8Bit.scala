package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}
import scala.util.Random

class UnsafeTable8Bit(val numberOfBuckets: Long) {
  private val random = new Random()
  private val tagsPerBucket = 4
  private val bytesPerBucket = (8 * tagsPerBucket + 7) >> 3
  private val tagMask = (1L << 8) - 1
  private val ptr = unsafe.allocateMemory(bytesPerBucket * numberOfBuckets)
  unsafe.setMemory(ptr, bytesPerBucket * numberOfBuckets, 0.toByte)

  def readTag(bucketIndex: Long, tagIndex: Int): Long = {
    val p = ptr + bucketIndex * bytesPerBucket + tagIndex
    val tag = unsafe.getByte(p)
    tag & tagMask
  }

  def writeTag(i: Long, j: Int, t: Long): Unit = {
    val p = ptr + i * bytesPerBucket
    val tag = t & tagMask
    unsafe.putByte(p + j, tag.toByte)
  }

  def insert(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == 0) {
        writeTag(index, tagIndex, tag)
        return true
      }
      tagIndex += 1
    }

    false
  }

  def insertWithReplace(index: Long, tag: Long): (Boolean, Long) = {
    var oldtagToRet = 0L
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == 0) {
        writeTag(index, tagIndex, tag)
        return (true, 0)
      }
      tagIndex += 1
    }

    val r = random.nextInt() % tagsPerBucket
    oldtagToRet = readTag(index, r)
    writeTag(index, r, tag)

    (false, oldtagToRet)
  }

  private def haszero4(x: Long) = (((x)-0x1111L) & (~(x)) & 0x8888L) > 0
  private def hasvalue4(x: Long, n: Long) = (haszero4((x) ^ (0x1111L * (n))))

  private def haszero8(x: Long): Boolean = (((x)-0x01010101L) & (~(x)) & 0x80808080L) > 0
  private def hasvalue8(x: Long, n: Long): Boolean = (haszero8((x) ^ (0x01010101L * (n))))

  private def haszero12(x: Long): Boolean = (((x)-0x001001001001L) & (~(x)) & 0x800800800800L) > 0
  private def hasvalue12(x: Long, n: Long): Boolean = (haszero12((x) ^ (0x001001001001L * (n))))

  private def haszero16(x: Long): Boolean =  (((x)-0x0001000100010001L) & (~(x)) & 0x8000800080008000L) > 0
  private def hasvalue16(x: Long, n: Long): Boolean = (haszero16((x) ^ (0x0001000100010001L * (n))))

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

  def findBoth(index: Long, index2: Long, tag: Long): Boolean = {
    val p1 = ptr + index * bytesPerBucket
    val p2 = ptr + index2 * bytesPerBucket
    val v1 = unsafe.getLong(p1)
    val v2 = unsafe.getLong(p2)

    var i = 0
    while (i < tagsPerBucket) {
      val tag1 = readTag(index, i)
      val tag2 = readTag(index2, i)
      if ((tag1 == tag) || (tag2 == tag)) {
        return true
      }
      i += 1
    }
    false
  }



  def dispose(): Unit = unsafe.freeMemory(ptr)
}
