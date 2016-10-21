package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeBitArray
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}

class UnsafeBitArraysSpec extends Properties("UnsafeBitArray") {
  def genListElems[A](max: Long)(implicit aGen: Gen[A]): Gen[List[A]] = {
    Gen.posNum[Int].map(_ % max).flatMap(i => Gen.listOfN(math.min(i, Int.MaxValue).toInt, aGen))
  }

  val genUnion = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2L)
    indices <- genListElems[Long](size)(Gen.chooseNum(0, size))
    thatIndices <- genListElems[Long](size)(Gen.chooseNum(0, size))
  } yield (size, indices, thatIndices)

  val genIntersection = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2L)
    indices <- genListElems[Long](size)(Gen.chooseNum(0, size))
    thatIndices <- genListElems[Long](size)(Gen.chooseNum(0, size))
    commonIndices <- genListElems[Long](size)(Gen.chooseNum(0, size))
  } yield (size, indices, thatIndices, commonIndices)


  property("|") = forAll(genUnion) {
    case (size: Long, indices: List[Long], thatIndices: List[Long]) =>
      val array = new UnsafeBitArray(size)
      indices.foreach(array.set)
      val thatArray = new UnsafeBitArray(size)
      thatIndices.foreach(thatArray.set)

      val sut = array | thatArray
      val result = (indices ++ thatIndices).forall(sut.get)

      array.dispose()
      thatArray.dispose()
      sut.dispose()

      result
  }

  property("&") = forAll(genIntersection) {
    case (size: Long, indices: List[Long], thatIndices: List[Long], commonIndices: List[Long]) =>
      val array = new UnsafeBitArray(size)
      indices.foreach(array.set)
      val thatArray = new UnsafeBitArray(size)
      thatIndices.foreach(thatArray.set)
      commonIndices.foreach(x => { array.set(x); thatArray.set(x) })

      val sut = array & thatArray
      val result = commonIndices.forall(sut.get)

      array.dispose()
      thatArray.dispose()
      sut.dispose()

      result
  }

}
