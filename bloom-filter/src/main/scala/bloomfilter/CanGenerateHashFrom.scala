package bloomfilter

import com.github.ghik.silencer.silent
import hashing.MurmurHash3Generic

import scala.concurrent.util.Unsafe.{instance => unsafe}

trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): Long
}

@silent
object CanGenerateHashFrom {

  implicit object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): Long = from
  }

  implicit object CanGenerateHashFromString extends CanGenerateHashFrom[String] {

    private val valueOffset = unsafe.objectFieldOffset(classOf[String].getDeclaredField("value"))
    private val charBase = unsafe.arrayBaseOffset(classOf[Array[java.lang.Character]])

    override def generateHash(from: String): Long = {
      val value = unsafe.getObject(from, valueOffset).asInstanceOf[Array[Char]]
      MurmurHash3Generic.murmurhash3_x64_64(value, charBase, from.length * 2, 0)
    }
  }

}
