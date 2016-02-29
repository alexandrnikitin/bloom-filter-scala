package bloomfilter

import bloomfilter.hashing.{MurmurHash3, MurmurHash3Generic}

trait CanGenerate128HashFrom[-From] {
  def generateHash(from: From, outHash: Hash): Unit
}

final class Hash {
  var _1: Long = 0
  var _2: Long = 0
}

@com.github.ghik.silencer.silent
object CanGenerate128HashFrom {

  implicit object CanGenerate128HashFromByteArray extends CanGenerate128HashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte], outHash: Hash): Unit =
      MurmurHash3Generic.murmurhash3_x64_128_pair(from, 0, from.length, 0, outHash)
  }

  implicit object CanGenerate128HashFromString extends CanGenerate128HashFrom[String] {

    import scala.concurrent.util.Unsafe.{instance => unsafe}

    private val valueOffset = unsafe.objectFieldOffset(classOf[String].getDeclaredField("value"))
    private val charBase = unsafe.arrayBaseOffset(classOf[Array[java.lang.Character]])

    override def generateHash(from: String, outHash: Hash): Unit = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_128(value, charBase, from.length * 2, 0)
    }
  }

  implicit object CanGenerate128HashFromLong extends CanGenerate128HashFrom[Long] {
    override def generateHash(from: Long, outHash: Hash): Unit = {
      val hash = MurmurHash3.fmix64(from)
      (hash, hash)
    }
  }

}
