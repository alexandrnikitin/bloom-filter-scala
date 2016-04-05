package bloomfilter.hashing

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import sandbox.hashing.MurmurHash3

@State(Scope.Benchmark)
class MurmurHash3GenericBenchmark {

  val key = Range(0, 64).map(_.toByte).toArray

  @Benchmark
  def scalaVersion() = {
    MurmurHash3.murmurhash3_x64_128(key, 0, key.length, 0)
  }

  @Benchmark
  def genericVersion() = {
    MurmurHash3Generic.murmurhash3_x64_128(key, 0, key.length, 0)
  }
}
