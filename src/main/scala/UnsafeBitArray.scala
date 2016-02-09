import scala.concurrent.util.Unsafe.{instance => unsafe}

class UnsafeBitArray(numberOfBits: Long) {
  private val indices = math.ceil(numberOfBits / 64).toLong
  private val ptr = unsafe.allocateMemory(indices)

  def get(index: Long): Boolean = {
    (unsafe.getLong(ptr + (index >>> 6)) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    val offset = ptr + (index >>> 6)
    val long = unsafe.getLong(offset)
    unsafe.putLong(offset, long | (1L << index))
  }
}
