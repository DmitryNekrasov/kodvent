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
    fun testEmptySegmentTree() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertNull(tree.getOrNull(0, 0))
    }

    @Test
    fun testSingleElementSegmentTree() {
        val tree = SegmentTree(listOf(42), Int::plus)
        assertEquals(42, tree[0, 0])
        assertEquals(42, tree.getOrNull(0, 0))
        assertEquals(42, tree.getOrDefault(0, 0, 0))
    }

    @Test
    fun testMultipleElementsConstruction() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree[0, 4])
    }

    @Test
    fun testSumFullRange() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5, 6, 7, 8), Int::plus)
        assertEquals(36, tree[0, 7])
    }

    @Test
    fun testSumPartialRanges() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5, 6, 7, 8), Int::plus)
        assertEquals(6, tree[0, 2])
        assertEquals(22, tree[3, 6])
        assertEquals(21, tree[5, 7])
        assertEquals(5, tree[1, 2])
    }

    @Test
    fun testSumSingleElement() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(1, tree[0, 0])
        assertEquals(3, tree[2, 2])
        assertEquals(5, tree[4, 4])
    }

    @Test
    fun testSumAdjacentElements() {
        val tree = SegmentTree(listOf(10, 20, 30, 40, 50), Int::plus)
        assertEquals(30, tree[0, 1])
        assertEquals(50, tree[1, 2])
        assertEquals(90, tree[3, 4])
    }

    @Test
    fun testMinFullRange() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::minOf)
        assertEquals(1, tree[0, 7])
    }

    @Test
    fun testMinPartialRanges() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::minOf)
        assertEquals(2, tree[0, 2])
        assertEquals(1, tree[1, 4])
        assertEquals(3, tree[5, 6])
        assertEquals(1, tree[3, 3])
    }

    @Test
    fun testMaxFullRange() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::maxOf)
        assertEquals(9, tree[0, 7])
    }

    @Test
    fun testMaxPartialRanges() {
        val tree = SegmentTree(listOf(5, 2, 8, 1, 9, 3, 7, 4), ::maxOf)
        assertEquals(8, tree[0, 2])
        assertEquals(9, tree[1, 4])
        assertEquals(7, tree[5, 6])
        assertEquals(9, tree[4, 4])
    }

    @Test
    fun testProductOperation() {
        val tree = SegmentTree(listOf(2, 3, 4, 5), Int::times)
        assertEquals(120, tree[0, 3])
        assertEquals(6, tree[0, 1])
        assertEquals(20, tree[2, 3])
        assertEquals(60, tree[1, 3])
    }

    @Test
    fun testGcdOperation() {
        val tree = SegmentTree(listOf(12, 18, 24, 36, 48), ::gcd)
        assertEquals(6, tree[0, 4])
        assertEquals(6, tree[0, 1])
        assertEquals(12, tree[2, 4])
    }

    @Test
    fun testGetOrNullValidRanges() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree.getOrNull(0, 4))
        assertEquals(6, tree.getOrNull(0, 2))
        assertEquals(3, tree.getOrNull(2, 2))
    }

    @Test
    fun testGetOrNullInvalidRanges() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertNull(tree.getOrNull(-1, 2))
        assertNull(tree.getOrNull(0, 5))
        assertNull(tree.getOrNull(3, 2))
        assertNull(tree.getOrNull(-5, -1))
        assertNull(tree.getOrNull(5, 10))
    }

    @Test
    fun testGetOrNullEmptyTree() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertNull(tree.getOrNull(0, 0))
        assertNull(tree.getOrNull(-1, 1))
    }

    @Test
    fun testGetOrDefaultValidRanges() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(15, tree.getOrDefault(0, 4, -1))
        assertEquals(6, tree.getOrDefault(0, 2, -1))
        assertEquals(3, tree.getOrDefault(2, 2, -1))
    }

    @Test
    fun testGetOrDefaultInvalidRanges() {
        val tree = SegmentTree(listOf(1, 2, 3, 4, 5), Int::plus)
        assertEquals(-1, tree.getOrDefault(-1, 2, -1))
        assertEquals(-1, tree.getOrDefault(0, 5, -1))
        assertEquals(-1, tree.getOrDefault(3, 2, -1))
        assertEquals(999, tree.getOrDefault(-5, -1, 999))
    }

    @Test
    fun testGetOrDefaultEmptyTree() {
        val tree = SegmentTree(emptyList(), Int::plus)
        assertEquals(0, tree.getOrDefault(0, 0, 0))
        assertEquals(42, tree.getOrDefault(-1, 1, 42))
    }

    @Test
    fun testUpdateSingleElement() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        // Before update
        assertEquals(15, tree[0, 4])
        assertEquals(3, tree[2, 2])

        // Update middle element
        tree.update(2, 10)

        // After update
        assertEquals(22, tree[0, 4])  // 1 + 2 + 10 + 4 + 5
        assertEquals(10, tree[2, 2])
        assertEquals(12, tree[1, 2])  // 2 + 10
    }

    @Test
    fun testUpdateMultipleElements() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        tree.update(0, 10)
        tree.update(4, 50)

        assertEquals(69, tree[0, 4])  // 10 + 2 + 3 + 4 + 50
        assertEquals(10, tree[0, 0])
        assertEquals(50, tree[4, 4])
        assertEquals(9, tree[1, 3])  // 2 + 3 + 4 (unchanged)
    }

    @Test
    fun testUpdateBoundaryElements() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        tree.update(0, 100)
        assertEquals(100, tree[0, 0])
        assertEquals(114, tree[0, 4])

        tree.update(4, 200)
        assertEquals(200, tree[4, 4])
        assertEquals(309, tree[0, 4])
    }

    @Test
    fun testUpdateWithMinOperation() {
        val data = listOf(5, 2, 8, 1, 9, 3, 7, 4)
        val tree = SegmentTree(data) { a, b -> minOf(a, b) }

        assertEquals(1, tree[0, 7])

        // Update the minimum element
        tree.update(3, 10)

        assertEquals(2, tree[0, 7])  // Now minimum is 2
        assertEquals(10, tree[3, 3])
        assertEquals(2, tree[0, 2])  // min(5, 2, 8)
    }

    @Test
    fun testUpdateWithMaxOperation() {
        val data = listOf(5, 2, 8, 1, 9, 3, 7, 4)
        val tree = SegmentTree(data) { a, b -> maxOf(a, b) }

        assertEquals(9, tree[0, 7])

        // Update the maximum element
        tree.update(4, 100)

        assertEquals(100, tree[0, 7])  // Now maximum is 100
        assertEquals(100, tree[4, 4])
        assertEquals(8, tree[0, 2])    // max(5, 2, 8) unchanged
    }

    @Test
    fun testMultipleUpdatesSequence() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val updates = listOf(
            Triple(0, 10, 24),  // update(0, 10) -> sum = 24
            Triple(2, 20, 41),  // update(2, 20) -> sum = 41
            Triple(4, 1, 37),   // update(4, 1) -> sum = 37
            Triple(1, 5, 40),   // update(1, 5) -> sum = 40
        )

        for ((index, value, expectedSum) in updates) {
            tree.update(index, value)
            assertEquals(expectedSum, tree[0, 4])
        }
    }

    // ==================== Error Condition Tests ====================

    @Test
    fun testGetWithNegativeStartIndex() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val exception = assertFailsWith<IllegalArgumentException> {
            tree[-1, 2]
        }
        assert(exception.message!!.contains("negative"))
    }

    @Test
    fun testGetWithEndIndexOutOfBounds() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val exception = assertFailsWith<IllegalArgumentException> {
            tree[0, 5]
        }
        assert(exception.message!!.contains("out of bounds"))
    }

    @Test
    fun testGetWithStartGreaterThanEnd() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val exception = assertFailsWith<IllegalArgumentException> {
            tree[3, 1]
        }
        assert(exception.message!!.contains("greater than end"))
    }

    @Test
    fun testUpdateWithNegativeIndex() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val exception = assertFailsWith<IllegalArgumentException> {
            tree.update(-1, 10)
        }
        assert(exception.message!!.contains("out of bounds"))
    }

    @Test
    fun testUpdateWithIndexOutOfBounds() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> a + b }

        val exception = assertFailsWith<IllegalArgumentException> {
            tree.update(5, 10)
        }
        assert(exception.message!!.contains("out of bounds"))
    }

    // ==================== Edge Cases and Special Scenarios ====================

    @Test
    fun testLargeDataSet() {
        val data = (1..1000).toList()
        val tree = SegmentTree(data) { a, b -> a + b }

        // Sum of 1 to 1000 = 500500
        assertEquals(500500, tree[0, 999])

        // Test various ranges
        assertEquals(5050, tree[0, 99])     // Sum of 1 to 100
        assertEquals(95050, tree[900, 999])  // Sum of 901 to 1000

        // Update and verify
        tree.update(500, 0)
        assertEquals(500500 - 501, tree[0, 999])
    }

    @Test
    fun testAllSameValues() {
        val data = List(10) { 5 }
        val tree = SegmentTree(data) { a, b -> a + b }

        assertEquals(50, tree[0, 9])
        assertEquals(25, tree[0, 4])
        assertEquals(5, tree[3, 3])
    }

    @Test
    fun testNegativeNumbers() {
        val data = listOf(-5, -2, -8, -1, -9)
        val tree = SegmentTree(data) { a, b -> a + b }

        assertEquals(-25, tree[0, 4])
        assertEquals(-15, tree[0, 2])

        val minTree = SegmentTree(data) { a, b -> minOf(a, b) }
        assertEquals(-9, minTree[0, 4])

        val maxTree = SegmentTree(data) { a, b -> maxOf(a, b) }
        assertEquals(-1, maxTree[0, 4])
    }

    @Test
    fun testMixedPositiveNegativeNumbers() {
        val data = listOf(-10, 5, -3, 8, -2, 4)
        val tree = SegmentTree(data) { a, b -> a + b }

        assertEquals(2, tree[0, 5])
        assertEquals(-8, tree[0, 2])
        assertEquals(10, tree[3, 5])
    }

    @Test
    fun testWithCustomDataType() {
        data class Point(val x: Int, val y: Int)

        val data = listOf(
            Point(1, 2),
            Point(3, 4),
            Point(5, 6)
        )

        val tree = SegmentTree(data) { a, b -> Point(a.x + b.x, a.y + b.y) }

        val result = tree[0, 2]
        assertEquals(9, result.x)  // 1 + 3 + 5
        assertEquals(12, result.y) // 2 + 4 + 6

        tree.update(1, Point(0, 0))
        val updatedResult = tree[0, 2]
        assertEquals(6, updatedResult.x)  // 1 + 0 + 5
        assertEquals(8, updatedResult.y)  // 2 + 0 + 6
    }

    @Test
    fun testStringConcatenation() {
        val data = listOf("Hello", " ", "World", "!")
        val tree = SegmentTree(data) { a, b -> a + b }

        assertEquals("Hello World!", tree[0, 3])
        assertEquals("Hello ", tree[0, 1])
        assertEquals("World!", tree[2, 3])

        tree.update(2, "Kotlin")
        assertEquals("Hello Kotlin!", tree[0, 3])
    }

    @Test
    fun testBitWiseOrOperation() {
        val data = listOf(1, 2, 4, 8, 16)
        val tree = SegmentTree(data) { a, b -> a or b }

        assertEquals(31, tree[0, 4])  // 1 | 2 | 4 | 8 | 16 = 31
        assertEquals(7, tree[0, 2])   // 1 | 2 | 4 = 7
        assertEquals(24, tree[3, 4])  // 8 | 16 = 24
    }

    @Test
    fun testPowerOfTwoSizes() {
        // Test with sizes that are powers of 2
        for (size in listOf(1, 2, 4, 8, 16, 32, 64)) {
            val data = (1..size).toList()
            val tree = SegmentTree(data) { a, b -> a + b }
            val expectedSum = size * (size + 1) / 2
            assertEquals(expectedSum, tree[0, size - 1], "Failed for size $size")
        }
    }

    @Test
    fun testNonPowerOfTwoSizes() {
        // Test with sizes that are NOT powers of 2
        for (size in listOf(3, 5, 7, 9, 15, 17, 31, 33)) {
            val data = (1..size).toList()
            val tree = SegmentTree(data) { a, b -> a + b }
            val expectedSum = size * (size + 1) / 2
            assertEquals(expectedSum, tree[0, size - 1], "Failed for size $size")
        }
    }

    @Test
    fun testConsecutiveUpdatesAndQueries() {
        val data = listOf(1, 2, 3, 4, 5)
        val tree = SegmentTree(data) { a, b -> maxOf(a, b) }

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
