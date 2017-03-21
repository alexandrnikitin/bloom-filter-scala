## Bloom filter for Scala

[![Build Status](https://travis-ci.org/alexandrnikitin/bloom-filter-scala.svg?branch=master)](https://travis-ci.org/alexandrnikitin/bloom-filter-scala)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alexandrnikitin/bloom-filter_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexandrnikitin/bloom-filter_2.11)

### Overview

>"A Bloom filter is a space-efficient probabilistic data structure that is used to test whether an element is a member of a set. False positive matches are possible, but false negatives are not. In other words, a query returns either "possibly in set" or "definitely not in set". Elements can be added to the set, but not removed," says [Wikipedia][wiki-bloom-filter].

What's Bloom filter in a nutshell:

- Optimization for memory. It comes into play when you cannot put whole set into memory.
- Solves the membership problem. It can answer one question: does an element belong to a set or not?
- Probabilistic (lossy) data structure. It can answer that an element **probably belongs** to a set with some probability.

### Getting Started

```scala
libraryDependencies += "com.github.alexandrnikitin" %% "bloom-filter" % "latest.release"
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

// Dispose the instance
bf.dispose()
```

### Motivation

You can read about this Bloom filter and motivation behind in [my blog post][post]

### Benchmarks

Here's a benchmark for the `String` type and results for other types are very similar to these:

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

Basically, this implementation is 2x faster than Google's Guava and 10-80x than Twitter's Algebird. Other benchmarks you can find in [the "benchmarks' module on github][github-benchmarks]

Warning: These are synthetic benchmarks in isolated environment. Usually the difference in throughput and latency is bigger in production system because it will stress the GC, lead to slow allocation paths and higher latencies, trigger the GC, etc.

  [wiki-bloom-filter]: https://en.wikipedia.org/wiki/Bloom_filter
  [post]: https://alexandrnikitin.github.io/blog/bloom-filter-for-scala/
  [github-benchmarks]: https://github.com/alexandrnikitin/bloom-filter-scala/tree/master/benchmarks/src/main/scala
