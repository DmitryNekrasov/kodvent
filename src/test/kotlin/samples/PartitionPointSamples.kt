/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.boundsearch.partitionPoint
import kodvent.math.sqr
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        val index = partitionPoint(0, sortedArray.size) { sortedArray[it] < target }

        assertEquals(4, index)
        assertEquals(25, sortedArray[index])
    }

    @Test
    fun lowerBoundAndUpperBoundWithDuplicates() {
        // Find lower bound and upper bound in an array with duplicates
        val sortedArray = intArrayOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 11)
        val target = 9

        val lowerBound = partitionPoint(0, sortedArray.size) { sortedArray[it] < target }
        val upperBound = partitionPoint(0, sortedArray.size) { sortedArray[it] <= target }

        assertEquals(7, lowerBound)
        assertEquals(9, sortedArray[lowerBound])

        assertEquals(11, upperBound)
        assertEquals(11, sortedArray[upperBound])

        val count = upperBound - lowerBound
        assertEquals(4, count)
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
        // Find floor(sqrt(n)) using binary search with a Long version
        val n = 12_345_678_912_345L

        val sqrtFloor = partitionPoint(0L, 10_000_000L) { x -> x * x <= n } - 1

        assertEquals(3_513_641L, sqrtFloor)
        assertEquals(12_345_673_076_881L, sqrtFloor.sqr())
        assertTrue((sqrtFloor + 1).sqr() > n)
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

    @Test
    fun maximumMinimumDistanceBetweenBalls() {
        // Binary search on answer: find maximum minimum distance between m balls
        // placed in n positions (LeetCode 1552: Magnetic Force Between Two Balls)
        // https://leetcode.com/problems/magnetic-force-between-two-balls/

        fun canPlaceBalls(positions: IntArray, m: Int, minDistance: Int): Boolean {
            var ballsPlaced = 1
            var lastPosition = positions[0]
            for (i in 1..positions.lastIndex) {
                if (positions[i] - lastPosition >= minDistance) {
                    ballsPlaced++
                    lastPosition = positions[i]
                }
            }
            return ballsPlaced >= m
        }

        // Example 1: positions = [1,2,3,4,7], m = 3
        run {
            val positions = intArrayOf(1, 2, 3, 4, 7)
            val m = 3
            positions.sort()

            val partitionPoint = partitionPoint(0, positions.last() + 1) { distance ->
                canPlaceBalls(positions, m, distance)
            }
            val maxMinDistance = partitionPoint - 1

            assertEquals(3, maxMinDistance)
        }

        // Example 2: positions = [5,4,3,2,1,1000000000], m = 2
        run {
            val positions = intArrayOf(5, 4, 3, 2, 1, 1000000000)
            val m = 2
            positions.sort()

            val partitionPoint = partitionPoint(0, positions.last() + 1) { distance ->
                canPlaceBalls(positions, m, distance)
            }
            val maxMinDistance = partitionPoint - 1

            assertEquals(999999999, maxMinDistance)
        }
    }
}
