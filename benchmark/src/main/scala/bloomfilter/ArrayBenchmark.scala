package bloomfilter

import java.util.BitSet

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

@State(Scope.Benchmark)
class ArrayBenchmark {

  private val numberOfBits = Int.MaxValue

  val unsafeBits = new UnsafeBitArray(numberOfBits)
  val bitsSet = new BitSet(numberOfBits)

  @Benchmark
  def getUnsafe() = {
    unsafeBits.get(1)
    unsafeBits.get(10)
    unsafeBits.get(100)
    unsafeBits.get(1000)
    unsafeBits.get(10000)
    unsafeBits.get(100000)
    unsafeBits.get(1000000)
  }

  @Benchmark
  def getBitSet() = {
    bitsSet.get(1)
    bitsSet.get(10)
    bitsSet.get(100)
    bitsSet.get(1000)
    bitsSet.get(10000)
    bitsSet.get(100000)
    bitsSet.get(1000000)
  }


}
