package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}

class UnsafeBitArray(val numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits.toDouble / 64).toLong
  private val ptr = unsafe.allocateMemory(8L*indices)
  unsafe.setMemory(ptr, 8L*indices, 0.toByte)

  def get(index: Long): Boolean = {
    (unsafe.getLong(ptr + (index >>> 6)*8L) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    val offset = ptr + (index >>> 6) * 8L
    val long = unsafe.getLong(offset)
    unsafe.putLong(offset, long | (1L << index))
  }

  def combine(that: UnsafeBitArray, combiner: (Byte, Byte) => Byte): UnsafeBitArray = {
    val result = new UnsafeBitArray(this.numberOfBits)
    for (index <- 0L until (indices * 8)) {
      val thisByte = unsafe.getByte(this.ptr + index)
      val thatByte = unsafe.getByte(that.ptr + index)
      val byteAtIndex = combiner(thisByte, thatByte)
      unsafe.putByte(result.ptr + index, byteAtIndex.toByte)
    }
    result
  }

  def |(that: UnsafeBitArray): UnsafeBitArray =
    combine(that, (b1: Byte, b2: Byte) => (b1 | b2).toByte)

  def &(that: UnsafeBitArray): UnsafeBitArray =
    combine(that, (b1: Byte, b2: Byte) => (b1 & b2).toByte)

  def getBitCount: Long = {
    throw new NotImplementedError("Not implemented yet")
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)

  def dump():Array[Byte] = {
    val bytes:Array[Byte] = Array.fill[Byte](indices.toInt*8)(0.toByte)
    unsafe.copyMemory(
      null,
      ptr,
      bytes,
      sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET,
      indices*8L);
    bytes
  }

  def setArray(a:Array[Byte]) = {
    assert(a.length == indices*8L, "array length mismatch")
    for (index <- 0L to indices*8L-1L) {
      val current = unsafe.getByte(ptr+index)
      unsafe.putByte(ptr+index, (current | a(index.toInt)).toByte)
    }
  }

}
