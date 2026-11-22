/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.graph

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SearchTest {

    private class Graph(val size: Int, val edges: List<List<Pair<Int, Int>>>) {
        fun steps(vertex: Int): List<Int> = edges[vertex].map { it.first }

        override fun toString(): String = edges.withIndex().joinToString("; ") { (from, out) -> "$from -> $out" }
    }

    private fun randomGraph(random: Random, maxCost: Int): Graph {
        val size = random.nextInt(1, 9)
        val edgeCount = random.nextInt(0, 2 * size * size + 1)
        val edges = List(size) { mutableListOf<Pair<Int, Int>>() }
        repeat(edgeCount) {
            edges[random.nextInt(size)] += random.nextInt(size) to random.nextInt(maxCost + 1)
        }
        return Graph(size, edges)
    }

    private fun floydWarshall(graph: Graph, starts: List<Int>, unitCosts: Boolean): Map<Int, Int> {
        val infinity = Int.MAX_VALUE / 2
        val cost = Array(graph.size) { from -> IntArray(graph.size) { to -> if (from == to) 0 else infinity } }
        for ((from, out) in graph.edges.withIndex()) {
            for ((to, step) in out) cost[from][to] = minOf(cost[from][to], if (unitCosts) 1 else step)
        }
        for (k in 0..<graph.size) for (i in 0..<graph.size) for (j in 0..<graph.size) {
            cost[i][j] = minOf(cost[i][j], cost[i][k] + cost[k][j])
        }
        return (0..<graph.size)
            .associateWith { to -> starts.minOfOrNull { cost[it][to] } ?: infinity }
            .filterValues { it < infinity }
    }

    @Test
    fun `bfs puts the start at 0 and leaves out what it cannot reach`() {
        val edges = mapOf("a" to listOf("b"), "b" to listOf("c"), "c" to listOf("a"), "d" to listOf("a"))

        val steps = bfs("a") { edges.getValue(it) }

        assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), steps)
    }

    @Test
    fun `bfs from a state without neighbors returns only the start`() {
        assertEquals(mapOf(7 to 0), bfs(7) { emptyList() })
    }

    @Test
    fun `bfs agrees with Floyd-Warshall on random graphs`() {
        for (seed in 0..<300) {
            val random = Random(seed)
            val graph = randomGraph(random, maxCost = 0)
            val start = random.nextInt(graph.size)

            assertEquals(floydWarshall(graph, listOf(start), unitCosts = true), bfs(start) { graph.steps(it) })
        }
    }

    @Test
    fun `bfs from several starts agrees with Floyd-Warshall on random graphs`() {
        for (seed in 0..<300) {
            val random = Random(seed)
            val graph = randomGraph(random, maxCost = 0)
            val starts = List(random.nextInt(0, 4)) { random.nextInt(graph.size) }

            assertEquals(
                floydWarshall(graph, starts, unitCosts = true),
                bfs(starts = starts) { graph.steps(it) },
            )
        }
    }

    @Test
    fun `bfs counts a repeated start once`() {
        val calls = mutableMapOf<Int, Int>()

        val steps = bfs(starts = listOf(1, 1, 2)) { state ->
            calls[state] = (calls[state] ?: 0) + 1
            listOf(state + 1).filter { it <= 3 }
        }

        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 1), steps)
        assertEquals(mapOf(1 to 1, 2 to 1, 3 to 1), calls)
    }

    @Test
    fun `bfs from no starts returns an empty map`() {
        assertTrue(bfs(starts = emptyList<Int>()) { listOf(it + 1) }.isEmpty())
    }

    @Test
    fun `bfs calls neighbors once for every reached state`() {
        val calls = mutableMapOf<Int, Int>()

        val steps = bfs(0) { state ->
            calls[state] = (calls[state] ?: 0) + 1
            listOf((state + 1) % 10, (state + 3) % 10, state, (state + 7) % 10)
        }

        assertEquals((0..<10).toSet(), steps.keys)
        assertEquals((0..<10).associateWith { 1 }, calls)
    }

    @Test
    fun `bfs takes a list as a single state when the argument is named start`() {
        val steps = bfs(start = listOf(0, 0)) { (x, y) -> listOf(listOf(x + 1, y), listOf(x, y + 1)).filter { it.max() <= 2 } }

        assertEquals(4, steps[listOf(2, 2)])
        assertEquals(9, steps.size)
    }

    @Test
    fun `bfs walks a long chain`() {
        val length = 100_000

        val steps = bfs(0) { if (it < length) listOf(it + 1) else emptyList() }

        assertEquals(length, steps[length])
        assertEquals(length + 1, steps.size)
    }

    @Test
    fun `dijkstra puts the start at 0 and leaves out what it cannot reach`() {
        val edges = mapOf(
            "a" to listOf("b" to 5, "c" to 1),
            "b" to listOf("d" to 1),
            "c" to listOf("b" to 1),
            "d" to emptyList(),
            "e" to listOf("a" to 1),
        )

        val costs = dijkstra("a") { edges.getValue(it) }

        assertEquals(mapOf("a" to 0, "b" to 2, "c" to 1, "d" to 3), costs)
    }

    @Test
    fun `dijkstra agrees with Floyd-Warshall on random graphs`() {
        for (seed in 0..<300) {
            val random = Random(seed)
            val graph = randomGraph(random, maxCost = 9)
            val start = random.nextInt(graph.size)

            assertEquals(floydWarshall(graph, listOf(start), unitCosts = false), dijkstra(start) { graph.edges[it] })
        }
    }

    @Test
    fun `dijkstra from several starts agrees with Floyd-Warshall on random graphs`() {
        for (seed in 0..<300) {
            val random = Random(seed)
            val graph = randomGraph(random, maxCost = 9)
            val starts = List(random.nextInt(0, 4)) { random.nextInt(graph.size) }

            assertEquals(
                floydWarshall(graph, starts, unitCosts = false),
                dijkstra(starts = starts) { graph.edges[it] },
            )
        }
    }

    @Test
    fun `dijkstra accepts zero costs`() {
        val edges = listOf(listOf(1 to 0, 2 to 4), listOf(2 to 0, 0 to 0), listOf(3 to 0), emptyList())

        assertEquals(mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0), dijkstra(0) { edges[it] })
    }

    @Test
    fun `dijkstra calls neighbors once for every reached state even when a cost is lowered`() {
        val edges = listOf(listOf(1 to 10, 2 to 1), listOf(3 to 1), listOf(1 to 1, 3 to 20), listOf(1 to 0))
        val calls = mutableMapOf<Int, Int>()

        val costs = dijkstra(0) { vertex ->
            calls[vertex] = (calls[vertex] ?: 0) + 1
            edges[vertex]
        }

        assertEquals(mapOf(0 to 0, 1 to 2, 2 to 1, 3 to 3), costs)
        assertEquals(mapOf(0 to 1, 1 to 1, 2 to 1, 3 to 1), calls)
    }

    @Test
    fun `dijkstra counts a repeated start once`() {
        val calls = mutableMapOf<Int, Int>()

        val costs = dijkstra(starts = listOf(0, 0, 1)) { vertex ->
            calls[vertex] = (calls[vertex] ?: 0) + 1
            listOf(vertex + 1 to 5).filter { it.first <= 2 }
        }

        assertEquals(mapOf(0 to 0, 1 to 0, 2 to 5), costs)
        assertEquals(mapOf(0 to 1, 1 to 1, 2 to 1), calls)
    }

    @Test
    fun `dijkstra from no starts returns an empty map`() {
        assertTrue(dijkstra(starts = emptyList<Int>()) { listOf(it + 1 to 1) }.isEmpty())
    }

    @Test
    fun `dijkstra rejects a negative cost`() {
        assertFailsWith<IllegalArgumentException> { dijkstra(0) { listOf(1 to -1) } }
    }

    @Test
    fun `dijkstra reaches the largest Int and throws beyond it`() {
        val farthest = listOf(listOf(1 to Int.MAX_VALUE), emptyList())
        val beyond = listOf(listOf(1 to Int.MAX_VALUE), listOf(2 to 1), emptyList())

        assertEquals(Int.MAX_VALUE, dijkstra(0) { farthest[it] }[1])
        assertFailsWith<ArithmeticException> { dijkstra(0) { beyond[it] } }
    }

    @Test
    fun `dijkstra rejects a negative step back into a finished state`() {
        assertFailsWith<IllegalArgumentException> { dijkstra(0) { if (it == 0) listOf(1 to 1) else listOf(0 to -1) } }
    }

    @Test
    fun `dijkstra ignores an overflowing step to a state that already costs less`() {
        assertEquals(mapOf(0 to 0, 1 to 2_000_000_000), dijkstra(0) { listOf(1 - it to 2_000_000_000) })
    }

    @Test
    fun `dijkstra ignores an overflowing step to a state that a cheaper path reaches later`() {
        val edges = listOf(listOf(1 to 1, 2 to 2), listOf(3 to Int.MAX_VALUE), listOf(3 to 1), emptyList())

        assertEquals(mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3), dijkstra(0) { edges[it] })
    }

    @Test
    fun `dijkstra takes a list as a single state when the argument is named start`() {
        val costs = dijkstra(start = listOf(0, 0)) { (x, y) ->
            listOf(listOf(x + 1, y) to 2, listOf(x, y + 1) to 3).filter { (next, _) -> next.max() <= 2 }
        }

        assertEquals(10, costs[listOf(2, 2)])
        assertEquals(9, costs.size)
    }

    @Test
    fun `dijkstra walks a long chain`() {
        val length = 100_000

        val costs = dijkstra(0) { if (it < length) listOf(it + 1 to 2) else emptyList() }

        assertEquals(2 * length, costs[length])
        assertEquals(length + 1, costs.size)
    }
}
