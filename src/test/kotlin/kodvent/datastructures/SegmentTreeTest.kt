/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kodvent.math.gcd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SegmentTreeTest {

    @Test
    fun `getOrNull should return null for empty segment tree`() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertNull(tree.getOrNull(0, 0))
    }

    @Test
    fun `methods for getting the value should handle single element segment tree`() {
        val tree = SegmentTree(listOf(42), Int::plus)
        assertEquals(42, tree[0, 0])
        assertEquals(42, tree.getOrNull(0, 0))
        assertEquals(42, tree.getOrDefault(0, 0, 0))
    }

    @Test
    fun `SegmentTree should construct from multiple elements`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree[0, 4])
    }

    @Test
    fun `get should compute sum over full range`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5, 6, 7, 8), Int::plus)
        assertEquals(36, tree[0, 7])
    }

    @Test
    fun `get should compute sum over partial ranges`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5, 6, 7, 8), Int::plus)
        assertEquals(6, tree[0, 2])
        assertEquals(22, tree[3, 6])
        assertEquals(21, tree[5, 7])
        assertEquals(5, tree[1, 2])
    }

    @Test
    fun `get should compute sum of single element`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(1, tree[0, 0])
        assertEquals(3, tree[2, 2])
        assertEquals(5, tree[4, 4])
    }

    @Test
    fun `get should compute sum of adjacent elements`() {
        val tree = SegmentTree(listOf(10, 20, 30, 40, 50), Int::plus)
        assertEquals(30, tree[0, 1])
        assertEquals(50, tree[1, 2])
        assertEquals(90, tree[3, 4])
    }

    @Test
    fun `get should compute min over full range`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::minOf)
        assertEquals(1, tree[0, 7])
    }

    @Test
    fun `get should compute min over partial ranges`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::minOf)
        assertEquals(2, tree[0, 2])
        assertEquals(1, tree[1, 4])
        assertEquals(3, tree[5, 6])
        assertEquals(1, tree[3, 3])
    }

    @Test
    fun `get should compute max over full range`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::maxOf)
        assertEquals(9, tree[0, 7])
    }

    @Test
    fun `get should compute max over partial ranges`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::maxOf)
        assertEquals(8, tree[0, 2])
        assertEquals(9, tree[1, 4])
        assertEquals(7, tree[5, 6])
        assertEquals(9, tree[4, 4])
    }

    @Test
    fun `get should handle product operation`() {
        val tree = SegmentTree(listOf(2, 3, 4, 5), Int::times)
        assertEquals(120, tree[0, 3])
        assertEquals(6, tree[0, 1])
        assertEquals(20, tree[2, 3])
        assertEquals(60, tree[1, 3])
    }

    @Test
    fun `get should handle gcd operation`() {
        val tree = SegmentTree(listOf(12, 18, 24, 36, 48), ::gcd)
        assertEquals(6, tree[0, 4])
        assertEquals(6, tree[0, 1])
        assertEquals(12, tree[2, 4])
    }

    @Test
    fun `getOrNull should return values for valid ranges`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree.getOrNull(0, 4))
        assertEquals(6, tree.getOrNull(0, 2))
        assertEquals(3, tree.getOrNull(2, 2))
    }

    @Test
    fun `getOrNull should return null for invalid ranges`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertNull(tree.getOrNull(-1, 2))
        assertNull(tree.getOrNull(0, 5))
        assertNull(tree.getOrNull(3, 2))
        assertNull(tree.getOrNull(-5, -1))
        assertNull(tree.getOrNull(5, 10))
    }

    @Test
    fun `getOrNull should return null for empty tree`() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertNull(tree.getOrNull(0, 0))
        assertNull(tree.getOrNull(-1, 1))
    }

    @Test
    fun `getOrDefault should return values for valid ranges`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree.getOrDefault(0, 4, -1))
        assertEquals(6, tree.getOrDefault(0, 2, -1))
        assertEquals(3, tree.getOrDefault(2, 2, -1))
    }

    @Test
    fun `getOrDefault should return default for invalid ranges`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(-1, tree.getOrDefault(-1, 2, -1))
        assertEquals(-1, tree.getOrDefault(0, 5, -1))
        assertEquals(-1, tree.getOrDefault(3, 2, -1))
        assertEquals(999, tree.getOrDefault(-5, -1, 999))
    }

    @Test
    fun `getOrDefault should return default for empty tree`() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertEquals(0, tree.getOrDefault(0, 0, 0))
        assertEquals(42, tree.getOrDefault(-1, 1, 42))
    }

    @Test
    fun `update should modify single element`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree[0, 4])
        assertEquals(3, tree[2, 2])

        tree.update(2, 10)
        assertEquals(22, tree[0, 4])
        assertEquals(10, tree[2, 2])
        assertEquals(12, tree[1, 2])
    }

    @Test
    fun `update should modify multiple elements`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        tree.update(0, 10)
        tree.update(4, 50)
        assertEquals(69, tree[0, 4])
        assertEquals(10, tree[0, 0])
        assertEquals(50, tree[4, 4])
        assertEquals(9, tree[1, 3])
    }

    @Test
    fun `update should modify boundary elements`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        tree.update(0, 100)
        assertEquals(100, tree[0, 0])
        assertEquals(114, tree[0, 4])

        tree.update(4, 200)
        assertEquals(200, tree[4, 4])
        assertEquals(309, tree[0, 4])
    }

    @Test
    fun `update should work with min operation`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::minOf)
        assertEquals(1, tree[0, 7])

        tree.update(3, 10)
        assertEquals(2, tree[0, 7])
        assertEquals(10, tree[3, 3])
        assertEquals(2, tree[0, 2])
    }

    @Test
    fun `update should work with max operation`() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::maxOf)
        assertEquals(9, tree[0, 7])

        tree.update(4, 100)
        assertEquals(100, tree[0, 7])
        assertEquals(100, tree[4, 4])
        assertEquals(8, tree[0, 2])
    }

    @Test
    fun `update should handle multiple updates in sequence`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        val updates = listOf(
            Triple(0, 10, 24),
            Triple(2, 20, 41),
            Triple(4, 1, 37),
            Triple(1, 5, 40),
        )
        for ((index, value, expectedSum) in updates) {
            tree.update(index, value)
            assertEquals(expectedSum, tree[0, 4])
        }
    }

    @Test
    fun `get should throw when start index is negative`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertFailsWith<IllegalArgumentException> { tree[-1, 2] }
    }

    @Test
    fun `get should throw when end index is out of bounds`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertFailsWith<IllegalArgumentException> { tree[0, 5] }
    }

    @Test
    fun `get should throw when start is greater than end`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertFailsWith<IllegalArgumentException> { tree[3, 1] }
    }

    @Test
    fun `update should throw when index is negative`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertFailsWith<IllegalArgumentException> { tree.update(-1, 10) }
    }

    @Test
    fun `update should throw when index is out of bounds`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertFailsWith<IllegalArgumentException> { tree.update(5, 10) }
    }

    @Test
    fun `SegmentTree should handle large data sets`() {
        val tree = SegmentTree((1..1000).toList(), Int::plus)
        assertEquals(500500, tree[0, 999])
        assertEquals(5050, tree[0, 99])
        assertEquals(95050, tree[900, 999])

        tree.update(500, 0)
        assertEquals(500500 - 501, tree[0, 999])
    }

    @Test
    fun `get should handle all same values`() {
        val tree = SegmentTree(List(10) { 5 }, Int::plus)
        assertEquals(50, tree[0, 9])
        assertEquals(25, tree[0, 4])
        assertEquals(5, tree[3, 3])
    }

    @Test
    fun `get should handle negative numbers`() {
        val data = listOf(-5, -2, -8, -1, -9)
        val tree = SegmentTree(data, Int::plus)
        assertEquals(-25, tree[0, 4])
        assertEquals(-15, tree[0, 2])

        val minTree = SegmentTree(data, ::minOf)
        assertEquals(-9, minTree[0, 4])

        val maxTree = SegmentTree(data, ::maxOf)
        assertEquals(-1, maxTree[0, 4])
    }

    @Test
    fun `get should handle mixed positive and negative numbers`() {
        val tree = SegmentTree(listOf(-10, 5, -3, 8, -2, 4), Int::plus)
        assertEquals(2, tree[0, 5])
        assertEquals(-8, tree[0, 2])
        assertEquals(10, tree[3, 5])
    }

    @Test
    fun `SegmentTree should work with custom data types`() {
        data class Point(val x: Int, val y: Int)

        val data = listOf(
            Point(1, 2),
            Point(3, 4),
            Point(5, 6)
        )
        val tree = SegmentTree(data) { a, b -> Point(a.x + b.x, a.y + b.y) }
        val result = tree[0, 2]
        assertEquals(9, result.x)
        assertEquals(12, result.y)

        tree.update(1, Point(0, 0))
        val updatedResult = tree[0, 2]
        assertEquals(6, updatedResult.x)
        assertEquals(8, updatedResult.y)
    }

    @Test
    fun `get should handle string concatenation`() {
        val tree = SegmentTree(listOf("Hello", " ", "World", "!"), String::plus)
        assertEquals("Hello World!", tree[0, 3])
        assertEquals("Hello ", tree[0, 1])
        assertEquals("World!", tree[2, 3])

        tree.update(2, "Kotlin")
        assertEquals("Hello Kotlin!", tree[0, 3])
    }

    @Test
    fun `get should handle bitwise or operation`() {
        val tree = SegmentTree(listOf(1, 2, 4, 8, 16), Int::or)
        assertEquals(31, tree[0, 4])
        assertEquals(7, tree[0, 2])
        assertEquals(24, tree[3, 4])
    }

    @Test
    fun `SegmentTree should handle power of two sizes`() {
        for (size in listOf(1, 2, 4, 8, 16, 32, 64)) {
            val tree = SegmentTree((1..size).toList(), Int::plus)
            val expectedSum = size * (size + 1) / 2
            assertEquals(expectedSum, tree[0, size - 1], "Failed for size $size")
        }
    }

    @Test
    fun `SegmentTree should handle non-power of two sizes`() {
        for (size in listOf(3, 5, 7, 9, 15, 17, 31, 33)) {
            val tree = SegmentTree((1..size).toList(), Int::plus)
            val expectedSum = size * (size + 1) / 2
            assertEquals(expectedSum, tree[0, size - 1], "Failed for size $size")
        }
    }

    @Test
    fun `update should work with consecutive updates and queries`() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), ::maxOf)
        assertEquals(5, tree[0, 4])

        tree.update(2, 100)
        assertEquals(100, tree[0, 4])
        assertEquals(100, tree[2, 3])

        tree.update(4, 200)
        assertEquals(200, tree[0, 4])
        assertEquals(200, tree[3, 4])

        tree.update(0, 300)
        assertEquals(300, tree[0, 4])
        assertEquals(300, tree[0, 1])
    }
}
