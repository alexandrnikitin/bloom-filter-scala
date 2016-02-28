package bloomfilter

import hashing.MurmurHash3

trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): Long
}

@com.github.ghik.silencer.silent
object CanGenerate128HashFrom {

  implicit object CanGenerate128HashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): Long =
      MurmurHash3.fmix64(from)
  }

}
