package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}
import scala.util.Random

class UnsafeTable(val numberOfBuckets: Long, numberOfBitsPerItem: Int) {
  private val tagsPerBucket = 4
  private val bytesPerBucket = (numberOfBitsPerItem * tagsPerBucket + 7) >> 3
  private val tagMask = (1L << numberOfBitsPerItem) - 1
  private val ptr = unsafe.allocateMemory(bytesPerBucket * numberOfBuckets)
  unsafe.setMemory(ptr, bytesPerBucket * numberOfBuckets, 0.toByte)

  def readTag(bucketIndex: Long, tagIndex: Int): Long = {
    var p = ptr + bucketIndex * bytesPerBucket
    var tag = 0
    /* following code only works for little-endian */
    if (numberOfBitsPerItem == 2) {
      tag = unsafe.getByte(p) >> (tagIndex * 2)
    } else if (numberOfBitsPerItem == 4) {
      p += (tagIndex >> 1)
      tag = unsafe.getByte(p) >> ((tagIndex & 1) << 2)
    } else if (numberOfBitsPerItem == 8) {
      p += tagIndex
      tag = unsafe.getByte(p)
    } else if (numberOfBitsPerItem == 12) {
      p += tagIndex + (tagIndex >> 1)
      tag = unsafe.getShort(p) >> ((tagIndex & 1) << 2)
    } else if (numberOfBitsPerItem == 16) {
      p += (tagIndex << 1)
      tag = unsafe.getShort(p)
    } else if (numberOfBitsPerItem == 32) {
      tag = unsafe.getInt(p + tagIndex * 4)
    }

    tag & tagMask
  }


  def writeTag(i: Long, j: Int, t: Long): Unit = {
    var p = ptr + i * bytesPerBucket
    val tag = t & tagMask
    /* following code only works for little-endian */
    if (numberOfBitsPerItem == 2) {
      val b = unsafe.getByte(p)
      unsafe.putByte(p, (b | tag << (2 * j)).toByte)
    } else if (numberOfBitsPerItem == 4) {
      p += (j >> 1)
      if ((j & 1) == 0) {
        val b = unsafe.getByte(p)
        unsafe.putByte(p, (b & 0xf0| tag).toByte)
      } else {
        val b = unsafe.getByte(p)
        unsafe.putByte(p, (b & 0x0f | (tag << 4)).toByte)
      }
    } else if (numberOfBitsPerItem == 8) {
      unsafe.putByte(p + j, tag.toByte)
    } else if (numberOfBitsPerItem == 12) {
      p += (j + (j >> 1))
      if ((j & 1) == 0) {
        val b = unsafe.getShort(p)
        unsafe.putShort(p, (b & 0xf000| tag).toShort)
      } else {
        val b = unsafe.getShort(p)
        unsafe.putShort(p, (b & 0x000f | (tag << 4)).toShort)
      }
    } else if (numberOfBitsPerItem == 16) {
      unsafe.putShort(p + j * 2, tag.toShort)
    } else if (numberOfBitsPerItem == 32) {
      unsafe.putInt(p + j * 4, tag.toInt)
    }
  }

  def insert(index: Long, tag: Long, kickout: Boolean): (Boolean, Long) = {
    var oldtagToRet = 0L
    var tagIndex = 0
    while (tagIndex < tagsPerBucket) {
      if (readTag(index, tagIndex) == 0) {
        writeTag(index, tagIndex, tag)
        return (true, oldtagToRet)
      }
      tagIndex += 1
    }

    if (kickout) {
      val r = Random.nextInt() % tagsPerBucket
      oldtagToRet = readTag(index, r)
      writeTag(index, r, tag)
    }

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

  def find(index: Long, index2: Long, tag: Long): Boolean = {
    val p1 = ptr + index * bytesPerBucket
    val p2 = ptr + index2 * bytesPerBucket
    val v1 = unsafe.getLong(p1)
    val v2 = unsafe.getLong(p2)

    // caution: unaligned access & assuming little endian
    if (numberOfBitsPerItem == 4 && tagsPerBucket == 4) {
      hasvalue4(v1, tag) || hasvalue4(v2, tag)
    } else if (numberOfBitsPerItem == 8 && tagsPerBucket == 4) {
      hasvalue8(v1, tag) || hasvalue8(v2, tag)
    } else if (numberOfBitsPerItem == 12 && tagsPerBucket == 4) {
      hasvalue12(v1, tag) || hasvalue12(v2, tag)
    } else if (numberOfBitsPerItem == 16 && tagsPerBucket == 4) {
      hasvalue16(v1, tag) || hasvalue16(v2, tag)
    } else {
      var i = 0
      while (i < tagsPerBucket) {
        if ((readTag(index, i) == tag) || (readTag(index2, i) == tag)) {
          return true
        }
        i+=1
      }
      false
    }
  }



  def dispose(): Unit = unsafe.freeMemory(ptr)
}
