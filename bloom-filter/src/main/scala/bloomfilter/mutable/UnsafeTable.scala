package bloomfilter.mutable

import java.io._

import scala.concurrent.util.Unsafe.{instance => unsafe}


// TODO macro for various bits?
trait UnsafeTable {
  def insert(index: Long, tag: Long): Boolean
  def swapAny(index: Long, tag: Long): Long
  def remove(index: Long, tag: Long): Boolean
  def find(index: Long, tag: Long): Boolean
  def dispose(): Unit

  protected def readPtrFrom(in: InputStream, ptr: Long, numBytes: Long): Unit = {
    val din = new DataInputStream(in)
    var n = 0L
    while (n + 8 <= numBytes) {
      val l = din.readLong()
      unsafe.putLong(ptr + n, l)
      n += 8
    }
    while (n < numBytes) {
      val b = din.readByte()
      unsafe.putByte(ptr + n, b)
      n += 1
    }
  }
  protected def writePtrTo(out: OutputStream, ptr: Long, numBytes: Long): Unit = {
    val dout = new DataOutputStream(out)
    var n = 0L
    while (n + 8 <= numBytes) {
      val l = unsafe.getLong(ptr + n)
      dout.writeLong(l)
      n += 8
    }
    while (n < numBytes) {
      val b = unsafe.getByte(ptr + n)
      dout.writeByte(b.toInt)
      n += 1
    }
  }

  protected def toSerializedForm(bytesPerBucket: Int, numberOfBuckets: Long): AnyRef = new UnsafeTable.SerializedForm(bytesPerBucket, numberOfBuckets, this)

  def writeTo(out: OutputStream): Unit
  def readFrom(in: InputStream): Unit
}

object UnsafeTable {

  @SerialVersionUID(1L)
  private class SerializedForm(bytesPerBucket: Int, numberOfBuckets: Long, @transient var unsafeTable: UnsafeTable) extends Serializable {
    private def writeObject(oos: ObjectOutputStream): Unit = {
      oos.defaultWriteObject()
      unsafeTable.writeTo(oos)
    }

    private def readObject(ois: ObjectInputStream): Unit = {
      ois.defaultReadObject()
      unsafeTable = bytesPerBucket match {
        case 8 => new UnsafeTable8Bit(numberOfBuckets)
        case 16 => new UnsafeTable16Bit(numberOfBuckets)
      }
      unsafeTable.readFrom(ois)
    }

    @throws(classOf[java.io.ObjectStreamException])
    private def readResolve: AnyRef = unsafeTable
  }

}

@SerialVersionUID(1L)
class UnsafeTable8Bit(val numberOfBuckets: Long) extends UnsafeTable with Serializable {

  import UnsafeTable8Bit._

  private val ptr = unsafe.allocateMemory(bytesPerBucket * numberOfBuckets)
  unsafe.setMemory(ptr, bytesPerBucket * numberOfBuckets, 0.toByte)

  def readTag(bucketIndex: Long, tagIndex: Int): Long = {
    val p = ptr + bucketIndex * bytesPerBucket + tagIndex
    val tag = unsafe.getByte(p)
    tag & tagMask
  }

  def writeTag(i: Long, j: Int, t: Long): Unit = {
    val p = ptr + i * bytesPerBucket
    val tag = t & tagMask
    unsafe.putByte(p + j, tag.toByte)
  }

  def insert(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return true
      }
      tagIndex += 1
    }

    false
  }

  def swapAny(index: Long, tag: Long): Long = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return EmptyTag
      }
      tagIndex += 1
    }

    random += 1
    val r = random & (TagsPerBucket - 1)
    val tagToSwap = readTag(index, r)
    writeTag(index, r, tag)
    tagToSwap
  }

  def remove(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == tag) {
        writeTag(index, tagIndex, EmptyTag)
        return true
      }
      tagIndex += 1
    }
    false
  }

  def find(index: Long, tag: Long): Boolean = {
    var i = 0
    while (i < TagsPerBucket) {
      val tag1 = readTag(index, i)
      if (tag1 == tag) {
        return true
      }
      i += 1
    }
    false
  }

  def writeTo(out: OutputStream): Unit = {
    writePtrTo(out, ptr, bytesPerBucket * numberOfBuckets)
  }

  def readFrom(in: InputStream): Unit = {
    readPtrFrom(in, ptr, bytesPerBucket * numberOfBuckets)
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)

  @throws(classOf[java.io.ObjectStreamException])
  private def writeReplace: AnyRef = toSerializedForm(8, numberOfBuckets)
}

object UnsafeTable8Bit {
  val EmptyTag = 0L
  val BitsPerItem = 8
  val TagsPerBucket = 4
  private var random = 0
  private val bytesPerBucket = (BitsPerItem * TagsPerBucket + 7) >> 3
  private val tagMask = (1L << BitsPerItem) - 1
}


@SerialVersionUID(1)
class UnsafeTable16Bit(val numberOfBuckets: Long) extends UnsafeTable with Serializable {

  import UnsafeTable16Bit._

  private val ptr = unsafe.allocateMemory(bytesPerBucket * numberOfBuckets)
  unsafe.setMemory(ptr, bytesPerBucket * numberOfBuckets, 0.toByte)

  def readTag(bucketIndex: Long, tagIndex: Int): Long = {
    val p = ptr + bucketIndex * bytesPerBucket + (tagIndex << 1)
    val tag = unsafe.getShort(p)
    tag & tagMask
  }

  def writeTag(bucketIndex: Long, tagIndex: Int, tag: Long): Unit = {
    val p = ptr + bucketIndex * bytesPerBucket + (tagIndex << 1)
    unsafe.putShort(p, (tag & tagMask).toShort)
  }

  def insert(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return true
      }
      tagIndex += 1
    }

    false
  }

  def swapAny(index: Long, tag: Long): Long = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == EmptyTag) {
        writeTag(index, tagIndex, tag)
        return EmptyTag
      }
      tagIndex += 1
    }

    random += 1
    val r = random & (TagsPerBucket - 1)
    val tagToSwap = readTag(index, r)
    writeTag(index, r, tag)
    tagToSwap
  }

  def remove(index: Long, tag: Long): Boolean = {
    var tagIndex = 0
    while (tagIndex < TagsPerBucket) {
      if (readTag(index, tagIndex) == tag) {
        writeTag(index, tagIndex, EmptyTag)
        return true
      }
      tagIndex += 1
    }
    false
  }

  def find(index: Long, tag: Long): Boolean = {
    var i = 0
    while (i < TagsPerBucket) {
      val tag1 = readTag(index, i)
      if (tag1 == tag) {
        return true
      }
      i += 1
    }
    false
  }

  def writeTo(out: OutputStream): Unit = {
    writePtrTo(out, ptr, bytesPerBucket * numberOfBuckets)
  }

  def readFrom(in: InputStream): Unit = {
    readPtrFrom(in, ptr, bytesPerBucket * numberOfBuckets)
  }

  def dispose(): Unit = unsafe.freeMemory(ptr)

  @throws(classOf[java.io.ObjectStreamException])
  private def writeReplace: AnyRef = toSerializedForm(16, numberOfBuckets)
}


object UnsafeTable16Bit {
  val EmptyTag = 0L
  val BitsPerItem = 16
  val TagsPerBucket = 4
  private var random = 0
  private val bytesPerBucket = (BitsPerItem * TagsPerBucket + 7) >> 3
  private val tagMask = (1L << BitsPerItem) - 1

}
