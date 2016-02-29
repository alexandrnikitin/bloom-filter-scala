package bloomfilter

import bloomfilter.hashing.{MurmurHash3Generic, MurmurHash3}

trait CanGenerate128HashFrom[-From] {
  def generateHash(from: From): (Long, Long)
}

@com.github.ghik.silencer.silent
object CanGenerate128HashFrom {

  implicit object CanGenerate128HashFromByteArray extends CanGenerate128HashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): (Long, Long) =
      MurmurHash3Generic.murmurhash3_x64_128(from, 0, from.length, 0)
  }

  implicit object CanGenerate128HashFromString extends CanGenerate128HashFrom[String] {
    override def generateHash(from: String): (Long, Long) = {
      val bytes = from.getBytes
      MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.length, 0)
    }
  }

  implicit object CanGenerate128HashFromLong extends CanGenerate128HashFrom[Long] {
    override def generateHash(from: Long): (Long, Long) =
      (MurmurHash3.fmix64(from), 0)
  }

}
