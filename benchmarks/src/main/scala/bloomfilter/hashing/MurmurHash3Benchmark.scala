package bloomfilter.hashing

import java.nio.ByteBuffer

import sandbox.hashing.{MurmurHash3 => jMurmurHash3, CassandraMurmurHash, AlgebirdMurmurHash128}
import sandbox.hashing.MurmurHash3.LongPair
import com.yahoo.sketches.hash.{MurmurHash3 => yMurmurHash3}
import com.google.common.hash.Hashing
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

  val guavaMurmur = Hashing.murmur3_128()

  @Benchmark
  def guavaVersion() = {
    guavaMurmur.hashBytes(key, 0, key.length)
  }

  @Benchmark
  def cassandraVersion() = {
    CassandraMurmurHash.hash3_x64_128(ByteBuffer.wrap(key), 0, key.length, 0)
  }

  val algebirdMurmur = AlgebirdMurmurHash128(0)

  @Benchmark
  def algebirdVersion() = {
    algebirdMurmur.apply(key)
  }

  @Benchmark
  def yahooVersion() = {
    yMurmurHash3.hash(key, 0)
  }
}
