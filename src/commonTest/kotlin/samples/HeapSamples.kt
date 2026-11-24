/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.Heap
import kodvent.datastructures.toHeap
import kodvent.grid.Grid
import kodvent.grid.MutableGrid
import kodvent.grid.Position
import kodvent.grid.neighbors
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertEquals(1, heap.peek())

        val sorted = mutableListOf<Int>()
        while (heap.isNotEmpty()) sorted += heap.pop()

        assertEquals(listOf(1, 3, 5), sorted)
        assertTrue(heap.isEmpty())
    }

    @Test
    fun customOrder() {
        val maxHeap = Heap<Int>(reverseOrder())
        maxHeap.push(1)
        maxHeap.push(5)
        maxHeap.push(3)
        assertEquals(5, maxHeap.pop())

        data class Task(val name: String, val priority: Int)

        val tasks = Heap<Task>(compareBy { it.priority })
        tasks.push(Task("write tests", 2))
        tasks.push(Task("fix the build", 1))
        assertEquals("fix the build", tasks.pop().name)
    }

    @Test
    fun buildFromCollection() {
        val heap = listOf(7, 2, 9, 4, 1, 8).toHeap()
        assertEquals(1, heap.peek())

        val smallest = List(3) { heap.pop() }
        assertEquals(listOf(1, 2, 4), smallest)
        assertEquals(3, heap.size)
    }

    @Test
    fun buildWithComparator() {
        val words = listOf("heap", "kotlin", "aoc").toHeap(compareByDescending { it.length })
        assertEquals("kotlin", words.pop())
    }

    @Test
    fun orNullVariants() {
        val heap = listOf(3, 1, 2).toHeap()
        assertEquals(1, heap.peekOrNull())
        assertEquals(listOf(1, 2, 3), generateSequence { heap.popOrNull() }.toList())

        assertNull(heap.peekOrNull())
        assertNull(heap.popOrNull())
    }

    @Test
    fun replaceKeepsTheLargest() {
        // The three largest: a min-heap of three, whose top is the smallest of them
        val largest = listOf(5, 1, 9).toHeap()
        for (number in listOf(4, 8, 2, 7)) {
            if (number > largest.peek()) largest.replace(number)
        }

        assertEquals(listOf(7, 8, 9), largest.toSortedList())
    }

    @Test
    fun sortedSnapshot() {
        val heap = listOf(5, 1, 4, 2, 3).toHeap()

        assertEquals(listOf(1, 2, 3, 4, 5), heap.toSortedList())
        assertEquals(5, heap.size)

        // Only the top has a sure place in toString: the rest follow in storage order
        assertTrue(heap.toString().startsWith("[1, "))
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
}
