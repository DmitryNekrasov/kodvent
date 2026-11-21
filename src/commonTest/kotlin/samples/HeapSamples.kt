/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.Heap
import kodvent.datastructures.toHeap
import kodvent.grid.Direction
import kodvent.grid.Grid
import kodvent.grid.MutableGrid
import kodvent.grid.Position
import kodvent.grid.neighbors
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeapSamples {

    @Test
    fun basicUsage() {
        val heap = Heap<Int>()
        heap.push(5)
        heap.push(1)
        heap.push(3)

        assertEquals(3, heap.size)
        assertFalse(heap.isEmpty())

        assertEquals(1, heap.peek())
        assertEquals(1, heap.pop())
        assertEquals(3, heap.pop())
        assertEquals(5, heap.pop())

        assertTrue(heap.isEmpty())
    }

    @Test
    fun maxHeap() {
        val heap = Heap<Int>(reverseOrder())
        heap.push(5)
        heap.push(1)
        heap.push(3)

        assertEquals(5, heap.pop())
        assertEquals(3, heap.pop())
        assertEquals(1, heap.pop())
    }

    @Test
    fun customOrder() {
        data class Task(val name: String, val priority: Int)

        val tasks = Heap<Task>(compareBy { it.priority })
        tasks.push(Task("write tests", 2))
        tasks.push(Task("fix the build", 1))
        tasks.push(Task("update the README", 3))

        assertEquals("fix the build", tasks.pop().name)
        assertEquals("write tests", tasks.pop().name)

        val words = Heap<String> { a, b -> a.length.compareTo(b.length) }
        words.push("kotlin")
        words.push("aoc")
        words.push("heap")

        assertEquals("aoc", words.pop())
        assertEquals("heap", words.pop())
    }

    @Test
    fun buildFromCollection() {
        val heap = listOf(7, 2, 9, 4, 1, 8).toHeap()
        assertEquals(1, heap.peek())

        val smallest = List(3) { heap.pop() }
        assertEquals(listOf(1, 2, 4), smallest)
        assertEquals(3, heap.size)

        val words = listOf("heap", "kotlin", "aoc").toHeap(compareByDescending { it.length })
        assertEquals("kotlin", words.pop())
    }

    @Test
    fun orNullVariants() {
        val heap = listOf(3, 1, 2).toHeap()
        assertEquals(1, heap.peekOrNull())

        val popped = mutableListOf<Int>()
        while (true) {
            val next = heap.popOrNull() ?: break
            popped += next
        }
        assertEquals(listOf(1, 2, 3), popped)

        assertNull(heap.peekOrNull())
        assertNull(heap.popOrNull())
    }

    @Test
    fun replaceMergesSortedLists() {
        val lists = listOf(listOf(1, 4, 7), emptyList(), listOf(2, 5), listOf(3, 6, 8, 9))

        val heap = lists.indices.filter { lists[it].isNotEmpty() }.map { it to 0 }
            .toHeap(compareBy { (list, index) -> lists[list][index] })

        val merged = mutableListOf<Int>()
        while (heap.isNotEmpty()) {
            val (list, index) = heap.peek()
            merged += lists[list][index]
            if (index + 1 < lists[list].size) heap.replace(list to index + 1) else heap.pop()
        }

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), merged)
    }

    @Test
    fun sortedSnapshot() {
        val heap = listOf(5, 1, 4, 2, 3).toHeap()

        assertEquals(listOf(1, 2, 3, 4, 5), heap.toSortedList())
        assertEquals(15, heap.toSortedList().sum())
        assertEquals("[1, 2, 3, 4, 5]", heap.toSortedList().toString())

        // Only the top has a sure place in toString: the rest follow in storage order
        assertTrue(heap.toString().startsWith("[1, "))

        assertEquals(5, heap.size)
        assertEquals(1, heap.peek())
    }

    @Test
    fun clearAndReuse() {
        val heap = listOf(3, 1, 2).toHeap()

        heap.clear()
        assertTrue(heap.isEmpty())

        heap.push(10)
        assertEquals(10, heap.peek())
    }

    @Test
    fun dijkstraOnGraph() {
        val edges = listOf(
            listOf(1 to 7, 2 to 9, 5 to 14),
            listOf(0 to 7, 2 to 10, 3 to 15),
            listOf(0 to 9, 1 to 10, 3 to 11, 5 to 2),
            listOf(1 to 15, 2 to 11, 4 to 6),
            listOf(3 to 6, 5 to 9),
            listOf(0 to 14, 2 to 2, 4 to 9),
        )
        val distance = IntArray(edges.size) { Int.MAX_VALUE }

        // (distance, vertex) entries: the place of an entry in the order never changes inside the heap
        val heap = Heap<Pair<Int, Int>>(compareBy { it.first })
        distance[0] = 0
        heap.push(0 to 0)
        while (heap.isNotEmpty()) {
            val (d, vertex) = heap.pop()

            // Stale entry: the vertex was pushed again with a shorter distance
            if (d > distance[vertex]) continue

            for ((next, weight) in edges[vertex]) {
                if (d + weight < distance[next]) {
                    distance[next] = d + weight
                    heap.push(d + weight to next)
                }
            }
        }

        assertEquals(listOf(0, 7, 9, 20, 20, 11), distance.toList())
    }

    @Test
    fun dijkstraOnGrid() {
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

        val grid = listOf(
            "1911",
            "1191",
            "9111",
        ).toGrid { it.digitToInt() }

        assertEquals(5, lowestTotalRisk(grid, Position(0, 0), grid.positions.last()))
    }

    @Test
    fun dijkstraOverStates() {
        // With a limit on steps in a row a cell can be worth reaching twice: the search runs over states
        data class State(val position: Position, val direction: Direction, val run: Int)

        fun cheapestRoute(grid: Grid<Int>, maxRun: Int): Int {
            val end = grid.positions.last()
            val start = State(Position(0, 0), Direction.RIGHT, 0)
            val best = mutableMapOf(start to 0)
            val heap = Heap<Pair<Int, State>>(compareBy { it.first })
            heap.push(0 to start)
            while (heap.isNotEmpty()) {
                val (cost, state) = heap.pop()
                if (cost > best.getValue(state)) continue
                if (state.position == end) return cost
                for (direction in listOf(state.direction, state.direction.turnLeft(), state.direction.turnRight())) {
                    val position = state.position + direction
                    val run = if (direction == state.direction) state.run + 1 else 1
                    if (position !in grid || run > maxRun) continue
                    val next = State(position, direction, run)
                    val nextCost = cost + grid[position]
                    if (nextCost < (best[next] ?: Int.MAX_VALUE)) {
                        best[next] = nextCost
                        heap.push(nextCost to next)
                    }
                }
            }
            error("$end is unreachable")
        }

        val grid = listOf(
            "1111",
            "9951",
            "9991",
        ).toGrid { it.digitToInt() }

        assertEquals(5, cheapestRoute(grid, maxRun = 3))

        // Three steps to the right in a row are not allowed, so the route goes through the 5
        assertEquals(9, cheapestRoute(grid, maxRun = 2))
    }
}
