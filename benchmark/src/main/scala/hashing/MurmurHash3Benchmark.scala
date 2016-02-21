package hashing

import bloomfilter.hash.MurmurHash3.LongPair
import bloomfilter.hash.{MurmurHash3 => jMurmurHash3}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}


@State(Scope.Benchmark)
class MurmurHash3Benchmark {

  val key = Range(0, 64).map(_.toByte).toArray

  @Benchmark
  def javaVersion() = {
    jMurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0, new LongPair)
  }

  @Benchmark
  def scalaVersion() = {
    MurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0)
  }
}
