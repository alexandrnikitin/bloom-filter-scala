package bloomfilter

import bloomfilter.hashing.MurmurHash3Generic

import java.lang.reflect.Field

trait CanGenerateHashFrom[From] {
  def generateHash(from: From): Long
}

object CanGenerateHashFrom {
  implicit case object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): Long = MurmurHash3Generic.fmix64(from)
  }

  implicit case object CanGenerateHashFromByteArray extends CanGenerateHashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): Long =
      MurmurHash3Generic.murmurhash3_x64_64(from, 0, from.length, 0)
  }

  import bloomfilter.util.Unsafe.unsafe

  case object CanGenerateHashFromString extends CanGenerateHashFrom[String] {
    private val valueOffset = unsafe.objectFieldOffset(stringValueField)

    override def generateHash(from: String): Long = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_64(value, 0, from.length * 2, 0)
    }
  }

  case object CanGenerateHashFromStringByteArray extends CanGenerateHashFrom[String] {
    private val valueOffset = unsafe.objectFieldOffset(stringValueField)

    override def generateHash(from: String): Long = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Byte]]
      MurmurHash3Generic.murmurhash3_x64_64(value, 0, from.length, 0)
    }
  }

  private val stringValueField: Field = classOf[String].getDeclaredField("value")
  implicit val canGenerateHashFromString: CanGenerateHashFrom[String] = {
    if (stringValueField.getType.getComponentType == java.lang.Byte.TYPE) CanGenerateHashFromStringByteArray else CanGenerateHashFromString
  }
}
