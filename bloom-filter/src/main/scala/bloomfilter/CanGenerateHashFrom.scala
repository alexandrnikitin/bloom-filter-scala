package bloomfilter

import bloomfilter.MurmurHash3.LongPair

trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): LongPair
}

object CanGenerateHashFrom {

  implicit object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): LongPair = {
      println("long")
      new LongPair
    }
  }

  implicit object CanGenerateHashFromByteArray extends CanGenerateHashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): LongPair = new LongPair
  }

  implicit object CanGenerateHashFromBoolean extends CanGenerateHashFrom[Boolean] {
    override def generateHash(from: Boolean): LongPair = new LongPair
  }

  implicit object CanGenerateHashFromNothing extends CanGenerateHashFrom[Nothing] {
    override def generateHash(from: Nothing): LongPair = new LongPair
  }

}
