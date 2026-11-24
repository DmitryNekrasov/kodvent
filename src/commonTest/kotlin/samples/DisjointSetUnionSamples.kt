/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.DisjointSetUnion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisjointSetUnionSamples {

    @Test
    fun basicUsage() {
        val dsu = DisjointSetUnion(5)
        assertTrue(dsu.union(0, 1))
        assertTrue(dsu.union(1, 2))
        assertFalse(dsu.union(0, 2)) // already in one set

        assertTrue(dsu.connected(0, 2))
        assertFalse(dsu.connected(0, 3))
        assertEquals(3, dsu.count) // {0, 1, 2}, {3}, {4}
    }

    @Test
    fun kruskalMinimumSpanningTree() {
        data class Edge(val from: Int, val to: Int, val weight: Int)

        val edges = listOf(
            Edge(0, 1, 4), Edge(0, 2, 2), Edge(1, 2, 1), Edge(1, 3, 5), Edge(2, 3, 8),
            Edge(2, 4, 10), Edge(3, 4, 2), Edge(3, 5, 6), Edge(4, 5, 3),
        )

        // Kruskal's algorithm: take the edges from the lightest, skipping one that would close a cycle
        val dsu = DisjointSetUnion(6)
        val tree = edges.sortedBy { it.weight }.filter { dsu.union(it.from, it.to) }

        assertEquals(13, tree.sumOf { it.weight })
        assertEquals(1, dsu.count)
    }

    @Test
    fun findingRepresentatives() {
        val dsu = DisjointSetUnion(6)
        dsu.union(0, 1)
        dsu.union(1, 2)
        dsu.union(3, 4)

        assertEquals(dsu.find(0), dsu.find(2))
        assertEquals(5, dsu.find(5))

        val sets = (0..<6).groupBy { dsu.find(it) }.values
        assertEquals(listOf(listOf(0, 1, 2), listOf(3, 4), listOf(5)), sets.toList())
    }

    @Test
    fun isolateUsage() {
        val dsu = DisjointSetUnion(5)
        dsu.union(0, 1)
        dsu.union(1, 2)
        assertEquals(3, dsu.count) // {0, 1, 2}, {3}, {4}

        dsu.isolate(1)

        assertEquals(4, dsu.count) // {0, 2}, {1}, {3}, {4}
        assertFalse(dsu.connected(0, 1))
        assertTrue(dsu.connected(0, 2))
    }
}
