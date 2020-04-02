### 0.12.0
- Add Scala 2.13 support (via \#45)

### 0.11.0
- BREAKING: Add approximateElementCount method that estimates number of added elements. Thanks to @SidWeng. It's a breaking change because it serializes one more field (via \#37 and \#38)

### 0.10.1
- Change the default long hash function to MurMurHash3 (via \#33)

### 0.10.0

- Performance improvement: set a bit only if it's not set already (via \#28)
- \#22 Scala 2.12.1 support (via \#31). Thanks to Fedor Lavrentyev @fediq.
- \#29 Fix hashing of small strings (via \#32). 

### 0.9.0

- \#23 Serialization support (via \#25). Thanks to Eyal Farago @eyalfa.

### 0.8.0

- \#19 Cuckoo Filter (via \#20)

### 0.7.0

- \#5 Add serialization support.

### 0.6.0

- \#2 Scala 2.10 support.

### 0.5.0

- \#4 Union and intersection of two Bloom filters (via \#6). Thanks to Mario Pastorelli @melrief.

### 0.4.2

- Fix memory access in UnsafeBitArray. Must update. Thanks to @cmarxer (via e79ff243ac)

### 0.4.1

- Fix memory allocation in UnsafeBitArray. Must update. Thanks to @cmarxer (via \#9)

### 0.4.0

- Initial release
