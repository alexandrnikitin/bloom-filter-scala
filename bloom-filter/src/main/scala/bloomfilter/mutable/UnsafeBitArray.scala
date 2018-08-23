package bloomfilter.mutable

import java.io._

import bloomfilter.util.Unsafe.unsafe

@SerialVersionUID(1L)
class UnsafeBitArray(val numberOfBits: Long) extends Serializable {
  private val indices = math.ceil(numberOfBits.toDouble / 64).toLong
  @transient
  private val ptr = unsafe.allocateMemory(8L * indices)
  unsafe.setMemory(ptr, 8L * indices, 0.toByte)
  private var bitCount = 0L

  def get(index: Long): Boolean = {
    (unsafe.getLong(ptr + (index >>> 6) * 8L) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    val offset = ptr + (index >>> 6) * 8L
    val long = unsafe.getLong(offset)
    if ((long & (1L << index)) == 0) {
      unsafe.putLong(offset, long | (1L << index))
      bitCount += 1
    }
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
    bitCount
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

  @throws(classOf[java.io.ObjectStreamException])
  private def writeReplace: AnyRef = new UnsafeBitArray.SerializedForm(this)

}

object UnsafeBitArray {

  @SerialVersionUID(1l)
  private class SerializedForm(@transient var unsafeBitArray: UnsafeBitArray) extends Serializable {
    private def writeObject(oos: ObjectOutputStream): Unit = {
      oos.defaultWriteObject()
      oos.writeLong(unsafeBitArray.numberOfBits)
      unsafeBitArray.writeTo(oos)
    }

    private def readObject(ois: ObjectInputStream): Unit = {
      ois.defaultReadObject()
      val numberOfBits = ois.readLong()
      unsafeBitArray = new UnsafeBitArray(numberOfBits)
      unsafeBitArray.readFrom(ois)
    }

    @throws(classOf[java.io.ObjectStreamException])
    private def readResolve: AnyRef = unsafeBitArray
  }

}
