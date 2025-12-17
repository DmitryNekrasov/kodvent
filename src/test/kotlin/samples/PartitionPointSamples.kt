/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.boundsearch.partitionPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class PartitionPointSamples {

    @Test
    fun insertionPointInSortedArray() {
        // Find where to insert a value to maintain sorted order
        val sortedArray = intArrayOf(10, 20, 30, 40, 50)
        val valueToInsert = 35

        val insertionIndex = partitionPoint(0, sortedArray.size) { i ->
            sortedArray[i] < valueToInsert
        }

        assertEquals(3, insertionIndex)
    }

    @Test
    fun findFirstElementGreaterOrEqual() {
        // Find the first element in a sorted array that is >= target value
        val sortedArray = intArrayOf(5, 10, 15, 20, 25, 30, 35, 40)
        val target = 22

        val index = partitionPoint(0, sortedArray.size) { i ->
            sortedArray[i] < target
        }

        assertEquals(4, index)
        assertEquals(25, sortedArray[index])
    }

    @Test
    fun findFirstElementGreaterOrEqualNotFound() {
        // When no element satisfies the condition, returns toIndex
        val sortedArray = intArrayOf(5, 10, 15, 20, 25)
        val target = 100

        val index = partitionPoint(0, sortedArray.size) { i ->
            sortedArray[i] < target
        }

        assertEquals(sortedArray.size, index)
    }

    @Test
    fun squareRootFloor() {
        // Find floor(sqrt(n)) using binary search
        val n = 50

        val sqrtFloor = partitionPoint(0, n + 1) { x -> x * x <= n } - 1

        assertEquals(7, sqrtFloor)
        assertEquals(49, sqrtFloor * sqrtFloor)
    }

    @Test
    fun longRangeSearch() {
        // Find the first power of 2 that exceeds a large number
        val threshold = 1_000_000_000L

        val exponent = partitionPoint(0, 64) { exp: Int ->
            1L shl exp <= threshold
        }

        assertEquals(30, exponent)
        assertEquals(1_073_741_824L, 1L shl exponent)
    }

    @Test
    fun findFirstNegativeInFunction() {
        // Find where a function transitions from positive to negative
        fun f(x: Int): Int = 100 - x * x

        val firstNegative = partitionPoint(-20, 20) { x: Int -> f(x) >= 0 }

        assertEquals(11, firstNegative)
        assertEquals(0, f(10))
        assertEquals(-21, f(11))
    }

    @Test
    fun findFirstFailingTest() {
        // Simulate finding the first failing version in a release sequence
        val versions = listOf("1.0", "1.1", "1.2", "1.3", "1.4", "1.5")
        val brokenVersions = setOf("1.3", "1.4", "1.5")

        val firstBrokenIndex = partitionPoint(0, versions.size) { i ->
            versions[i] !in brokenVersions
        }

        assertEquals(3, firstBrokenIndex)
        assertEquals("1.3", versions[firstBrokenIndex])
    }

    @Test
    fun searchInRange() {
        // Search only in a subrange of an array
        val array = intArrayOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
        val target = 12

        val fromIndex = 3
        val toIndex = 8

        val index = partitionPoint(fromIndex, toIndex) { i -> array[i] < target }

        assertEquals(6, index)
        assertEquals(13, array[index])
    }
}
