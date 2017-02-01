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
    var p = bucketIndex * bytesPerBucket
    var tag = 0
    /* following code only works for little-endian */
    if (numberOfBitsPerItem == 2) {
      tag = unsafe.getByte(ptr + p) >> (tagIndex * 2)
    } else if (numberOfBitsPerItem == 4) {
      p += (tagIndex >> 1)
      tag = unsafe.getByte(ptr + p) >> ((tagIndex & 1) << 2)
    } else if (numberOfBitsPerItem == 8) {
      p += tagIndex
      tag = unsafe.getByte(ptr + p)
    } else if (numberOfBitsPerItem == 12) {
      p += tagIndex + (tagIndex >> 1)
      tag = unsafe.getShort(ptr + p) >> ((tagIndex & 1) << 2)
    } else if (numberOfBitsPerItem == 16) {
      p += (tagIndex << 1)
      tag = unsafe.getShort(ptr + p)
    } else if (numberOfBitsPerItem == 32) {
      tag = unsafe.getInt(ptr + p + tagIndex * 4)
    }

    tag & tagMask
  }


  def writeTag(i: Long, j: Int, t: Long): Unit = {
    var p = i * bytesPerBucket
    val tag = t & tagMask
    /* following code only works for little-endian */
    if (numberOfBitsPerItem == 2) {
      val b = unsafe.getByte(ptr + p)
      unsafe.putByte(p, (b | tag << (2 * j)).toByte)
    } else if (numberOfBitsPerItem == 4) {
      p += (j >> 1)
      if ((j & 1) == 0) {
        val b = unsafe.getByte(ptr + p)
        unsafe.putByte(p, (b & 0xf0| tag).toByte)
      } else {
        val b = unsafe.getByte(ptr + p)
        unsafe.putByte(p, (b & 0x0f | (tag << 4)).toByte)
      }
    } else if (numberOfBitsPerItem == 8) {
      unsafe.putByte(p + j, tag.toByte)
    } else if (numberOfBitsPerItem == 12) {
      p += (j + (j >> 1))
      if ((j & 1) == 0) {
        val b = unsafe.getShort(ptr + p)
        unsafe.putShort(p, (b & 0xf000| tag).toShort)
      } else {
        val b = unsafe.getShort(ptr + p)
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
    var i = 0
    while (i < tagsPerBucket) {
      if (readTag(index, i) == 0) {
        writeTag(index, i, tag)
        return (true, oldtagToRet)
      }

      i += 1
    }

    if (kickout) {
      val r = Random.nextInt() % tagsPerBucket
      oldtagToRet = readTag(index, r)
      writeTag(index, r, tag)
    }

    (false, oldtagToRet)
  }


  def dispose(): Unit = unsafe.freeMemory(ptr)
}