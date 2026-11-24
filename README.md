# 🌲 Kodvent: Kotlin toolkit for AoC and CP

[![JetBrains team project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/Heapy/awesome-kotlin)
[![GitHub license](https://img.shields.io/badge/license-Apache%202.0-green.svg?style=flat)](https://github.com/DmitryNekrasov/kodvent/blob/main/LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/dmitrynekrasov/kodvent/build.yml)](https://github.com/DmitryNekrasov/kodvent/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dmitrynekrasov/kodvent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.dmitrynekrasov/kodvent)
[![KDoc link](https://img.shields.io/badge/API_reference-KDoc-blue)](https://dmitrynekrasov.github.io/kodvent/)

![kodvent](kodvent-logo.png)

A Kotlin Multiplatform utility library for Advent of Code challenges, featuring efficient data structures, algorithms, and convenient extension functions.

## Why Kodvent?

Every December, thousands of developers solve [Advent of Code](https://adventofcode.com/) puzzles. In Kotlin, that means writing the same GCD function, the same DSU boilerplate, and the same binary search wrapper – year after year. Kodvent gives you **battle-tested, zero-dependency implementations** so you can focus on the puzzle, not the plumbing.

```kotlin
// cycleLengths: List<Long>, because their least common multiple may not fit in an Int

// Before: writing it from scratch every year
fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b
val answer = cycleLengths.reduce(::lcm)

// After: just import and solve
import kodvent.math.*
val answer = cycleLengths.reduce(::lcm)
```

- **Zero dependencies** — pure Kotlin, nothing extra to pull in
- **Multiplatform** — works on JVM, JS, Wasm, and Native
- **Idiomatic API** — operator overloads, infix functions, extension functions
- **Published to Maven Central** — one line in your `build.gradle.kts`

### Supported platforms

- **JVM** (target 1.8+)
- **JS** (IR, Node.js & Browser)
- **Wasm** (wasmJs, wasmWasi)
- **Native** — Linux (x64, arm64), macOS (arm64), Windows (mingwX64), iOS, watchOS, tvOS

## Features

| Feature | Package | What you get | Complexity |
|---------|---------|-------------|------------|
| **Grid** | `kodvent.grid` | `Grid` / `MutableGrid` addressed by `Position(x, y)` or `grid[x, y]`, `Direction` and `Direction8`, `toGrid()` parsing, `neighbors`, `ray`, rotation, wrap-around | $O(1)$ cell access |
| **Math** | `kodvent.math` | `gcd`, `lcm`, `Long.pow` (binary exponentiation), modular `pow`, `sqr` | $O(\log n)$ multiplications for pow |
| **Disjoint Set Union** | `kodvent.datastructures` | `find`, `union`, `connected`, `count` with path compression + union by rank, `isolate` in $O(n)$ | $O(\alpha(n))$ amortized |
| **Heap** | `kodvent.datastructures` | Binary min-heap with any `Comparator`: `push`, `pop`, `peek`, `replace`, `…OrNull` variants, `toHeap()` in $O(n)$ | $O(\log n)$ push/pop |
| **Graph Search** | `kodvent.graph` | `bfs` and `dijkstra` over any state with a `neighbors` lambda, from one start or several, giving the distance to every reachable state | $O(V + E)$ and $O((V + E) \log (V + E))$ |
| **Segment Tree** | `kodvent.datastructures` | Range queries (`sum`, `min`, `max`, `gcd`, …) and point updates with `tree[i, j]` syntax | $O(\log n)$ query/update |
| **String Algorithms** | `kodvent.strings` | KMP prefix function, `allIndicesOf` for pattern matching, generic sequence support | $O(n + m)$ |
| **Binary Search** | `kodvent.search` | `partitionPoint` (Int and Long), `lowerBound`, `upperBound` with multiple overloads | $O(\log n)$ |
| **Ternary Search** | `kodvent.search` | Find the maximum of a unimodal function on an interval | $O(\log((r-l)/\varepsilon))$ |
| **Map Counters** | `kodvent.collections` | `MutableMap<T, Int>.increment` / `decrement` for frequency maps | $O(1)$ |

For full API details and parameter descriptions, see the [KDoc reference](https://dmitrynekrasov.github.io/kodvent/).

## Installation

Add the dependency to your project:

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.dmitrynekrasov:kodvent:0.4.0")
            }
        }
    }
}
```

### Gradle (Kotlin DSL, JVM only)

```kotlin
dependencies {
    implementation("io.github.dmitrynekrasov:kodvent:0.4.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.dmitrynekrasov:kodvent:0.4.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.dmitrynekrasov</groupId>
    <artifactId>kodvent-jvm</artifactId>
    <version>0.4.0</version>
</dependency>
```

## Usage

```kotlin
// Grids: parse the input, find a marker, look around
val grid = input.toGrid()  // input: List<String>
val start = grid.positionOf('S')
val walls = grid.neighbors8(start).count { grid[it] == '#' }

// Shortest paths over any kind of state
val steps = bfs(start) { p -> grid.neighbors(p).filter { grid[it] != '#' } }
val costs = dijkstra(0) { vertex -> edges[vertex] }  // edges[vertex]: pairs of next vertex to cost

// Data structures
val heap = listOf(7, 2, 9, 4).toHeap()  // heap.pop() is 2
val dsu = DisjointSetUnion(4)  // dsu.union(0, 1) joins two sets
val sums = SegmentTree(listOf(1, 3, 5, 7), Int::plus)  // sums[1, 2] is 8

// Binary search on the answer, number theory, string matching
val root = partitionPoint(0, 100) { i: Int -> i * i < 50 }  // 8
val divisor = gcd(48, 18)  // 6
val power = 2L.pow(100, 1_000_000_007)  // 976371285
val found = "ababcababa".allIndicesOf("aba")  // [0, 5, 7]
```

The [documentation](https://dmitrynekrasov.github.io/kodvent/) describes every package with examples and shows real Advent of Code solutions as they look without kodvent and with it.

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew jvmTest  # JVM only, fast
./gradlew allTests  # every target of the host (the Apple simulator tests need the simulator runtimes)
```

The project includes comprehensive tests for all functions. Test reports are generated at `build/reports/tests/jvmTest/index.html` and `build/reports/tests/allTests/index.html`.

### Changing the Public API

`./gradlew build` compares the public API with the dumps in the `api` directory. After an intended change, update the dumps and commit them:

```bash
./gradlew updateKotlinAbi
```

### Changing the Documentation

`./gradlew build` does not run Dokka, but CI does. After a change of KDoc, of a sample or of `Module.md`, generate the documentation:

```bash
./gradlew dokkaGeneratePublicationHtml
```

It fails on a link that does not resolve and on a wrong `@sample`. The pages go to the `docs` directory, which is not tracked on `main`. The solutions that `Module.md` prints under "With kodvent" are also tests in `src/commonTest/kotlin/samples/PuzzleSamples.kt`, so change both together.

## License

This project is licensed under the Apache License 2.0 – see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Here's how to get started:

1. Check [open issues](https://github.com/DmitryNekrasov/kodvent/issues) for things to work on
2. Fork the repository and create a feature branch
3. Make sure `./gradlew build` and `./gradlew dokkaGeneratePublicationHtml` pass (the project uses `allWarningsAsErrors` and `explicitApi`)
4. Submit a Pull Request with a clear description of your changes
