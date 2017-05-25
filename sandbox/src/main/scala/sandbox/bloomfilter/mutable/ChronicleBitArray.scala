package sandbox.bloomfilter.mutable

import net.openhft.chronicle.bytes.NativeBytesStore

import bloomfilter.util.Unsafe.unsafe

class ChronicleBitArray(numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits.toDouble / 64).toLong
  private val ptr = unsafe.allocateMemory(indices)
  private val bytes = new NativeBytesStore(ptr, indices)

  def get(index: Long): Boolean = {
    (bytes.readLong(index >>> 6) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    val offset = index >>> 6
    val long = bytes.readLong(offset)
    val _ = bytes.writeLong(offset, long | (1L << index))
  }

  def getBitCount: Long = 0
}
