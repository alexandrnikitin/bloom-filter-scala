package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeBitArray
import org.scalacheck.Arbitrary._
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop._

class UnsafeBitArraysSpec extends Properties("UnsafeBitArray") {
  def genListElems[A](max: Int)(implicit aGen: Gen[A]): Gen[List[A]] = {
    Gen.posNum[Int].map(_ % max).flatMap(i => Gen.listOfN(i, aGen))
  }

  val numOfSetBits: Int = 100

  val genUnion = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    indices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    thatIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
  } yield (size, indices, thatIndices)

  val genIntersection = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    array = new UnsafeBitArray(size)
    indices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    thatArray = new UnsafeBitArray(size)
    thatIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    commonIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
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
      commonIndices.foreach(x => { thatArray.set(x); thatArray.set(x) })

      val sut = array & thatArray
      val result = commonIndices.forall(sut.get)

      array.dispose()
      thatArray.dispose()
      sut.dispose()

      result
  }

}
