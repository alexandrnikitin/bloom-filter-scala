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
libraryDependencies += "com.github.alexandrnikitin" %% "bloom-filter" % "0.3.0"
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

TBA
