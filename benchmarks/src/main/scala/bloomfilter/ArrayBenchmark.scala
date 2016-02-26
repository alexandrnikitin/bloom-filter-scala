package bloomfilter

import java.util.BitSet

import bloomfilter.mutable.UnsafeBitArray
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import sandbox.bloomfilter.mutable.ChronicleBitArray

@State(Scope.Benchmark)
class ArrayBenchmark {

  private val numberOfBits = Int.MaxValue

  val unsafeBits = new UnsafeBitArray(numberOfBits.toLong)
  val bitsSet = new BitSet(numberOfBits)
  val chronicle = new ChronicleBitArray(numberOfBits.toLong)

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

  @Benchmark
  def getChronicle() = {
    chronicle.get(1)
    chronicle.get(10)
    chronicle.get(100)
    chronicle.get(1000)
    chronicle.get(10000)
    chronicle.get(100000)
    chronicle.get(1000000)
  }


}
