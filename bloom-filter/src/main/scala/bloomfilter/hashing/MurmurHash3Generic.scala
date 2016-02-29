package bloomfilter.hashing

import java.lang.Long.rotateLeft

import bloomfilter.CanGetDataFrom

object MurmurHash3Generic {

  private val c1: Long = 0x87c37b91114253d5L
  private val c2: Long = 0x4cf5ad432745937fL

  def fmix64(l: Long): Long = {
    var k = l
    k ^= k >>> 33
    k *= 0xff51afd7ed558ccdL
    k ^= k >>> 33
    k *= 0xc4ceb9fe1a85ec53L
    k ^= k >>> 33
    k
  }

  def murmurhash3_x64_128[From](key: From, offset: Int, len: Int, seed: Int)(implicit cgdf: CanGetDataFrom[From]): (Long, Long) = {
    var h1: Long = seed & 0x00000000FFFFFFFFL
    var h2: Long = seed & 0x00000000FFFFFFFFL

    val roundedEnd = offset + (len & 0xFFFFFFF0);  // round down to 16 byte block

    var i = offset
    while (i < roundedEnd) {
      var k1 = cgdf.getLong(key, i)
      var k2 = cgdf.getLong(key, i + 8)
      k1 *= c1; k1 = rotateLeft(k1, 31); k1 *= c2; h1 ^= k1
      h1 = rotateLeft(h1, 27); h1 += h2; h1 = h1 * 5 + 0x52dce729
      k2 *= c2; k2 = rotateLeft(k2, 33); k2 *= c1; h2 ^= k2
      h2 = rotateLeft(h2, 31); h2 += h1; h2 = h2 * 5 + 0x38495ab5

      i += 16
    }

    var k1: Long = 0
    var k2: Long = 0

    val lenVar = len & 15
    if (lenVar == 15) k2 = (cgdf.getByte(key, roundedEnd + 14) & 0xffL) << 48
    if (lenVar >= 14) k2 |= (cgdf.getByte(key, roundedEnd + 13) & 0xffL) << 40
    if (lenVar >= 13) k2 |= (cgdf.getByte(key, roundedEnd + 12) & 0xffL) << 32
    if (lenVar >= 12) k2 |= (cgdf.getByte(key, roundedEnd + 11) & 0xffL) << 24
    if (lenVar >= 11) k2 |= (cgdf.getByte(key, roundedEnd + 10) & 0xffL) << 16
    if (lenVar >= 10) k2 |= (cgdf.getByte(key, roundedEnd + 9) & 0xffL) << 8
    if (lenVar >= 9) {
      k2 |= (cgdf.getByte(key, roundedEnd + 8) & 0xffL)
      k2 *= c2
      k2 = rotateLeft(k2, 33)
      k2 *= c1
      h2 ^= k2
    }
    if (lenVar >= 8) k1 = cgdf.getByte(key, roundedEnd + 7).toLong << 56
    if (lenVar >= 7) k1 |= (cgdf.getByte(key, roundedEnd + 6) & 0xffL) << 48
    if (lenVar >= 6) k1 |= (cgdf.getByte(key, roundedEnd + 5) & 0xffL) << 40
    if (lenVar >= 5) k1 |= (cgdf.getByte(key, roundedEnd + 4) & 0xffL) << 32
    if (lenVar >= 4) k1 |= (cgdf.getByte(key, roundedEnd + 3) & 0xffL) << 24
    if (lenVar >= 3) k1 |= (cgdf.getByte(key, roundedEnd + 2) & 0xffL) << 16
    if (lenVar >= 2) k1 |= (cgdf.getByte(key, roundedEnd + 1) & 0xffL) << 8
    if (lenVar >= 1) {
      k1 |= (cgdf.getByte(key, roundedEnd) & 0xffL)
      k1 *= c1
      k1 = rotateLeft(k1, 31)
      k1 *= c2
      h1 ^= k1
    }

    h1 ^= len; h2 ^= len

    h1 += h2
    h2 += h1

    h1 = fmix64(h1)
    h2 = fmix64(h2)

    h1 += h2
    h2 += h1

    (h1, h2)
  }

  def murmurhash3_x64_64[From](key: From, offset: Int, len: Int, seed: Int)(implicit cgdf: CanGetDataFrom[From]): Long = {
    var h1: Long = seed & 0x00000000FFFFFFFFL
    var h2: Long = seed & 0x00000000FFFFFFFFL

    val roundedEnd = offset + (len & 0xFFFFFFF0);  // round down to 16 byte block

    var i = offset
    while (i < roundedEnd) {
      var k1 = cgdf.getLong(key, i)
      var k2 = cgdf.getLong(key, i + 8)
      k1 *= c1; k1 = rotateLeft(k1, 31); k1 *= c2; h1 ^= k1
      h1 = rotateLeft(h1, 27); h1 += h2; h1 = h1 * 5 + 0x52dce729
      k2 *= c2; k2 = rotateLeft(k2, 33); k2 *= c1; h2 ^= k2
      h2 = rotateLeft(h2, 31); h2 += h1; h2 = h2 * 5 + 0x38495ab5

      i += 16
    }

    var k1: Long = 0
    var k2: Long = 0

    val lenVar = len & 15
    if (lenVar == 15) k2 = (cgdf.getByte(key, roundedEnd + 14) & 0xffL) << 48
    if (lenVar >= 14) k2 |= (cgdf.getByte(key, roundedEnd + 13) & 0xffL) << 40
    if (lenVar >= 13) k2 |= (cgdf.getByte(key, roundedEnd + 12) & 0xffL) << 32
    if (lenVar >= 12) k2 |= (cgdf.getByte(key, roundedEnd + 11) & 0xffL) << 24
    if (lenVar >= 11) k2 |= (cgdf.getByte(key, roundedEnd + 10) & 0xffL) << 16
    if (lenVar >= 10) k2 |= (cgdf.getByte(key, roundedEnd + 9) & 0xffL) << 8
    if (lenVar >= 9) {
      k2 |= (cgdf.getByte(key, roundedEnd + 8) & 0xffL)
      k2 *= c2
      k2 = rotateLeft(k2, 33)
      k2 *= c1
      h2 ^= k2
    }
    if (lenVar >= 8) k1 = cgdf.getByte(key, roundedEnd + 7).toLong << 56
    if (lenVar >= 7) k1 |= (cgdf.getByte(key, roundedEnd + 6) & 0xffL) << 48
    if (lenVar >= 6) k1 |= (cgdf.getByte(key, roundedEnd + 5) & 0xffL) << 40
    if (lenVar >= 5) k1 |= (cgdf.getByte(key, roundedEnd + 4) & 0xffL) << 32
    if (lenVar >= 4) k1 |= (cgdf.getByte(key, roundedEnd + 3) & 0xffL) << 24
    if (lenVar >= 3) k1 |= (cgdf.getByte(key, roundedEnd + 2) & 0xffL) << 16
    if (lenVar >= 2) k1 |= (cgdf.getByte(key, roundedEnd + 1) & 0xffL) << 8
    if (lenVar >= 1) {
      k1 |= (cgdf.getByte(key, roundedEnd) & 0xffL)
      k1 *= c1
      k1 = rotateLeft(k1, 31)
      k1 *= c2
      h1 ^= k1
    }

    h1 ^= len; h2 ^= len

    h1 += h2
    h2 += h1

    h1 = fmix64(h1)
    h2 = fmix64(h2)

    h1 += h2
    h2 += h1

    h1 + h2
  }
}
