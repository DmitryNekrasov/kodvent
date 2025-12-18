# 🌲 Kodvent: Kotlin toolkit for AoC and CP

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/dmitrynekrasov/kodvent/build.yml)](https://github.com/DmitryNekrasov/kodvent/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dmitrynekrasov/kodvent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.dmitrynekrasov/kodvent)
[![KDoc link](https://img.shields.io/badge/API_reference-KDoc-blue)](https://dmitrynekrasov.github.io/kodvent/)

![kodvent](kodvent-logo.png)

A Kotlin utility library for Advent of Code challenges, featuring efficient data structures, algorithms, and convenient extension functions.

## Features

### Map Counter Extensions

The library provides convenient extension functions for `MutableMap<T, Int>` to easily maintain counter/frequency maps:

- **`increment(key: T)`** - Increments the count for a key, initializing to 1 if the key doesn't exist
- **`decrement(key: T): Boolean`** - Decrements the count for a key, removing it when count reaches 0

### Math Functions

Common mathematical functions for number theory and algorithmic problems:

- **`gcd(a: Int, b: Int): Int`** - Computes the greatest common divisor (GCD) of two integers using the Euclidean algorithm
- **`lcm(a: Int, b: Int): Int`** - Computes the least common multiple (LCM) of two integers
- Both functions are also available for `Long` parameters

#### Power Functions

Efficient exponentiation using binary exponentiation (O(log n) time complexity):

- **`Long.pow(power: Long): Long`** - Raises a Long to the given power using fast binary exponentiation
  - Supports infix notation: `2L pow 10L`
  - Warning: Does not check for overflow with large values
- **`Long.pow(power: Long, modulo: Long): Long`** - Modular exponentiation that prevents overflow
  - Useful for computing large powers modulo a number
  - Common in number theory problems

#### Square Functions

Convenient extension functions for computing the square of a number:

- **`Int.sqr(): Int`** - Returns the square of an Int value
- **`Long.sqr(): Long`** - Returns the square of a Long value
- **`Double.sqr(): Double`** - Returns the square of a Double value

### Disjoint Set Union (Union-Find)

An efficient data structure for managing disjoint sets with near-constant time operations:

- **`DisjointSetUnion(size: Int)`** - Creates a DSU with `size` elements, each initially in its own set
- **`find(x: Int): Int`** - Finds the representative (root) of the set containing element `x`
- **`union(x: Int, y: Int): Boolean`** - Merges the sets containing `x` and `y`, returns `true` if merged
- **`connected(x: Int, y: Int): Boolean`** - Checks if `x` and `y` are in the same set
- **`count: Int`** - The number of disjoint sets
- **`isolate(x: Int)`** - Resets element `x` to be in its own singleton set

The implementation uses path compression and union by rank optimizations for optimal performance.

### Segment Tree

An efficient data structure for answering range queries and performing point updates on arrays:

- **`SegmentTree(source: List<T>, operation: (T, T) -> T)`** - Creates a segment tree from a list with an associative binary operation
  - Common operations: `Int::plus` (sum), `::minOf` (minimum), `::maxOf` (maximum), `::gcd` (GCD)
  - Construction time: O(n)
- **`get(start: Int, end: Int): T`** - Queries the result over range [start, end] (inclusive)
  - Supports bracket notation: `tree[2, 5]`
  - Time complexity: O(log n)
- **`get(index: Int): T`** - Queries a single element at the given index
  - Supports bracket notation: `tree[3]`
- **`getOrNull(start: Int, end: Int): T?`** - Safe range query that returns null for invalid indices
- **`getOrDefault(start: Int, end: Int, defaultValue: T): T`** - Safe range query with default value
- **`set(index: Int, value: T)`** - Updates the element at the given index
  - Supports assignment syntax: `tree[3] = 10`
  - Time complexity: O(log n)
  - Compound assignment operators work automatically: `tree[3] += 5`, `tree[2] *= 2`

Space complexity: O(n) where n is the size of the source list.

### String Algorithms

Efficient string matching and analysis functions based on the prefix function and KMP algorithm:

- **`CharSequence.prefixFunction(): IntArray`** - Computes the prefix function for a character sequence
  - The prefix function π[i] represents the length of the longest proper prefix that is also a suffix
  - Fundamental component of the Knuth-Morris-Pratt algorithm
  - Time complexity: O(n)
- **`prefixFunction(length: Int, at: (Int) -> T): IntArray`** - Generic version that works with any type of sequence
  - Works with any elements that can be accessed by index and compared for equality
  - Useful for pattern matching in non-string sequences
- **`CharSequence.allIndicesOf(needle: CharSequence, delimiter: Char = '#'): List<Int>`** - Finds all occurrences of a substring
  - Uses the KMP algorithm for efficient string matching
  - Time complexity: O(n + m) where n is text length and m is pattern length
  - Returns all starting indices where the pattern occurs

## Installation

Add the dependency to your project:

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.dmitrynekrasov:kodvent:0.2.1")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.dmitrynekrasov:kodvent:0.2.1'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.dmitrynekrasov</groupId>
    <artifactId>kodvent</artifactId>
    <version>0.2.1</version>
</dependency>
```

## Usage

### Counting Elements

```kotlin
val frequencies = mutableMapOf<Char, Int>()

// Count character occurrences
"hello world".forEach { char ->
    frequencies.increment(char)
}

// frequencies now contains: {h=1, e=1, l=3, o=2, w=1, r=1, d=1}
```

### Mathematical Computations

```kotlin
import kodvent.math.gcd
import kodvent.math.lcm

// Find the greatest common divisor
val result1 = gcd(48, 18)  // 6

// Simplify a fraction using GCD
val numerator = 48
val denominator = 180
val divisor = gcd(numerator, denominator)
val simplified = "${ numerator / divisor }/${ denominator / divisor }"  // "4/15"

// Find the least common multiple
val result2 = lcm(12, 18)  // 36

// Solve a scheduling problem
// Two buses arrive every 12 and 18 minutes
val nextSimultaneousArrival = lcm(12, 18)  // 36 minutes
```

### Power and Modular Exponentiation

```kotlin
import kodvent.math.pow

// Basic exponentiation using binary exponentiation
val result1 = 2L.pow(10L)  // 1024
val result2 = 5L.pow(3L)   // 125

// Using infix notation for cleaner syntax
val result3 = 2L pow 10L   // 1024
val area = 5L pow 2L       // 25

// Modular exponentiation to prevent overflow
// Useful for large computations
val modulo = 1000000007L
val result4 = 2L.pow(100L, modulo)  // 976371285

// Find the last digit of a large power
val lastDigit = 7L.pow(100L, 10L)  // 1 (last digit of 7^100)

// Number theory: verify Fermat's Little Theorem
// For prime p: a^(p-1) ≡ 1 (mod p)
val a = 5L
val p = 13L  // prime
val fermat = a.pow(p - 1, p)  // 1
```

### Computing Squares

```kotlin
import kodvent.math.sqr

val intSquare = 5.sqr()      // 25
val longSquare = 10L.sqr()   // 100
val doubleSquare = 3.5.sqr() // 12.25

val sideLength = 7
val area = sideLength.sqr()  // 49

val x1 = 3
val y1 = 4
val x2 = 6
val y2 = 8

val dx = x2 - x1
val dy = y2 - y1
val distanceSquared = dx.sqr() + dy.sqr()  // 25
```

### Graph Algorithms with Disjoint Set Union

```kotlin
import kodvent.datastructures.DisjointSetUnion

// Detect cycles in a graph
val dsu = DisjointSetUnion(4)

// Add edges: 0-1, 1-2, 2-3
dsu.union(0, 1)  // true - edge added
dsu.union(1, 2)  // true - edge added
dsu.union(2, 3)  // true - edge added

// Try to add edge 0-3 (would create a cycle)
val hasCycle = !dsu.union(0, 3)  // true - cycle detected!

// Kruskal's algorithm for Minimum Spanning Tree
data class Edge(val from: Int, val to: Int, val weight: Int)

val edges = listOf(
    Edge(0, 1, 4),
    Edge(0, 2, 2),
    Edge(1, 2, 1),
    Edge(1, 3, 5)
)

val dsuMst = DisjointSetUnion(4)
val mst = edges.sortedBy { it.weight }
    .filter { dsuMst.union(it.from, it.to) }

// mst contains edges with minimum total weight
```

### Range Queries with Segment Tree

```kotlin
import kodvent.datastructures.SegmentTree
import kodvent.math.gcd

// Range sum queries
val numbers = listOf(1, 3, 5, 7, 9, 11)
val sumTree = SegmentTree(numbers, Int::plus)

// Query sum of range [1, 4]: 3 + 5 + 7 + 9 = 24
val sum = sumTree[1, 4]  // 24

// Query entire array
val totalSum = sumTree[0, 5]  // 36

// Update a value and query again
sumTree[2] = 15  // Change 5 to 15
val newSum = sumTree[0, 5]  // 46

// Use compound assignment operators
sumTree[2] += 10  // 15 + 10 = 25
sumTree[3] -= 2   // 7 - 2 = 5

// Range minimum queries
val data = listOf(5, 2, 8, 1, 9, 3, 7, 4)
val minTree = SegmentTree(data, ::minOf)

val minRange = minTree[0, 3]  // min of [5, 2, 8, 1] = 1
val minAll = minTree[0, 7]    // min of entire array = 1

// Range maximum queries
val maxTree = SegmentTree(data, ::maxOf)
val maxRange = maxTree[4, 7]  // max of [9, 3, 7, 4] = 9

// Range GCD queries
val values = listOf(12, 18, 24, 30, 36, 42)
val gcdTree = SegmentTree(values, ::gcd)
val rangeGcd = gcdTree[0, 2]  // GCD of [12, 18, 24] = 6

// Safe queries with getOrNull and getOrDefault
val safeResult = sumTree.getOrNull(0, 100)    // null (out of bounds)
val withDefault = sumTree.getOrDefault(0, 100, 0)  // 0

// Stock price analysis
val prices = listOf(100, 120, 95, 110, 105, 130, 125)
val priceMinTree = SegmentTree(prices, ::minOf)
val priceMaxTree = SegmentTree(prices, ::maxOf)

// Find min/max in the first 3 days
val minPrice = priceMinTree[0, 2]  // 95
val maxPrice = priceMaxTree[0, 2]  // 120
val volatility = maxPrice - minPrice  // 25
```

### String Matching with KMP Algorithm

```kotlin
import kodvent.strings.allIndicesOf
import kodvent.strings.prefixFunction

// Find all occurrences of a pattern in text
val text = "ababcababa"
val pattern = "aba"
val indices = text.allIndicesOf(pattern)  // [0, 5, 7]

// Count pattern occurrences
val dna = "ATGCATGCATGC"
val dnaPattern = "ATGC"
val count = dna.allIndicesOf(dnaPattern).size  // 3

// Find overlapping patterns
val str = "aaaa"
val overlapping = str.allIndicesOf("aa")  // [0, 1, 2] - finds overlaps

// Compute prefix function for pattern analysis
val s = "abacaba"
val pi = s.prefixFunction()  // [0, 0, 1, 0, 1, 2, 3]
// pi[6] = 3 means "aba" is both prefix and suffix of "abacaba"

// Use prefix function for period detection
val periodic = "abababab"
val prefixArray = periodic.prefixFunction()
val period = periodic.length - prefixArray.last()  // 2
// The string has a repeating pattern of length 2

// Generic prefix function with custom types
val numbers = listOf(1, 2, 1, 2, 1, 2, 3)
val numPi = prefixFunction(numbers.size, numbers::get)
// Works with any comparable elements
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

The project includes comprehensive tests for all extension functions. Test reports are generated at `build/reports/tests/test/index.html`.

## Requirements

- Kotlin 2.2.20+
- JVM 23+

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
