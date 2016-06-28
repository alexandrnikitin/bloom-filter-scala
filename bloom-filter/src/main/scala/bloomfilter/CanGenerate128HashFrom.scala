package bloomfilter

import bloomfilter.hashing.MurmurHash3Generic

trait CanGenerate128HashFrom[From] {
  def generateHash(from: From): (Long, Long)
}

object CanGenerate128HashFrom {

  implicit object CanGenerate128HashFromByteArray extends CanGenerate128HashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): (Long, Long) =
      MurmurHash3Generic.murmurhash3_x64_128(from, 0, from.length, 0)
  }

  implicit object CanGenerate128HashFromString extends CanGenerate128HashFrom[String] {

    import scala.concurrent.util.Unsafe.{instance => unsafe}

    private val valueOffset = unsafe.objectFieldOffset(classOf[String].getDeclaredField("value"))

    override def generateHash(from: String): (Long, Long) = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_128(value, 0, from.length, 0)
    }
  }

  implicit object CanGenerate128HashFromLong extends CanGenerate128HashFrom[Long] {
    override def generateHash(from: Long): (Long, Long) = {
      val hash = MurmurHash3Generic.fmix64(from)
      (hash, hash)
    }
  }

}
