package bloomfilter

import bloomfilter.hash.MurmurHash3
import bloomfilter.hash.MurmurHash3.LongPair

trait CanGenerateHashFrom[-From] {
  def generateHash(from: From): LongPair
}

object CanGenerateHashFrom {

  implicit object CanGenerateHashFromLong extends CanGenerateHashFrom[Long] {
    override def generateHash(from: Long): LongPair = new LongPair
  }

  implicit object CanGenerateHashFromByteArray extends CanGenerateHashFrom[Array[Byte]] {
    override def generateHash(from: Array[Byte]): LongPair = {
      val pair = new LongPair
      MurmurHash3.murmurhash3_x64_128(from, 0, from.length, 0, pair)
      pair
    }
  }

  implicit object CanGenerateHashFromNothing extends CanGenerateHashFrom[Nothing] {
    override def generateHash(from: Nothing): LongPair = new LongPair
  }

}
