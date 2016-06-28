package bloomfilter

import bloomfilter.hashing.MurmurHash3Generic

trait CanGenerateHashFrom[From] {
  def generateHash(from: From): Long
}

object CanGenerateHashFrom {

  implicit object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): Long = from
  }

  implicit object CanGenerateHashFromByteArray extends CanGenerateHashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): Long =
      MurmurHash3Generic.murmurhash3_x64_64(from, 0, from.length, 0)
  }

  implicit object CanGenerateHashFromString extends CanGenerateHashFrom[String] {

    import scala.concurrent.util.Unsafe.{instance => unsafe}

    private val valueOffset = unsafe.objectFieldOffset(classOf[String].getDeclaredField("value"))

    override def generateHash(from: String): Long = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_64(value, 0, from.length, 0)
    }
  }

}
