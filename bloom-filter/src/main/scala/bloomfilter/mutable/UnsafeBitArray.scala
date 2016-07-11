package bloomfilter.mutable

import scala.concurrent.util.Unsafe.{instance => unsafe}

class UnsafeBitArray(val numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits.toDouble / 64).toLong
  private val ptr = unsafe.allocateMemory(8L * indices)
  unsafe.setMemory(ptr, 8L * indices, 0.toByte)

  def get(index: Long): Boolean = {
    (unsafe.getLong(ptr + (index >>> 6) * 8L) & (1L << index)) != 0
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

  def |(that: UnsafeBitArray): UnsafeBitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise OR works only on arrays with the same number of bits")

    combine(that, (b1: Byte, b2: Byte) => (b1 | b2).toByte)
  }

  def &(that: UnsafeBitArray): UnsafeBitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise AND works only on arrays with the same number of bits")

    combine(that, (b1: Byte, b2: Byte) => (b1 & b2).toByte)
  }

  def getBitCount: Long = {
    throw new NotImplementedError("Not implemented yet")
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)
}
