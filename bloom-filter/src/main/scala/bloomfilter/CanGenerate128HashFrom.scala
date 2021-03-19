package bloomfilter

import bloomfilter.hashing.MurmurHash3Generic

import java.lang.reflect.Field

trait CanGenerate128HashFrom[From] {
  def generateHash(from: From): (Long, Long)
}

object CanGenerate128HashFrom {
  implicit case object CanGenerate128HashFromLong extends CanGenerate128HashFrom[Long] {
    override def generateHash(from: Long): (Long, Long) = {
      val hash = MurmurHash3Generic.fmix64(from)
      (hash, hash)
    }
  }

  implicit case object CanGenerate128HashFromByteArray extends CanGenerate128HashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): (Long, Long) =
      MurmurHash3Generic.murmurhash3_x64_128(from, 0, from.length, 0)
  }

  import bloomfilter.util.Unsafe.unsafe

  case object CanGenerate128HashFromString extends CanGenerate128HashFrom[String] {
    private val valueOffset = unsafe.objectFieldOffset(stringValueField)

    override def generateHash(from: String): (Long, Long) = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_128(value, 0, from.length * 2, 0)
    }
  }

  case object CanGenerate128HashFromStringByteArray extends CanGenerate128HashFrom[String] {
    private val valueOffset = unsafe.objectFieldOffset(stringValueField)

    override def generateHash(from: String): (Long, Long) = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Byte]]
      MurmurHash3Generic.murmurhash3_x64_128(value, 0, value.length, 0)
    }
  }

  private val stringValueField: Field = classOf[String].getDeclaredField("value")
  implicit val canGenerate128HashFromString: CanGenerate128HashFrom[String] = {
    if (stringValueField.getType.getComponentType == java.lang.Byte.TYPE) CanGenerate128HashFromStringByteArray else CanGenerate128HashFromString
  }
}
