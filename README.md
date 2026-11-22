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

| Module | What you get | Complexity |
|--------|-------------|------------|
| **Grid** | `Grid` / `MutableGrid` addressed by `Position(x, y)` or `grid[x, y]`, `Direction` and `Direction8`, `toGrid()` parsing, `neighbors`, `ray`, rotation, wrap-around | $O(1)$ cell access |
| **Math** | `gcd`, `lcm`, `Long.pow` (binary exponentiation), modular `pow`, `sqr` | $O(\log n)$ for pow |
| **Disjoint Set Union** | `find`, `union`, `connected`, `count`, `isolate` with path compression + union by rank | $O(\alpha(n))$ amortized |
| **Heap** | Binary min-heap with any `Comparator`: `push`, `pop`, `peek`, `replace`, `…OrNull` variants, `toHeap()` in $O(n)$ | $O(\log n)$ push/pop |
| **Graph Search** | `bfs` and `dijkstra` over any state with a `neighbors` lambda, from one start or several, giving the distance to every reachable state | $O(V + E)$ and $O((V + E) \log (V + E))$ |
| **Segment Tree** | Range queries (`sum`, `min`, `max`, `gcd`, …) and point updates with `tree[i, j]` syntax | $O(\log n)$ query/update |
| **String Algorithms** | KMP prefix function, `allIndicesOf` for pattern matching, generic sequence support | $O(n + m)$ |
| **Binary Search** | `partitionPoint` (Int and Long), `lowerBound`, `upperBound` with multiple overloads | $O(\log n)$ |
| **Ternary Search** | Find the maximum of a unimodal function on an interval | $O(\log((r-l)/\varepsilon))$ |
| **Map Counters** | `MutableMap<T, Int>.increment` / `decrement` for frequency maps | $O(1)$ |

