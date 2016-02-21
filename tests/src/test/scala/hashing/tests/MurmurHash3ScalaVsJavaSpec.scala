package hashing.tests

import hashing.MurmurHash3
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import sandbox.hashing.MurmurHash3.LongPair
import sandbox.hashing.{MurmurHash3 => jMurmurHash3}

object MurmurHash3ScalaVsJavaSpec extends Properties("MurmurHash3") {

  property("murmurhash3_x64_128") = forAll { (key: Array[Byte]) =>
    val tuple = MurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0)
    val pair = new LongPair
    jMurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0, pair)
    pair.val1 == tuple._1 && pair.val2 == tuple._2
  }

}
