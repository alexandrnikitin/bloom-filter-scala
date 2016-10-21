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

  def dispose(): Unit = unsafe.freeMemory(ptr)
}
