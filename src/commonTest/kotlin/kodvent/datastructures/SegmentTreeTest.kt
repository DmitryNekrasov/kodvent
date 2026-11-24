/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SegmentTreeTest {

    // Concatenation is associative and not commutative: a result shows which leaves were combined, and in what order.
    private fun symbols(size: Int): List<String> = List(size) { ('0' + it).toString() }

    @Test
    fun `get combines every range of every size in order`() {
        for (size in 1..40) {
            val source = symbols(size)
            val tree = SegmentTree(source, String::plus)

            for (start in 0..<size) for (end in start..<size) {
                val expected = source.subList(start, end + 1).joinToString("")
                assertEquals(expected, tree[start, end])
                assertEquals(expected, tree.getOrNull(start, end))
                assertEquals(expected, tree.getOrDefault(start, end, "none"))
            }
        }
    }

    @Test
    fun `get of one index is the range of that index`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        for (i in 0..4) {
            assertEquals(tree[i, i], tree[i])
        }
    }

    @Test
    fun `set replaces one value and every range follows`() {
        for (size in 1..9) for (index in 0..<size) {
            val expected = symbols(size).toMutableList()
            val tree = SegmentTree(expected, String::plus)

            tree[index] = "X"
            expected[index] = "X"

            for (start in 0..<size) for (end in start..<size) {
                assertEquals(expected.subList(start, end + 1).joinToString(""), tree[start, end])
            }
        }
    }

    @Test
    fun `compound assignment reads one value and sets the result`() {
        val tree = SegmentTree(listOf(1, 2, 3), Int::plus)

        tree[1] += 10

        assertEquals(12, tree[1])
        assertEquals(16, tree[0, 2])
    }

    @Test
    fun `getOrNull and getOrDefault give null and the default for a range that is not inside`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        val empty = SegmentTree(emptyList(), Int::plus)

        for ((start, end) in listOf(-1 to 2, 0 to 5, 3 to 2, -5 to -1, 5 to 10)) {
            assertNull(tree.getOrNull(start, end))
            assertEquals(-1, tree.getOrDefault(start, end, -1))
        }
        assertNull(empty.getOrNull(0, 0))
        assertEquals(42, empty.getOrDefault(0, 0, 42))
    }

    @Test
    fun `get and set throw IllegalArgumentException on an empty tree and get on an inverted range`() {
        val empty = SegmentTree(emptyList(), Int::plus)
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)

        assertFailsWith<IllegalArgumentException> { empty[0, 0] }
        assertFailsWith<IllegalArgumentException> { empty[0] }
        assertFailsWith<IllegalArgumentException> { empty[0] = 42 }
        assertFailsWith<IllegalArgumentException> { tree[3, 1] }
    }

    @Test
    fun `get and set throw IndexOutOfBoundsException for an index outside`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)

        assertFailsWith<IndexOutOfBoundsException> { tree[-1, 2] }
        assertFailsWith<IndexOutOfBoundsException> { tree[0, 5] }
        for (outside in listOf(-1, 5, Int.MIN_VALUE, Int.MAX_VALUE)) {
            assertFailsWith<IndexOutOfBoundsException> { tree[outside] }
            assertFailsWith<IndexOutOfBoundsException> { tree[outside] = 10 }
        }
        assertEquals(15, tree[0, 4])
    }

    @Test
    fun `the tree keeps a copy of the source list`() {
        val source = mutableListOf(1, 2, 3)
        val tree = SegmentTree(source, Int::plus)

        source[0] = 10
        source += 4

        assertEquals(1, tree[0])
        assertEquals(6, tree[0, 2])
        assertNull(tree.getOrNull(0, 3))
    }

    @Test
    fun `construction is linear and get and set are logarithmic in calls of the operation`() {
        val levels = 10
        val size = 1 shl levels
        var calls = 0
        val tree = SegmentTree(List(size) { 1 }) { a, b ->
            calls++
            a + b
        }
        assertTrue(calls < 2 * size)

        // Without its first and its last element the whole range splits into the most nodes: two on almost every level
        calls = 0
        assertEquals(size - 2, tree[1, size - 2])
        assertTrue(calls <= 2 * levels)

        calls = 0
        tree[size - 1] = 5
        assertTrue(calls <= levels)
    }
}
