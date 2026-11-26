# 🌲 Kodvent: Kotlin toolkit for AoC and CP

[![JetBrains team project](https://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/Heapy/awesome-kotlin)
[![GitHub license](https://img.shields.io/badge/license-Apache%202.0-green.svg?style=flat)](https://github.com/DmitryNekrasov/kodvent/blob/main/LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.4.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/dmitrynekrasov/kodvent/build.yml)](https://github.com/DmitryNekrasov/kodvent/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dmitrynekrasov/kodvent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.dmitrynekrasov/kodvent)
[![KDoc link](https://img.shields.io/badge/API_reference-KDoc-blue)](https://dmitrynekrasov.github.io/kodvent/)

![kodvent](kodvent-logo.png)

A Kotlin Multiplatform utility library for Advent of Code challenges, featuring grids for puzzle maps, graph search over any kind of state, efficient data structures, algorithms, and convenient extension functions.

## Why Kodvent?

Every December, thousands of developers solve [Advent of Code](https://adventofcode.com/) puzzles. In Kotlin, that means writing the same bounds checks around a map, the same table of 4 offsets, the same queue for a breadth-first search, and the same priority queue for Dijkstra's algorithm – year after year. Kodvent gives you **battle-tested, zero-dependency implementations** so you can focus on the puzzle, not the plumbing.

This is [Reindeer Maze](https://adventofcode.com/2024/day/16), day 16 of 2024, solved with kodvent. A reindeer stands on `S`, looks east and has to reach `E`. A step forward costs 1 point and a turn by 90 degrees costs 1000 points. Part 1 is the lowest score, and part 2 is the number of cells that lie on at least one path with this score.

```kotlin
import kodvent.graph.*
import kodvent.grid.*

data class Reindeer(val position: Position, val direction: Direction)

fun solve(input: List<String>): Pair<Int, Int> {
    val maze = input.toGrid()
    val start = Reindeer(maze.positionOf('S'), Direction.RIGHT)
    val ends = Direction.entries.map { Reindeer(maze.positionOf('E'), it) }

    fun Reindeer.moves(next: Position) = listOfNotNull(
        copy(direction = direction.turnLeft()) to 1000,
        copy(direction = direction.turnRight()) to 1000,
        (copy(position = next) to 1).takeIf { maze[next] != '#' },
    )

    val fromStart = dijkstra(start) { it.moves(it.position + it.direction) }
    val toEnd = dijkstra(starts = ends) { it.moves(it.position - it.direction) }

    val best = ends.minOf { fromStart.getValue(it) }
    val onBestPath = fromStart.keys.filter { fromStart.getValue(it) + toEnd.getValue(it) == best }
    return best to onBestPath.map { it.position }.toSet().size
}
```

These are 16 lines of code, and the solution without the library takes 108. The [documentation](https://dmitrynekrasov.github.io/kodvent/) shows both versions, for this puzzle and for 2 more:

| Puzzle | Uses | Without kodvent | With kodvent |
|--------|------|-----------------|--------------|
| [Printing Department](https://adventofcode.com/2025/day/4), day 4 of 2025 | grid | [49 lines](https://github.com/DmitryNekrasov/AoC-2025/blob/247c7eeb1d5402221498fb397ce649527d125446/src/main/kotlin/day04/Day04.kt) | 19 lines |
| [RAM Run](https://adventofcode.com/2024/day/18), day 18 of 2024 | grid, `bfs`, `partitionPoint` | [36 lines](https://github.com/DmitryNekrasov/AoC-2024/blob/47770b639b6daec5ff9a85e0cb34ca6fabbe3f82/src/day18/Day18.kt) | 12 lines |
| [Reindeer Maze](https://adventofcode.com/2024/day/16), day 16 of 2024 | grid, `dijkstra` | [108 lines](https://github.com/DmitryNekrasov/AoC-2024/blob/47770b639b6daec5ff9a85e0cb34ca6fabbe3f82/src/day16/Day16.kt) | 16 lines |

## Features

| Feature | Package | What you get | Complexity |
|---------|---------|-------------|------------|
| **Grid** | `kodvent.grid` | `Grid` / `MutableGrid` addressed by `Position(x, y)` or `grid[x, y]`, `Direction` and `Direction8`, `toGrid()` parsing, `neighbors`, `ray`, rotation, wrap-around | $O(1)$ cell access |
| **Graph Search** | `kodvent.graph` | `bfs` and `dijkstra` over any state with a `neighbors` lambda, from one start or several, giving the distance to every reachable state | $O(V + E)$ and $O((V + E) \log (V + E))$ |
| **Heap** | `kodvent.datastructures` | Binary min-heap with any `Comparator`: `push`, `pop`, `peek`, `replace`, `…OrNull` variants, `toHeap()` in $O(n)$ | $O(\log n)$ push/pop |
| **Disjoint Set Union** | `kodvent.datastructures` | `find`, `union`, `connected`, `count` with path compression + union by rank, `isolate` in $O(n)$ | $O(\alpha(n))$ amortized |
| **Segment Tree** | `kodvent.datastructures` | Range queries (`sum`, `min`, `max`, `gcd`, …) and point updates with `tree[i, j]` syntax | $O(\log n)$ query/update |
| **Enumerator** | `kodvent.datastructures` | Numbers distinct values of any type from 0 with `ids[value]` and counts them with `size`, for array indices and `DisjointSetUnion` elements | $O(1)$ amortized |
| **Binary Search** | `kodvent.search` | `partitionPoint` (Int and Long), `lowerBound`, `upperBound` with multiple overloads | $O(\log n)$ |
| **Ternary Search** | `kodvent.search` | Find the maximum of a unimodal function on an interval | $O(\log((r-l)/\varepsilon))$ |
| **Math** | `kodvent.math` | `gcd`, `lcm`, `Long.pow` (binary exponentiation), modular `pow`, `sqr` | $O(\log n)$ multiplications for pow |
| **String Algorithms** | `kodvent.strings` | KMP prefix function, `allIndicesOf` for pattern matching, generic sequence support | $O(n + m)$ |
| **Map Counters** | `kodvent.collections` | `MutableMap<T, Int>.increment` / `decrement` for frequency maps | $O(1)$ |

`Enumerator` is not released yet, it will be available in the next version.

For full API details and parameter descriptions, see the [KDoc reference](https://dmitrynekrasov.github.io/kodvent/).

## Installation

Kodvent 0.4.0 requires Kotlin 2.4.0 or newer.

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

```bash
./gradlew build  # every target of the host, the tests and the check of the public API
./gradlew jvmTest  # JVM only, fast
./gradlew updateKotlinAbi  # after an intended change of the public API, then commit api/
./gradlew dokkaGeneratePublicationHtml  # after a change of KDoc, of a sample or of Module.md
```

## License

This project is licensed under the Apache License 2.0 – see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Check [open issues](https://github.com/DmitryNekrasov/kodvent/issues) for things to work on, and make sure `./gradlew build` and `./gradlew dokkaGeneratePublicationHtml` pass before you submit a Pull Request: CI runs both.
