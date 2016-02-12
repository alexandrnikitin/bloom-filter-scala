package bloomfilter

import bloomfilter.MurmurHash3.LongPair

import scala.annotation.implicitNotFound

@implicitNotFound(msg = "Cannot generate hash for a type ${From}.")
trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): LongPair
}

object CanGenerateHashFrom {
  lazy val longHash = new CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): LongPair = {
      val out: LongPair = new LongPair
      out.val1 = 0
      out.val2 = 0
      out
    }
  }

  implicit def canGenerateHash: CanGenerateHashFrom[Long] = longHash
}
