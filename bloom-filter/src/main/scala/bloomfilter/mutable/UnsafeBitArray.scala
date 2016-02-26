package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}

class UnsafeBitArray(numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits.toDouble / 64).toLong
  private val ptr = unsafe.allocateMemory(indices)
  unsafe.setMemory(ptr, indices, 0.toByte)

  def get(index: Long): Boolean = {
    (unsafe.getLong(ptr + (index >>> 6)) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    val offset = ptr + (index >>> 6)
    val long = unsafe.getLong(offset)
    unsafe.putLong(offset, long | (1L << index))
  }

  def getBitCount: Long = 0

  def dispose(): Unit = unsafe.freeMemory(ptr)
}
