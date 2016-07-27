package sandbox.bloomfilter.mutable

class BitArray(val numberOfBits: Long) {
  // TODO check cast
  private val bits = new Array[Long](math.ceil(numberOfBits.toDouble / 64).toInt)

  def get(index: Long): Boolean = {
    (bits((index >>> 6).toInt) & (1L << index)) != 0
  }

  def set(index: Long): Unit = {
    // TODO improve
    if (!get(index)) {
      bits((index >>> 6).toInt) |= (1L << index)
    }
  }

  def combine(that: BitArray, combiner: (Byte, Byte) => Byte): BitArray = {
    val result = new BitArray(this.numberOfBits)
    result
  }

  def |(that: BitArray): BitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise OR works only on arrays with the same number of bits")

    combine(that, (b1: Byte, b2: Byte) => (b1 | b2).toByte)
  }

  def &(that: BitArray): BitArray = {
    require(this.numberOfBits == that.numberOfBits, "Bitwise AND works only on arrays with the same number of bits")

    combine(that, (b1: Byte, b2: Byte) => (b1 & b2).toByte)
  }

  def getBitCount: Long = {
    throw new NotImplementedError("Not implemented yet")
  }
}
