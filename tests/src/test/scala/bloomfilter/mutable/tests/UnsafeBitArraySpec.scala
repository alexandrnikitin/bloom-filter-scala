package bloomfilter.mutable.tests

import bloomfilter.mutable.UnsafeBitArray
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

class UnsafeBitArraySpec extends Properties("UnsafeBitArray") {

  val gen = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    array = new UnsafeBitArray(size)
    index <- Gen.choose[Long](0, size)
  } yield (array, index)

  property("set") = forAll(gen) {
    case (sut: UnsafeBitArray, index: Long) =>
      sut.set(index)
      sut.get(index)
  }

}
