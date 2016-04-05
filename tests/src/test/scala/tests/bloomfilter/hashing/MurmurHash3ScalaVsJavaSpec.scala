package tests.bloomfilter.hashing

import bloomfilter.hashing.MurmurHash3Generic
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import sandbox.hashing.YonikMurmurHash3
import sandbox.hashing.YonikMurmurHash3.LongPair

object MurmurHash3ScalaVsJavaSpec extends Properties("MurmurHash3ScalaVsJavaSpec") {

  property("murmurhash3_x64_128") = forAll { (key: Array[Byte]) =>
    val tuple = MurmurHash3Generic.murmurhash3_x64_128(key, 0, key.length, 0)
    val pair = new LongPair
    YonikMurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0, pair)
    pair.val1 == tuple._1 && pair.val2 == tuple._2
  }

}
