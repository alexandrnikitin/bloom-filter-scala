## Bloom filter for Scala

[![Build Status](https://travis-ci.org/alexandrnikitin/bloom-filter-scala.svg?branch=master)](https://travis-ci.org/alexandrnikitin/bloom-filter-scala)
[![codecov.io](https://codecov.io/github/alexandrnikitin/bloom-filter-scala/coverage.svg?branch=master)](https://codecov.io/github/alexandrnikitin/bloom-filter-scala?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alexandrnikitin/bloom-filter_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexandrnikitin/bloom-filter_2.11)

in progress...

### Overview

>A Bloom filter is a space-efficient probabilistic data structure that is used to test whether an element is a member of a set. False positive matches are possible, but false negatives are not. In other words, a query returns either "possibly in set" or "definitely not in set". Elements can be added to the set, but not removed

[More on wikipedia](https://en.wikipedia.org/wiki/Bloom_filter)

### Getting Started

```scala
libraryDependencies += "com.github.alexandrnikitin" %% "bloom-filter" % "0.3.1"
```

```scala
// Create a Bloom filter
val expectedElements = 1000000
val falsePositiveRate = 0.1
val bf = BloomFilter[String](expectedElements, falsePositiveRate)

// Put an element
bf.add(element)

// Check whether an element in a set
bf.mightContain(element)
```

TBA

### Documentation

TBA

### Motivation

TBA

### Benchmarks

Here's a benchmark for the `String` type:
```
[info] Benchmark                                              (length)   Mode  Cnt          Score         Error  Units
[info] alternatives.algebird.StringItemBenchmark.algebirdGet      1024  thrpt   20    1181080.172 ▒    9867.840  ops/s
[info] alternatives.algebird.StringItemBenchmark.algebirdPut      1024  thrpt   20     157158.453 ▒     844.623  ops/s
[info] alternatives.breeze.StringItemBenchmark.breezeGet          1024  thrpt   20    5113222.168 ▒   47005.466  ops/s
[info] alternatives.breeze.StringItemBenchmark.breezePut          1024  thrpt   20    4482377.337 ▒   19971.209  ops/s
[info] alternatives.guava.StringItemBenchmark.guavaGet            1024  thrpt   20    5712237.339 ▒  115453.495  ops/s
[info] alternatives.guava.StringItemBenchmark.guavaPut            1024  thrpt   20    5621712.282 ▒  307133.297  ops/s

[info] bloomfilter.mutable.StringItemBenchmark.myGet              1024  thrpt   20   11483828.730 ▒  342980.166  ops/s
[info] bloomfilter.mutable.StringItemBenchmark.myPut              1024  thrpt   20   11634399.272 ▒   45645.105  ops/s
[info] bloomfilter.mutable._128bit.StringItemBenchmark.myGet      1024  thrpt   20   11119086.965 ▒   43696.519  ops/s
[info] bloomfilter.mutable._128bit.StringItemBenchmark.myPut      1024  thrpt   20   11303765.075 ▒   52581.059  ops/s
```

TBA
