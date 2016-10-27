package tests.bloomfilter.mutable

import java.io._

import bloomfilter.mutable.BloomFilter
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

class BloomFilterSerializationSpec extends Properties("BloomFilter") {
  def genListElems[A](max: Long)(implicit aGen: Gen[A]): Gen[List[A]] = {
    Gen.posNum[Int].map(_ % max).flatMap(i => Gen.listOfN(math.min(i, Int.MaxValue).toInt, aGen))
  }

  val gen = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2L)
    indices <- genListElems[Long](size)(Gen.chooseNum(0, size))
  } yield (size, indices)

  property("writeTo & readFrom") = forAll(gen) {
    case (size: Long, indices: List[Long]) =>
      val initial = BloomFilter[Long](size, 0.01)
      indices.foreach(initial.add)

      val file = File.createTempFile("bloomFilterSerialized", ".tmp")
      val out = new FileOutputStream(file)
      initial.writeTo(out)
      val in = new FileInputStream(file)
      val sut = BloomFilter.readFrom[Long](in)

      val result = indices.forall(sut.mightContain)

      initial.dispose()
      sut.dispose()

      result
  }

}