For full API details and parameter descriptions, see the [KDoc reference](https://dmitrynekrasov.github.io/kodvent/).

## Installation

Add the dependency to your project:

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.dmitrynekrasov:kodvent:0.3.1")
            }
        }
    }
}
```

### Gradle (Kotlin DSL, JVM only)

```kotlin
dependencies {
    implementation("io.github.dmitrynekrasov:kodvent:0.3.1")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.dmitrynekrasov:kodvent:0.3.1'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.dmitrynekrasov</groupId>
    <artifactId>kodvent-jvm</artifactId>
    <version>0.3.1</version>
</dependency>
```

## Usage

### 2D Grids

The `kodvent.grid` package is not released yet, it will be available in the next version.

```kotlin
// input: List<String>, the puzzle input lines; input[y][x] is the cell at Position(x, y)
val grid = input.toGrid()

// Before: bounds checks by hand in every grid puzzle
var count = 0
for (k in (i - 1)..(i + 1)) for (l in (j - 1)..(j + 1))
    if ((k != i || l != j) && k in input.indices && l in input[k].indices && input[k][l] == '@') count++

// After
val count = grid.neighbors8(position).count { grid[it] == '@' }
```

```kotlin
// Walk from '^', turning right at every '#', until a step leaves the grid
var position = grid.positionOf('^')
var direction = Direction.UP
val visited = mutableSetOf(position)
while (true) {
    val ahead = position + direction
    when (grid.getOrNull(ahead)) {
        null -> break                              // the step leaves the grid
        '#' -> direction = direction.turnRight()
        else -> { position = ahead; visited += ahead }
    }
}

// Digits, mutation, and a snapshot of the grid as a map key
// levels: List<String>, lines of digits such as "5483"
val energy = levels.toMutableGrid { it.digitToInt() }
energy[0, 0] += 1                             // the same cell as energy[Position(0, 0)]
val seen = mutableMapOf(energy.toGrid() to 0)

// The first non-empty cell along a direction, diagonals included
val firstSeen = grid.ray(position, Direction8.UP_RIGHT).firstOrNull { grid[it] != '.' }

// Rotation, and a pattern that repeats endlessly in every direction
val turned = grid.rotatedClockwise()
val repeated = grid[Position(-1, 1000).wrap(grid)]
```

`x` counts columns from the left and `y` counts lines from the top, so a puzzle's `x,y` pair goes into
`Position(x, y)` unchanged, and `grid[x, y]` is `grid[Position(x, y)]`. Lines must have the same length and
nothing is trimmed, because a space can be a cell; a whole text is
`text.lines().dropLastWhile { it.isEmpty() }.toGrid()`.

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
// Greatest common divisor and the least common multiple
val g = gcd(48, 18)   // 6
val l = lcm(12, 18)   // 36

// Simplify a fraction
val divisor = gcd(48, 180)
val simplified = "${48 / divisor}/${180 / divisor}"  // "4/15"

// Binary exponentiation with infix syntax
val result = 2L pow 10L  // 1024

// Modular exponentiation — useful for number theory problems
val modulo = 1000000007L
val big = 2L.pow(100L, modulo)  // 976371285

// Squared distance without temporary variables
val distanceSquared = dx.sqr() + dy.sqr()
```

### Graph Algorithms with Disjoint Set Union

```kotlin
// Detect cycles in a graph
val dsu = DisjointSetUnion(4)

dsu.union(0, 1)
dsu.union(1, 2)
dsu.union(2, 3)

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
val mst = edges.sortedBy { it.weight }.filter { dsuMst.union(it.from, it.to) }
```

### Priority Queue with Heap

`Heap` is not released yet, it will be available in the next version.

```kotlin
// The smallest element is on top
val heap = Heap<Int>()
heap.push(5)
heap.push(1)
heap.push(3)

heap.peek()  // 1
heap.pop()   // 1
heap.pop()   // 3

// A max-heap, and a heap ordered by a property
val largestFirst = Heap<Int>(reverseOrder())
val byPriority = Heap<Task>(compareBy { it.priority })

// From a collection in O(n)
val numbers = listOf(7, 2, 9, 4).toHeap()
```

```kotlin
// Dijkstra's algorithm on a grid where entering a cell costs its digit
// grid: Grid<Int>, e.g. input.toGrid { it.digitToInt() }
fun lowestTotalRisk(grid: Grid<Int>, start: Position, end: Position): Int {
    val dist = MutableGrid(grid.width, grid.height) { Int.MAX_VALUE }
    val heap = Heap<Pair<Int, Position>>(compareBy { it.first })
    dist[start] = 0
    heap.push(0 to start)
    while (heap.isNotEmpty()) {
        val (d, p) = heap.pop()
        if (d > dist[p]) continue // stale entry: p was reached cheaper after this push
        if (p == end) return d
        for (q in grid.neighbors(p)) {
            val nd = d + grid[q]
            if (nd < dist[q]) {
                dist[q] = nd
                heap.push(nd to q)
            }
        }
    }
    error("$end is unreachable from $start")
}
```

The entries are `(distance, position)` pairs because the order of an element must not change while it is in
the heap: a comparator that reads a distance array the search keeps lowering breaks that rule.

`dijkstra` from `kodvent.graph`, below, runs this search for any kind of state.

### Shortest Paths with BFS and Dijkstra

The `kodvent.graph` package is not released yet, it will be available in the next version.

```kotlin
// Fewest steps through a maze: a state is a Position, and its neighbors are the open cells next to it
// maze: Grid<Char>, e.g. input.toGrid()
val steps = bfs(start) { p -> maze.neighbors(p).filter { maze[it] != '#' } }
val answer = steps[end] ?: -1  // a cell that cannot be reached is not in the map

// Flood fill: the region of equal letters around a cell
val region = bfs(position) { p -> garden.neighbors(p).filter { garden[it] == garden[p] } }.keys

// From several starts at once: the distance to the nearest one
val toNearestExit = bfs(starts = maze.positionsOf('E')) { p -> maze.neighbors(p).filter { maze[it] != '#' } }
```

```kotlin
// Dijkstra over states that carry more than a cell: at most three steps in a row in one direction
// grid: Grid<Int>, where entering a cell costs its digit
data class Cart(val position: Position, val direction: Direction, val run: Int)

val origin = Position(0, 0)
val starts = listOf(Cart(origin, Direction.RIGHT, 0), Cart(origin, Direction.DOWN, 0))
val costs = dijkstra(starts = starts) { (position, direction, run) ->
    listOf(direction, direction.turnLeft(), direction.turnRight())
        .map { Cart(position + it, it, if (it == direction) run + 1 else 1) }
        .filter { it.position in grid && it.run <= 3 }
        .map { it to grid[it.position] }  // next to cost
}
val cheapest = costs.filterKeys { it.position == grid.positions.last() }.values.min()
```

Both functions return the distance to every state the search reaches. A state is any value whose `equals` and
`hashCode` do not change: a `Position`, a data class, a pair. Name the argument `starts =` when you pass a
collection of starts: with a lambda that does not give the type of its parameter, the call fits both overloads.

### Range Queries with Segment Tree

```kotlin
// Range sum queries with bracket syntax
val numbers = listOf(1, 3, 5, 7, 9, 11)
val sumTree = SegmentTree(numbers, Int::plus)

sumTree[1, 4]           // 24 (sum of 3 + 5 + 7 + 9)
sumTree[2] = 15         // point update
sumTree[2] += 10        // compound assignment

// Works with any associative operation
val minTree = SegmentTree(data, ::minOf)
val maxTree = SegmentTree(data, ::maxOf)
val gcdTree = SegmentTree(values, ::gcd)

// Safe queries for bounds you can't guarantee
sumTree.getOrNull(0, 100)         // null (out of bounds)
sumTree.getOrDefault(0, 100, 0)   // 0
```

### String Matching with KMP Algorithm

```kotlin
// Find all occurrences of a pattern in text
val indices = "ababcababa".allIndicesOf("aba")  // [0, 5, 7]

// Finds overlapping patterns
"aaaa".allIndicesOf("aa")  // [0, 1, 2]

// Period detection via prefix function
val pi = "abababab".prefixFunction()
val period = "abababab".length - pi.last()  // 2

// Generic prefix function works with any sequence
val numPi = prefixFunction(numbers.size, numbers::get)
```

### Binary Search with Partition Point

```kotlin
// Binary search on answer: maximize minimum distance
// LeetCode 1552: Magnetic Force Between Two Balls
fun canPlaceBalls(positions: IntArray, m: Int, minDistance: Int): Boolean {
    var ballsPlaced = 1
    var lastPosition = positions[0]
    for (i in 1..positions.lastIndex) {
        if (positions[i] - lastPosition >= minDistance) {
            ballsPlaced++
            lastPosition = positions[i]
        }
    }
    return ballsPlaced >= m
}

val positions = intArrayOf(1, 2, 3, 4, 7)
val m = 3
positions.sort()

val partitionPoint = partitionPoint(0, positions.last() + 1) { distance ->
    canPlaceBalls(positions, m, distance)
}
val maxMinDistance = partitionPoint - 1  // 3
```

### Counting Occurrences with Lower and Upper Bound

```kotlin
val list = listOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 11)

val lower = list.lowerBound(9)  // 7 - index of first 9
val upper = list.upperBound(9)  // 11 - index after last 9
val count = upper - lower       // 4

// For elements that don't exist, lower == upper
list.upperBound(8) - list.lowerBound(8)  // 0
```

### Finding the Maximum of an Unimodal Function with Ternary Search

```kotlin
// A rectangle has a fixed perimeter P = 40.
// Side lengths: x and (P/2 - x). Area = x * (20 - x).
// Find x that maximizes area, with x in [0, 20].
val halfP = 40.0 / 2

val x = ternarySearch(0.0, halfP) { x -> x * (halfP - x) }
// x = 10.0 (maximum area when the rectangle is a square)
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

The project includes comprehensive tests for all functions. Test reports are generated at `build/reports/tests/test/index.html`.

### Changing the Public API

`./gradlew build` compares the public API with the dumps in the `api` directory. After an intended change, update the dumps and commit them:

```bash
./gradlew updateKotlinAbi
```

## License

This project is licensed under the Apache License 2.0 – see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Here's how to get started:

1. Check [open issues](https://github.com/DmitryNekrasov/kodvent/issues) for things to work on
2. Fork the repository and create a feature branch
3. Make sure `./gradlew build` passes (the project uses `allWarningsAsErrors` and `explicitApi`)
4. Submit a Pull Request with a clear description of your changes
