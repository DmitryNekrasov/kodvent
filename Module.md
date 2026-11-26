# Module kodvent

Kodvent is a Kotlin Multiplatform library for [Advent of Code](https://adventofcode.com/) and competitive programming. It has grids, breadth-first search and Dijkstra's algorithm, a heap, a disjoint set union and a segment tree, binary and ternary search, and functions for number theory and string matching. All of it is common Kotlin with no dependencies, so it works on JVM, JS, Wasm and Native.

A solution of a puzzle usually starts with code that is not about the puzzle: bounds checks around a map, a table of 4 offsets, a queue for a search, a priority queue, which common Kotlin does not have. With kodvent this code is already written and tested, and the solution keeps only the logic of the puzzle. The sections below describe each package with examples. For grids, [bfs][kodvent.graph.bfs] and [dijkstra][kodvent.graph.dijkstra] they also show real solutions from Advent of Code, as they were written without the library and as they look with it.

The library is published on Maven Central as `io.github.dmitrynekrasov:kodvent`. The [README](https://github.com/DmitryNekrasov/kodvent#installation) shows how to add it to a project.

## Grids

The [kodvent.grid] package is for puzzles with a map. A map is a [Grid][kodvent.grid.Grid] of cells, and a cell is addressed by a [Position][kodvent.grid.Position]. `x` is the column counted from the left and `y` is the input line counted from the top, so `input[y][x]` is the cell at `Position(x, y)`, and a pair `x,y` from a puzzle goes into `Position(x, y)` unchanged. [toGrid][kodvent.grid.toGrid] makes a grid from the input lines.

```kotlin
import kodvent.grid.*

// input: List<String>, the lines of the puzzle input
val grid = input.toGrid()

val start = grid.positionOf('S')  // the first 'S' in reading order
val walls = grid.count { it == '#' }

// The neighbors inside the grid: up, right, down and left, then the same with the diagonals
val open = grid.neighbors(start).filter { grid[it] != '#' }
val around = grid.neighbors8(start).count { grid[it] == '#' }
```

[Direction][kodvent.grid.Direction] has the 4 orthogonal directions and [Direction8][kodvent.grid.Direction8] has all 8. A direction is added to a position, so a walk needs no table of offsets. A step that leaves the grid is found with `getOrNull`, or with `position in grid`.

```kotlin
// Walk from '^', turning right at every '#', until a step leaves the grid
var position = grid.positionOf('^')
var direction = Direction.UP
val visited = mutableSetOf(position)
while (true) {
    val ahead = position + direction
    when (grid.getOrNull(ahead)) {
        null -> break  // the step leaves the grid
        '#' -> direction = direction.turnRight()
        else -> { position = ahead; visited += ahead }
    }
}
```

The cells of a [MutableGrid][kodvent.grid.MutableGrid] can be overwritten. Grids are compared by their cells, so a snapshot of a grid can be a key of a map. There are also functions for a ray in a direction, for rotation, and for a map that repeats in every direction.

```kotlin
// Digits, mutation, and a snapshot of the grid as a map key
// levels: List<String>, lines of digits such as "5483"
val energy = levels.toMutableGrid { it.digitToInt() }
energy[0, 0] += 1  // the same cell as energy[Position(0, 0)]
val seen = mutableMapOf(energy.toGrid() to 0)

// The first non-empty cell along a direction, diagonals included
val firstSeen = grid.ray(position, Direction8.UP_RIGHT).firstOrNull { grid[it] != '.' }

// Rotation, and a pattern that repeats endlessly in every direction
val turned = grid.rotatedClockwise()
val repeated = grid[Position(-1, 1000).wrap(grid)]
```

The lines must have the same length and nothing is trimmed, because a space can be a cell. A whole text is parsed as `text.lines().dropLastWhile { it.isEmpty() }.toGrid()`.

### A real puzzle: Printing Department

[Printing Department](https://adventofcode.com/2025/day/4) is day 4 of 2025. The map shows rolls of paper as `@`. A roll can be taken if fewer than 4 of the 8 cells around it hold a roll. Part 1 counts such rolls. In part 2 they are taken away, after that other rolls can be taken, and this repeats while there is something to take. The answer is the number of rolls taken.

Without kodvent ([the original solution](https://github.com/DmitryNekrasov/AoC-2025/blob/247c7eeb1d5402221498fb397ce649527d125446/src/main/kotlin/day04/Day04.kt)):

```kotlin
class Day04 {
    fun List<CharArray>.countAround(i: Int, j: Int): Int {
        var count = 0
        for (k in (i - 1)..(i + 1)) {
            for (l in (j - 1)..(j + 1)) {
                if (k in 0..<size && l in 0..<first().size && this[k][l] == '@') {
                    count++
                }
            }
        }
        return count
    }

    fun part1(grid: List<CharArray>): Int {
        val n = grid.size
        val m = grid.first().size
        var result = 0
        for (i in 0..<n) {
            for (j in 0..<m) {
                if (grid[i][j] == '@') {
                    if (grid.countAround(i, j) <= 4) {
                        result++
                    }
                }
            }
        }
        return result
    }

    fun part2(grid: List<CharArray>): Int {
        val n = grid.size
        val m = grid.first().size
        var result = 0
        var wasRemoved: Boolean
        do {
            wasRemoved = false
            for (i in 0..<n) {
                for (j in 0..<m) {
                    if (grid[i][j] == '@') {
                        if (grid.countAround(i, j) <= 4) {
                            grid[i][j] = '.'
                            wasRemoved = true
                            result++
                        }
                    }
                }
            }
        } while (wasRemoved)
        return result
    }
}
```

With kodvent:

```kotlin
import kodvent.grid.*

class Day04 {
    fun Grid<Char>.countAround(position: Position): Int = neighbors8(position).count { this[it] == '@' }

    fun part1(grid: Grid<Char>): Int = grid.positionsOf('@').count { grid.countAround(it) < 4 }

    fun part2(grid: MutableGrid<Char>): Int {
        var result = 0
        var wasRemoved: Boolean
        do {
            wasRemoved = false
            for (position in grid.positionsOf('@')) {
                if (grid.countAround(position) < 4) {
                    grid[position] = '.'
                    wasRemoved = true
                    result++
                }
            }
        } while (wasRemoved)
        return result
    }
}
```

The loop over the 3 x 3 square with its bounds test is now [neighbors8][kodvent.grid.neighbors8], and the 2 nested loops that look for `@` are [positionsOf][kodvent.grid.Grid.positionsOf]. The original counts the roll itself, and this is why it compares with `<= 4`. Of 49 lines of code 19 are left.

## Graph search

The [kodvent.graph] package has [bfs][kodvent.graph.bfs] and [dijkstra][kodvent.graph.dijkstra]. They search over states of any kind: a `Position`, a data class, a pair, a number, anything with stable `equals` and `hashCode`. An array does not fit, because it is compared by identity. There is no graph to build. A lambda returns the neighbors of a state, and for `dijkstra` it returns every neighbor with the cost of the step. The result is a map from every state that can be reached to its distance from the start, so a state that cannot be reached is not in the map.

```kotlin
import kodvent.graph.*
import kodvent.grid.*

// maze: Grid<Char>, for example input.toGrid()

// Fewest steps through a maze
val steps = bfs(start) { p -> maze.neighbors(p).filter { maze[it] != '#' } }
val answer = steps[end] ?: -1  // an unreachable cell is not in the map

// Flood fill
val region = bfs(position) { p -> garden.neighbors(p).filter { garden[it] == garden[p] } }.keys

// The distance to the nearest of several starts
val exits = maze.positionsOf('E')
val toNearestExit = bfs(starts = exits) { p -> maze.neighbors(p).filter { maze[it] != '#' } }

// Steps with costs: edges[vertex] is a list of pairs, the next vertex to the cost of the step
val costs = dijkstra(0) { vertex -> edges[vertex] }
```

With a lambda that does not give the type of its parameter, a collection of starts fits both overloads, so name the argument `starts =`.

A state does not have to be a cell. In [Factory](https://adventofcode.com/2025/day/10), day 10 of 2025, a machine has lights and buttons, and a button toggles some of the lights. Part 1 asks for the fewest presses that turn the lights from all off into a given pattern. A state is the set of lights that are on, kept as a bitmask. Without kodvent ([the original solution](https://github.com/DmitryNekrasov/AoC-2025/blob/247c7eeb1d5402221498fb397ce649527d125446/src/main/kotlin/day10/Day10.kt#L23-L40)):

```kotlin
fun bfs(target: Int, transitions: List<Int>): Int {
    val start = 0
    val queue: Queue<Pair<Int, Int>> = LinkedList()
    queue.add(start to 0)
    val visited = hashSetOf(start)
    while (queue.isNotEmpty()) {
        val (from, distance) = queue.poll()
        if (from == target) return distance
        for (transition in transitions) {
            val to = from xor transition
            if (to !in visited) {
                visited.add(to)
                queue.add(to to distance + 1)
            }
        }
    }
    error("Should not reach here")
}
```

With kodvent:

```kotlin
fun fewestPresses(target: Int, transitions: List<Int>): Int =
    bfs(0) { lights -> transitions.map { lights xor it } }.getValue(target)
```

### Breadth-first search in a real puzzle: RAM Run

[RAM Run](https://adventofcode.com/2024/day/18) is day 18 of 2024. Bytes fall on a memory area of 71 x 71 cells, one after another. Each byte is given as `x,y`, and a cell that was hit cannot be entered. Part 1 is the fewest steps from the top left corner to the bottom right one after the first 1024 bytes. Part 2 is the first byte that makes the bottom right corner unreachable.

Without kodvent ([the original solution](https://github.com/DmitryNekrasov/AoC-2024/blob/47770b639b6daec5ff9a85e0cb34ca6fabbe3f82/src/day18/Day18.kt)):

```kotlin
import java.util.LinkedList
import java.util.Queue

fun List<String>.parse() = map { it.split(",").map(String::toInt).let { (x, y) -> x to y } }

fun distance(n: Int, limit: Int, points: List<Pair<Int, Int>>): Int {
    val grid = Array(n) { CharArray(n) { '.' } }
    for (i in 0..<limit) {
        val (x, y) = points[i]
        grid[y][x] = '#'
    }

    val visited = Array(n) { BooleanArray(n) }
    val queue: Queue<List<Int>> = LinkedList()
    queue.offer(listOf(0, 0, 0))
    visited[0][0] = true
    while (queue.isNotEmpty()) {
        val (x, y, distance) = queue.poll()
        if (x == n - 1 && y == n - 1) return distance
        for ((dx, dy) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
            val nextX = x + dx
            val nextY = y + dy
            if (nextX in 0..<n && nextY in 0..<n && grid[nextY][nextX] == '.' && !visited[nextY][nextX]) {
                visited[nextY][nextX] = true
                queue.offer(listOf(nextX, nextY, distance + 1))
            }
        }
    }

    return -1
}

fun part1(n: Int, limit: Int, points: List<Pair<Int, Int>>): Int {
    return distance(n, limit, points)
}

fun part2(n: Int, points: List<Pair<Int, Int>>): String {
    fun binSearch(left: Int = 0, right: Int = points.size): Int {
        if (left >= right) return right
        val mid = left + (right - left) / 2
        return if (distance(n, mid, points) == -1) binSearch(left, mid) else binSearch(mid + 1, right)
    }

    return points[binSearch() - 1].let { (x, y) -> "$x,$y" }
}
```

With kodvent:

```kotlin
import kodvent.graph.*
import kodvent.grid.*
import kodvent.search.*

fun List<String>.parse() = map { it.split(",").map(String::toInt).let { (x, y) -> Position(x, y) } }

fun distance(n: Int, limit: Int, points: List<Position>): Int {
    val grid = MutableGrid(n, n) { '.' }
    for (point in points.take(limit)) grid[point] = '#'

    val steps = bfs(Position(0, 0)) { position -> grid.neighbors(position).filter { grid[it] == '.' } }
    return steps[Position(n - 1, n - 1)] ?: -1
}

fun part1(n: Int, limit: Int, points: List<Position>): Int = distance(n, limit, points)

fun part2(n: Int, points: List<Position>): String {
    val blocked = partitionPoint(0, points.size) { limit -> distance(n, limit, points) != -1 }
    return points[blocked - 1].let { (x, y) -> "$x,$y" }
}
```

The queue, the `visited` array, the table of offsets and the bounds test are now one call of [bfs][kodvent.graph.bfs]. The recursive binary search is [partitionPoint][kodvent.search.partitionPoint]. A pair `x,y` goes into a `Position` as it is, so there is no `grid[y][x]` to get wrong. Of 36 lines of code 12 are left, and without `java.util` the solution compiles for every platform.

### Dijkstra's algorithm in a real puzzle: Reindeer Maze

[Reindeer Maze](https://adventofcode.com/2024/day/16) is day 16 of 2024. A reindeer stands on `S`, looks east and has to reach `E`. A step forward costs 1 point and a turn by 90 degrees costs 1000 points. Part 1 is the lowest score. Part 2 is the number of cells that lie on at least one path with this score.

Without kodvent ([the original solution](https://github.com/DmitryNekrasov/AoC-2024/blob/47770b639b6daec5ff9a85e0cb34ca6fabbe3f82/src/day16/Day16.kt)):

```kotlin
import java.util.LinkedList
import java.util.PriorityQueue
import java.util.Queue

const val N = 0
const val E = 1
const val S = 2
const val W = 3

fun List<String>.get(c: Char): Pair<Int, Int> {
    for (i in indices) {
        for (j in first().indices) {
            if (this[i][j] == c) {
                return i to j
            }
        }
    }
    throw RuntimeException("Should not reach here")
}

fun List<String>.id(i: Int, j: Int, direction: Int) = (i * first().length + j) * 4 + direction

fun List<String>.ij(id: Int) = id / 4 / first().length to id / 4 % first().length

fun List<String>.generateGraph(startI: Int, startJ: Int): HashMap<Int, MutableSet<Pair<Int, Int>>> {
    val graph = HashMap<Int, MutableSet<Pair<Int, Int>>>()

    val queue: Queue<Pair<Int, Int>> = LinkedList()
    queue.offer(startI to startJ)

    val visited = Array(size) { BooleanArray(first().length) }

    while (queue.isNotEmpty()) {
        val (i, j) = queue.poll()
        visited[i][j] = true

        for (k in N..W) {
            graph.getOrPut(id(i, j, k)) { mutableSetOf() } += id(i, j, (k + 1) % 4) to 1000
            graph.getOrPut(id(i, j, (k + 1) % 4)) { mutableSetOf() } += id(i, j, k) to 1000
        }

        if (this[i - 1][j] != '#') {
            graph.getOrPut(id(i, j, N)) { mutableSetOf() } += id(i - 1, j, N) to 1
            graph.getOrPut(id(i - 1, j, S)) { mutableSetOf() } += id(i, j, S) to 1
            if (!visited[i - 1][j]) {
                queue.offer(i - 1 to j)
            }
        }
        if (this[i + 1][j] != '#') {
            graph.getOrPut(id(i, j, S)) { mutableSetOf() } += id(i + 1, j, S) to 1
            graph.getOrPut(id(i + 1, j, N)) { mutableSetOf() } += id(i, j, N) to 1
            if (!visited[i + 1][j]) {
                queue.offer(i + 1 to j)
            }
        }
        if (this[i][j - 1] != '#') {
            graph.getOrPut(id(i, j, W)) { mutableSetOf() } += id(i, j - 1, W) to 1
            graph.getOrPut(id(i, j - 1, E)) { mutableSetOf() } += id(i, j, E) to 1
            if (!visited[i][j - 1]) {
                queue.offer(i to j - 1)
            }
        }
        if (this[i][j + 1] != '#') {
            graph.getOrPut(id(i, j, E)) { mutableSetOf() } += id(i, j + 1, E) to 1
            graph.getOrPut(id(i, j + 1, W)) { mutableSetOf() } += id(i, j, W) to 1
            if (!visited[i][j + 1]) {
                queue.offer(i to j + 1)
            }
        }
    }

    return graph
}

const val INF = 1_000_000_000

fun solve(grid: List<String>): Pair<Int, Int> {
    val (startI, startJ) = grid.get('S')
    val (endI, endJ) = grid.get('E')
    val graph = grid.generateGraph(startI, startJ)

    val n = grid.size
    val m = grid.first().length
    val vertexNumber = 4 * m * (n + 1) + 5
    for (k in N..W) {
        graph.getOrPut(grid.id(endI, endJ, k)) { mutableSetOf() } += vertexNumber - 1 to 0
    }

    val d = IntArray(vertexNumber) { INF }
    val p = Array(vertexNumber) { mutableListOf<Int>() }
    val startId = grid.id(startI, startJ, E)
    d[startId] = 0
    val queue = PriorityQueue<Int> { i, j -> d[i] compareTo d[j] }
    queue.offer(startId)
    while (queue.isNotEmpty()) {
        val from = queue.poll()
        graph[from]?.also { neighbors ->
            for ((to, cost) in neighbors) {
                if (d[from] + cost < d[to]) {
                    d[to] = d[from] + cost
                    p[to].clear()
                    p[to] += from
                    queue.offer(to)
                } else if (d[from] + cost == d[to]) {
                    p[to] += from
                }
            }
        }
    }

    fun bfs(start: Int): Int {
        val visited = mutableSetOf<Int>()
        val queue: Queue<Int> = LinkedList()
        queue.offer(start)
        while (queue.isNotEmpty()) {
            val from = queue.poll()
            for (to in p[from]) {
                if (to !in visited) {
                    visited += to
                    queue.offer(to)
                }
            }
        }
        return visited.map { grid.ij(it) }.toSet().size
    }

    return d[vertexNumber - 1] to bfs(vertexNumber - 1)
}
```

With kodvent:

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

A state is a position with a direction, so the data class replaces the numbering of vertices, and the lambda replaces the graph that the original builds in advance. For part 2 the original keeps the lists of predecessors and walks them back from the end. [dijkstra][kodvent.graph.dijkstra] returns only costs, so the solution searches a second time, backwards from the 4 states on `E`. A state lies on a best path if its cost from the start plus its cost to the end is the lowest score. Of 108 lines of code 16 are left.

## Data structures

The [kodvent.datastructures] package has a heap, a disjoint set union, a segment tree and an enumerator.

[Heap][kodvent.datastructures.Heap] is a binary heap with any comparator, and the smallest element by the comparator is on top. A search for shortest paths does not need it directly, because `dijkstra` already runs on it.

```kotlin
import kodvent.datastructures.*

val heap = Heap<Int>()
heap.push(5)
heap.push(1)
heap.push(3)

heap.peek()  // 1
heap.pop()  // 1
heap.pop()  // 3

// A max-heap, and a heap ordered by a property
val largestFirst = Heap<Int>(reverseOrder())
val byPriority = Heap<Task>(compareBy { it.priority })

// From a collection in O(n)
val numbers = listOf(7, 2, 9, 4).toHeap()
```

[DisjointSetUnion][kodvent.datastructures.DisjointSetUnion] keeps the elements `0..<size` in disjoint sets. `union` merges 2 sets and returns `false` if the 2 elements are already in one set, which is the test for a cycle and the main step of Kruskal's algorithm. `isolate` moves an element into a set of its own.

```kotlin
val dsu = DisjointSetUnion(4)

dsu.union(0, 1)
dsu.union(1, 2)
dsu.union(2, 3)

val hasCycle = !dsu.union(0, 3)  // true: 0 and 3 are already connected

// Kruskal's algorithm for a minimum spanning tree
data class Edge(val from: Int, val to: Int, val weight: Int)

val edges = listOf(Edge(0, 1, 4), Edge(0, 2, 2), Edge(1, 2, 1), Edge(1, 3, 5))
val forest = DisjointSetUnion(4)
val tree = edges.sortedBy { it.weight }.filter { forest.union(it.from, it.to) }
```

[SegmentTree][kodvent.datastructures.SegmentTree] answers a query on a range of a list and updates one element, both in O(log n), for any associative operation. Both ends of a range are inclusive.

```kotlin
val numbers = listOf(1, 3, 5, 7, 9, 11)
val sums = SegmentTree(numbers, Int::plus)

sums[1, 4]  // 24, the sum of 3, 5, 7 and 9
sums[2] = 15  // a point update
sums[2] += 10  // a compound assignment

// Any associative operation
val minimums = SegmentTree(numbers, ::minOf)
val divisors = SegmentTree(numbers, ::gcd)

// Queries with bounds that may be outside
sums.getOrNull(0, 100)  // null
sums.getOrDefault(0, 100, 0)  // 0
```

[Enumerator][kodvent.datastructures.Enumerator] numbers distinct elements from 0 in the order in which it first sees them. It turns names into indices of an array or into elements of a `DisjointSetUnion`.

```kotlin
val id = Enumerator<String>()

id["you"]  // 0
id["out"]  // 1
id["you"]  // 0, the same number as before

// Names as elements of a disjoint set union: a cable brings at most 2 new names
val cables = listOf("kh" to "tc", "qp" to "kh", "de" to "cg")
val computer = Enumerator<String>()
val networks = DisjointSetUnion(2 * cables.size)
for ((a, b) in cables) networks.union(computer[a], computer[b])

networks.connected(computer["tc"], computer["qp"])  // true
```

## Binary and ternary search

The [kodvent.search] package has binary search in 3 forms and ternary search.

[partitionPoint][kodvent.search.partitionPoint] is a binary search on the answer. The predicate must be `true` up to some index and `false` from it on, and the function returns the first index with `false`, or the end of the range if there is none. RAM Run above uses it on a real puzzle.

```kotlin
import kodvent.search.*

val sorted = intArrayOf(5, 10, 15, 20, 25, 30)
partitionPoint(0, sorted.size) { sorted[it] < 22 }  // 4, the index of 25

// The floor of a square root: the last x with x * x <= n is one before the partition point
val n = 12_345_678_912_345L
partitionPoint(0L, 10_000_000L) { x -> x * x <= n } - 1  // 3513641
```

With 2 integer literals as bounds both the `Int` and the `Long` overload fit, so give the lambda parameter a type, as in `partitionPoint(0, 100) { i: Int -> i * i < 50 }`.

[lowerBound][kodvent.search.lowerBound] and [upperBound][kodvent.search.upperBound] are for sorted lists. They return the index of the first element that is not less than the given one, and the index of the first element that is greater. If there is no such element, the result is the size of the list.

```kotlin
val list = listOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 11)

val lower = list.lowerBound(9)  // 7, the index of the first 9
val upper = list.upperBound(9)  // 11, the index after the last 9
val count = upper - lower  // 4

// For an element that is not in the list both are the same index
list.upperBound(8) - list.lowerBound(8)  // 0
```

[ternarySearch][kodvent.search.ternarySearch] finds the point where a unimodal function has its maximum.

```kotlin
// The area of a rectangle with the perimeter 40 and the side x is x * (20 - x)
val x = ternarySearch(0.0, 20.0) { x -> x * (20.0 - x) }
// x is within about 1e-7 of 10.0: the largest rectangle is a square
```

## Math

The [kodvent.math] package has functions for number theory.

```kotlin
import kodvent.math.*

val g = gcd(48, 18)  // 6
val l = lcm(12, 18)  // 36

// The least common multiple of several numbers
// cycleLengths: List<Long>, because the result may not fit in an Int
val period = cycleLengths.reduce(::lcm)

// Binary exponentiation, also as an infix call
val result = 2L pow 10L  // 1024

// Modular exponentiation
val modulo = 1_000_000_007L
val big = 2L.pow(100L, modulo)  // 976371285

// A squared distance
// dx, dy: Long, because their squares may not fit in an Int
val distanceSquared = dx.sqr() + dy.sqr()
```

## Strings

The [kodvent.strings] package has the prefix function of the Knuth-Morris-Pratt algorithm and a search for every occurrence of a pattern.

```kotlin
import kodvent.strings.*

val indices = "ababcababa".allIndicesOf("aba")  // [0, 5, 7]

// Occurrences may overlap
"aaaa".allIndicesOf("aa")  // [0, 1, 2]

// The period of a string from its prefix function
val pi = "abababab".prefixFunction()
val period = "abababab".length - pi.last()  // 2

// The prefix function of any sequence
// numbers: List<Int>
val numbersPi = prefixFunction(numbers.size, numbers::get)
```

## Counting

The [kodvent.collections] package has [increment][kodvent.collections.increment] and [decrement][kodvent.collections.decrement] for a map that counts elements. `decrement` removes a key when its count reaches 0.

```kotlin
import kodvent.collections.*

val frequencies = mutableMapOf<Char, Int>()
"mississippi".forEach { frequencies.increment(it) }
// frequencies is {m=1, i=4, s=4, p=2}

frequencies.decrement('m')
// frequencies is {i=4, s=4, p=2}
```

# Package kodvent.collections

[increment] and [decrement] for a `MutableMap` that counts elements.

# Package kodvent.datastructures

A [Heap] with any comparator, a [DisjointSetUnion] with path compression and union by rank, a [SegmentTree] for range queries with point updates, and an [Enumerator] that numbers distinct elements from 0.

# Package kodvent.graph

Shortest paths over states of any kind, where a lambda returns the neighbors of a state. [bfs] counts steps and [dijkstra] sums the costs of steps, from one start or from several.

# Package kodvent.grid

Rectangular grids for puzzle maps: a read-only [Grid] and a [MutableGrid], whose cells are addressed by a [Position], and the directions of a step, [Direction] and [Direction8]. [toGrid] parses input lines, and there are functions for neighbors, rays, rotation and wrap-around.

# Package kodvent.math

[gcd] and [lcm], [pow] by binary exponentiation, also by a modulo, and [sqr].

# Package kodvent.search

Binary search on the answer with [partitionPoint], [lowerBound] and [upperBound] for sorted lists, and [ternarySearch] for the maximum of a unimodal function.

# Package kodvent.strings

The prefix function of the Knuth-Morris-Pratt algorithm, [prefixFunction], and [allIndicesOf], which finds every occurrence of a pattern in a text.
