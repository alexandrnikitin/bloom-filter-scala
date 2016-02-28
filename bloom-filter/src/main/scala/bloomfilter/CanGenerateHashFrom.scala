package bloomfilter

import hashing.{MurmurHash3Generic, MurmurHash3}

trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): (Long, Long)
}

@com.github.ghik.silencer.silent
object CanGenerateHashFrom {

  implicit object CanGenerateHashFromByteArray extends CanGenerateHashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): (Long, Long) =
      MurmurHash3Generic.murmurhash3_x64_128(from, 0, from.length, 0)
  }

  implicit object CanGenerateHashFromString extends CanGenerateHashFrom[String] {
    override def generateHash(from: String): (Long, Long) = {
      val bytes = from.getBytes
      MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.length, 0)
    }
  }

  implicit object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): (Long, Long) =
      (MurmurHash3.fmix64(from), 0)
  }

}
