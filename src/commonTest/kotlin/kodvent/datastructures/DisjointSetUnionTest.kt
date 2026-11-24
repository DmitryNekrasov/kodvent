/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisjointSetUnionTest {

    @Test
    fun `a new DSU has every element in its own set`() {
        for (size in listOf(0, 1, 5)) {
            val dsu = DisjointSetUnion(size)

            assertEquals(size, dsu.count)
            for (i in 0..<size) {
                assertEquals(i, dsu.find(i))
                for (j in 0..<size) assertEquals(i == j, dsu.connected(i, j))
            }
        }
    }

    @Test
    fun `a negative size throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> { DisjointSetUnion(-1) }
    }

    @Test
    fun `union of two elements already in one set returns false and keeps the count`() {
        // 3 is two links under the root 0 until a find compresses its path, so every pair gets a new DSU
        for ((x, y) in listOf(0 to 1, 1 to 0, 1 to 3, 3 to 1, 3 to 3, 0 to 0)) {
            val dsu = DisjointSetUnion(5)
            for ((a, b) in listOf(0 to 1, 2 to 3, 0 to 2)) dsu.union(a, b)

            assertFalse(dsu.union(x, y))

            assertEquals(2, dsu.count)
        }
    }

    @Test
    fun `union merges the whole sets when neither element is a root`() {
        val sets = listOf(listOf(0, 1, 2, 3), listOf(4, 5), listOf(6, 7))

        // The three pairs take the three branches of union by rank, and 3 is two links under its root
        for ((x, y) in listOf(5 to 7, 5 to 3, 3 to 5)) {
            val dsu = DisjointSetUnion(8)
            for ((a, b) in listOf(0 to 1, 2 to 3, 0 to 2, 4 to 5, 6 to 7)) dsu.union(a, b)

            assertTrue(dsu.union(x, y))

            assertEquals(2, dsu.count)
            val merged = sets.first { x in it } + sets.first { y in it }
            for (a in merged) for (b in merged) assertTrue(dsu.connected(a, b))
        }
    }

    @Test
    fun `find follows a path of any length to the root`() {
        val size = 64
        val dsu = DisjointSetUnion(size)

        // Joining sets of equal size builds the deepest tree that union by rank allows: 6 links for 64 elements
        var step = 1
        while (step < size) {
            for (i in 0..<size step 2 * step) dsu.union(i, i + step)
            step *= 2
        }

        assertTrue(dsu.connected(0, size - 1))
        val root = dsu.find(size - 1)
        for (i in 0..<size) assertEquals(root, dsu.find(i))
        assertEquals(1, dsu.count)
    }

    @Test
    fun `find stays shallow when 200000 elements join one by one`() {
        val size = 200_000
        val newFirst = DisjointSetUnion(size)
        val oldFirst = DisjointSetUnion(size)

        // With union by rank broken, one of the two orders chains every element or every second one, and the
        // recursive find overflows the stack: measured on the JVM, it holds 24,000 to 50,000 nested calls of find,
        // depending on what ran before.
        for (i in 1..<size) {
            newFirst.union(i, i - 1)
            oldFirst.union(i - 1, i)
        }

        for (dsu in listOf(newFirst, oldFirst)) {
            assertEquals(1, dsu.count)
            assertTrue(dsu.connected(0, size - 1))
        }
    }

    @Test
    fun `isolate takes one element out of each set and leaves the rest as it was`() {
        val dsu = DisjointSetUnion(10)
        for ((a, b) in listOf(0 to 1, 1 to 2, 2 to 3, 4 to 5, 5 to 6, 7 to 8)) dsu.union(a, b)

        dsu.isolate(1)
        dsu.isolate(5)
        dsu.isolate(8)

        val sets = listOf(listOf(0, 2, 3), listOf(1), listOf(4, 6), listOf(5), listOf(7), listOf(8), listOf(9))
        assertEquals(sets.size, dsu.count)
        for (a in 0..<10) for (b in 0..<10) {
            val together = sets.first { a in it } == sets.first { b in it }
            assertEquals(together, dsu.connected(a, b))
        }
    }

    @Test
    fun `isolate of an inner node leaves its children with the rest of the set`() {
        // The unions put leaf under inner, and inner and other under root. The second row mirrors the first:
        // there the root is the largest element of the set, and the leaf is smaller than the inner node.
        for ((root, other, inner, leaf) in listOf(listOf(0, 1, 2, 3), listOf(3, 2, 1, 0))) {
            val dsu = DisjointSetUnion(4)
            dsu.union(root, other)
            dsu.union(inner, leaf)
            dsu.union(root, inner)

            dsu.isolate(inner)

            assertEquals(2, dsu.count)
            assertFalse(dsu.connected(inner, leaf))
            assertTrue(dsu.connected(root, leaf))
            assertTrue(dsu.connected(other, leaf))
        }
    }

    @Test
    fun `isolate of the root keeps the others connected`() {
        val dsu = DisjointSetUnion(5)
        dsu.union(0, 1)
        dsu.union(1, 2)
        val root = dsu.find(0)
        val others = listOf(0, 1, 2) - root

        dsu.isolate(root)

        assertEquals(4, dsu.count)
        assertEquals(root, dsu.find(root))
        for (other in others) assertFalse(dsu.connected(root, other))
        assertTrue(dsu.connected(others[0], others[1]))
    }

    @Test
    fun `isolate of an element that is alone changes nothing`() {
        val dsu = DisjointSetUnion(3)
        dsu.union(0, 1)

        dsu.isolate(2)

        assertEquals(2, dsu.count)
        assertTrue(dsu.connected(0, 1))
        assertFalse(dsu.connected(1, 2))
    }

    @Test
    fun `isolate takes an element out of a set that an earlier isolate has rebuilt`() {
        // The first isolate rebuilds the set around a new root: the second one takes each of the three in turn
        for (second in 1..3) {
            val dsu = DisjointSetUnion(4)
            for ((a, b) in listOf(0 to 1, 1 to 2, 2 to 3)) dsu.union(a, b)

            dsu.isolate(0)
            dsu.isolate(second)

            val others = listOf(1, 2, 3) - second
            assertEquals(3, dsu.count)
            assertTrue(dsu.connected(others[0], others[1]))
            for (other in others) assertFalse(dsu.connected(second, other))
        }
    }

    @Test
    fun `every operation throws IndexOutOfBoundsException for an element outside`() {
        val dsu = DisjointSetUnion(5)

        for (outside in listOf(-1, 5, Int.MIN_VALUE, Int.MAX_VALUE)) {
            assertFailsWith<IndexOutOfBoundsException> { dsu.find(outside) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.union(outside, 0) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.union(0, outside) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.union(outside, outside) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.connected(outside, 0) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.connected(0, outside) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.connected(outside, outside) }
            assertFailsWith<IndexOutOfBoundsException> { dsu.isolate(outside) }
        }
    }
}
