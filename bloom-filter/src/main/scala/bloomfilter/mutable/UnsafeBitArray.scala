package bloomfilter.mutable

import java.io.{DataInputStream, DataOutputStream, InputStream, OutputStream}

import scala.concurrent.util.Unsafe.{instance => unsafe}

class UnsafeBitArray(val numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits.toDouble / 8).toLong
  private val ptr = unsafe.allocateMemory(indices)
  unsafe.setMemory(ptr, indices, 0.toByte)
  def get(index: Long): Boolean = {
    (unsafe.getByte(ptr + (index >>> 3)) & (1L << (index & 0x7))) != 0
  }

  def set(index: Long): Unit = {
    val offset = ptr + (index >>> 3)
    val value = unsafe.getByte(offset)
    unsafe.putByte(offset, (value | (1L << (index & 0x7))).toByte)
  }

  def combine(that: UnsafeBitArray, combiner: (Long, Long) => Long): UnsafeBitArray = {
    val result = new UnsafeBitArray(this.numberOfBits)
    var index = 0L
    while (index < numberOfBits) {
      val thisLong = unsafe.getLong(this.ptr + (index >>> 6) * 8L)
      val thatLong = unsafe.getLong(that.ptr + (index >>> 6) * 8L)
      val longAtIndex = combiner(thisLong, thatLong)
      unsafe.putLong(result.ptr + (index >>> 6) * 8L, longAtIndex)
      index += 64
    }
    result
  }

  def |(that: UnsafeBitArray): UnsafeBitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise OR works only on arrays with the same number of bits")

    combine(that, _ | _)
  }

  def &(that: UnsafeBitArray): UnsafeBitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise AND works only on arrays with the same number of bits")

    combine(that, _ & _)
  }

  def getBitCount: Long = {
    throw new NotImplementedError("Not implemented yet")
  }

  def writeTo(out: OutputStream): Unit = {
    val dout = new DataOutputStream(out)
    var index = 0L
    while (index < numberOfBits) {
      dout.writeLong(unsafe.getLong(this.ptr + (index >>> 6) * 8L))
      index += 64
    }
  }

  def readFrom(in: InputStream): Unit = {
    val din = new DataInputStream(in)
    var index = 0L
    while (index < numberOfBits) {
      unsafe.putLong(this.ptr + (index >>> 6) * 8L, din.readLong())
      index += 64
    }
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)
}
