package tests.bloomfilter.mutable

import java.io._

import bloomfilter.mutable.BloomFilter
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import org.scalatest.Matchers

class BloomFilterSerializationSpec extends Properties("BloomFilter") with Matchers {
  def genListElems[A](max: Long)(implicit aGen: Gen[A]): Gen[List[A]] = {
    Gen.posNum[Int].map(_ % max).flatMap(i => Gen.listOfN(math.min(i, Int.MaxValue).toInt, aGen))
  }

  val gen = for {
    size <- Gen.oneOf[Long](1, 1000 /*, Int.MaxValue.toLong + 1*/)
    indices <- genListElems[Long](size)(Gen.chooseNum(0, size - 1))
  } yield (size, indices)

  property("writeTo & readFrom") = forAll(gen) {
    case (size: Long, indices: List[Long]) =>
      val initial = BloomFilter[Long](size, 0.01)
      indices.foreach(initial.add)

      val file = File.createTempFile("bloomFilterSerialized", ".tmp")
      val out = new BufferedOutputStream(new FileOutputStream(file), 10 * 1000 * 1000)
      initial.writeTo(out)
      out.close()
      val in = new BufferedInputStream(new FileInputStream(file), 10 * 1000 * 1000)
      val sut = BloomFilter.readFrom[Long](in)
      in.close()

      val result = indices.forall(sut.mightContain)

      file.delete()
      initial.dispose()
      sut.dispose()

      result
  }

  property("supports java serialization") = {
    forAll(gen) {
      case (size, indices) =>
        val initial = BloomFilter[Long](size, 0.01)
        indices.foreach(initial.add)
        val file = File.createTempFile("bloomFilterSerialized", ".tmp")
        val out = new BufferedOutputStream(new FileOutputStream(file), 10 * 1000 * 1000)
        val oos = new ObjectOutputStream(out)
        oos.writeObject(initial)
        oos.close()
        out.close()
        val in = new BufferedInputStream(new FileInputStream(file), 10 * 1000 * 1000)
        val ois = new ObjectInputStream(in)
        val desrialized = ois.readObject()
        ois.close()
        in.close()

        desrialized should not be null
        desrialized should be(a[BloomFilter[Long]])
        val sut = desrialized.asInstanceOf[BloomFilter[Long]]

        sut.numberOfBits shouldEqual initial.numberOfBits
        sut.numberOfHashes shouldEqual initial.numberOfHashes


        val result = indices.forall(sut.mightContain)

        file.delete()
        initial.dispose()
        sut.dispose()

        result
    }
  }

}
