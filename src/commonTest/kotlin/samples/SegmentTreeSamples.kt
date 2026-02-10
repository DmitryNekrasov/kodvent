/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.SegmentTree
import kodvent.math.gcd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SegmentTreeSamples {

    @Test
    fun singleIndexQuery() {
        val numbers = listOf(5, 10, 15, 20, 25)
        val segmentTree = SegmentTree(numbers, Int::plus)

        val value = segmentTree[2]
        assertEquals(15, value)

        val firstValue = segmentTree[0]
        assertEquals(5, firstValue)

        val lastValue = segmentTree[4]
        assertEquals(25, lastValue)
    }

    @Test
    fun rangeQuery() {
        val numbers = listOf(2, 4, 6, 8, 10)
        val segmentTree = SegmentTree(numbers, Int::plus)

        // Elements: 4, 6, 8
        val sum = segmentTree[1, 3]
        assertEquals(18, sum)

        // Elements: 2, 4, 6, 8, 10
        val totalSum = segmentTree[0, 4]
        assertEquals(30, totalSum)

        // Elements: 10
        val singleRange = segmentTree[4, 4]
        assertEquals(10, singleRange)
    }

    @Test
    fun basicRangeSumQuery() {
        // Create a segment tree for range sum queries
        val numbers = listOf(1, 3, 5, 7, 9, 11)
        val segmentTree = SegmentTree(numbers, Int::plus)

        // Query the sum of elements from index 1 to 4 (inclusive)
        // Elements: 3, 5, 7, 9
        val sum = segmentTree[1, 4]
        assertEquals(24, sum)

        // Query the sum of the entire array
        val totalSum = segmentTree[0, 5]
        assertEquals(36, totalSum)

        // Query a single element (range of size 1)
        val single = segmentTree[2, 2]
        assertEquals(5, single)

        // Use compound assignment operators to update values
        segmentTree[2] += 10  // 5 + 10 = 15
        assertEquals(15, segmentTree[2])
        assertEquals(46, segmentTree[0, 5])

        segmentTree[3] -= 2   // 7 - 2 = 5
        assertEquals(5, segmentTree[3])
        assertEquals(44, segmentTree[0, 5])
    }

    @Test
    fun rangeMinimumQuery() {
        // Create a segment tree for finding minimum values in ranges
        val numbers = listOf(5, 2, 8, 1, 9, 3, 7, 4)
        val segmentTree = SegmentTree(numbers, ::minOf)

        // Find minimum in range [0, 3]
        // Elements: 5, 2, 8, 1
        val min1 = segmentTree[0, 3]
        assertEquals(1, min1)

        // Find minimum in range [4, 7]
        // Elements: 9, 3, 7, 4
        val min2 = segmentTree[4, 7]
        assertEquals(3, min2)

        // Find minimum in the entire array
        val minAll = segmentTree[0, 7]
        assertEquals(1, minAll)
    }

    @Test
    fun rangeMaximumQuery() {
        // Create a segment tree for finding maximum values in ranges
        val numbers = listOf(5, 2, 8, 1, 9, 3, 7, 4)
        val segmentTree = SegmentTree(numbers, ::maxOf)

        // Find maximum in range [0, 3]
        // Elements: 5, 2, 8, 1
        val max1 = segmentTree[0, 3]
        assertEquals(8, max1)

        // Find maximum in range [4, 7]
        // Elements: 9, 3, 7, 4
        val max2 = segmentTree[4, 7]
        assertEquals(9, max2)

        // Find maximum in the entire array
        val maxAll = segmentTree[0, 7]
        assertEquals(9, maxAll)
    }

    @Test
    fun rangeGCDQuery() {
        // Create a segment tree for finding GCD of ranges
        val numbers = listOf(12, 18, 24, 30, 36, 42)
        val segmentTree = SegmentTree(numbers, ::gcd)

        // Find GCD of range [0, 2]
        // Elements: 12, 18, 24 -> GCD = 6
        val gcd1 = segmentTree[0, 2]
        assertEquals(6, gcd1)

        // Find GCD of range [3, 5]
        // Elements: 30, 36, 42 -> GCD = 6
        val gcd2 = segmentTree[3, 5]
        assertEquals(6, gcd2)

        // Find GCD of the entire array
        // All elements are multiples of 6, so GCD = 6
        val gcdAll = segmentTree[0, 5]
        assertEquals(6, gcdAll)
    }

    @Test
    fun stockPriceAnalysis() {
        // Analyze stock prices to find min/max in time ranges
        // Stock prices for 7 days: [100, 120, 95, 110, 105, 130, 125]
        val prices = listOf(100, 120, 95, 110, 105, 130, 125)

        val minTree = SegmentTree(prices, ::minOf)
        val maxTree = SegmentTree(prices, ::maxOf)

        // Find min and max prices for the first 3 days (indices 0-2)
        val minFirstThreeDays = minTree[0, 2]
        val maxFirstThreeDays = maxTree[0, 2]
        assertEquals(95, minFirstThreeDays)
        assertEquals(120, maxFirstThreeDays)

        // Find min and max prices for the last 4 days (indices 3-6)
        val minLastFourDays = minTree[3, 6]
        val maxLastFourDays = maxTree[3, 6]
        assertEquals(105, minLastFourDays)
        assertEquals(130, maxLastFourDays)

        // Calculate price volatility (max - min) for the entire week
        val weekMin = minTree[0, 6]
        val weekMax = maxTree[0, 6]
        val volatility = weekMax - weekMin
        assertEquals(35, volatility) // 130 - 95 = 35

        // Apply price corrections using compound assignment operators
        maxTree[2] += 5  // Day 2 price increased by 5: 95 + 5 = 100
        assertEquals(100, maxTree[2])
        assertEquals(130, maxTree[0, 6])  // Max is still 130

        maxTree[5] -= 20  // Day 5 price decreased by 20: 130 - 20 = 110
        assertEquals(110, maxTree[5])
        assertEquals(125, maxTree[0, 6])  // Max is now 125 (day 6)
    }

    @Test
    fun safeQueryMethods() {
        val numbers = listOf(5, 10, 15, 20, 25)
        val segmentTree = SegmentTree(numbers, Int::plus)

        // Valid queries
        assertEquals(45, segmentTree.getOrNull(1, 3)) // 10 + 15 + 20

        // Invalid queries return null with getOrNull
        assertNull(segmentTree.getOrNull(-1, 2))  // negative start
        assertNull(segmentTree.getOrNull(0, 10))  // end out of bounds
        assertNull(segmentTree.getOrNull(3, 1))   // start > end

        // Invalid queries return default value with getOrDefault
        assertEquals(0, segmentTree.getOrDefault(-1, 2, 0))
        assertEquals(-1, segmentTree.getOrDefault(0, 10, -1))
        assertEquals(100, segmentTree.getOrDefault(3, 1, 100))

        // Valid queries work normally with getOrDefault
        assertEquals(75, segmentTree.getOrDefault(0, 4, -1))
    }

    @Test
    fun competitiveProgrammingScenario() {
        // Solve a typical competitive programming problem:
        // Given an array, answer queries for a range minimum and support updates
        val initialArray = listOf(8, 3, 9, 5, 1, 7, 4, 6, 2)
        val segmentTree = SegmentTree(initialArray, ::minOf)

        // Query 1: Find minimum in range [2, 5]
        // Elements: 9, 5, 1, 7 -> minimum is 1
        assertEquals(1, segmentTree[2, 5])

        // Update: Change element at index 4 from 1 to 10
        segmentTree[4] = 10

        // Query 2: Find minimum in range [2, 5] after update
        // Elements: 9, 5, 10, 7 -> minimum is now 5
        assertEquals(5, segmentTree[2, 5])

        // Query 3: Find minimum in range [0, 3]
        // Elements: 8, 3, 9, 5 -> minimum is 3
        assertEquals(3, segmentTree[0, 3])

        // Update: Change element at index 1 from 3 to 1
        segmentTree[1] = 1

        // Query 4: Find minimum in range [0, 3] after update
        // Elements: 8, 1, 9, 5 -> minimum is now 1
        assertEquals(1, segmentTree[0, 3])

        // Query 5: Find minimum in an entire array
        assertEquals(1, segmentTree[0, 8])
    }

    @Test
    fun slidingWindowMinimum() {
        // Find minimum in sliding windows of size 3
        val array = listOf(4, 2, 7, 1, 5, 8, 3, 6)
        val segmentTree = SegmentTree(array, ::minOf)

        // Window [0, 2]: elements 4, 2, 7 -> min = 2
        assertEquals(2, segmentTree[0, 2])

        // Window [1, 3]: elements 2, 7, 1 -> min = 1
        assertEquals(1, segmentTree[1, 3])

        // Window [2, 4]: elements 7, 1, 5 -> min = 1
        assertEquals(1, segmentTree[2, 4])

        // Window [3, 5]: elements 1, 5, 8 -> min = 1
        assertEquals(1, segmentTree[3, 5])

        // Window [4, 6]: elements 5, 8, 3 -> min = 3
        assertEquals(3, segmentTree[4, 6])

        // Window [5, 7]: elements 8, 3, 6 -> min = 3
        assertEquals(3, segmentTree[5, 7])
    }

    @Test
    fun compoundAssignmentOperators() {
        val numbers = listOf(10, 20, 30, 40, 50)
        val segmentTree = SegmentTree(numbers, Int::plus)

        assertEquals(150, segmentTree[0, 4])

        segmentTree[2] += 5  // 30 + 5 = 35
        assertEquals(35, segmentTree[2])
        assertEquals(155, segmentTree[0, 4])

        segmentTree[1] -= 5  // 20 - 5 = 15
        assertEquals(15, segmentTree[1])
        assertEquals(150, segmentTree[0, 4])

        segmentTree[0] *= 2  // 10 * 2 = 20
        assertEquals(20, segmentTree[0])
        assertEquals(160, segmentTree[0, 4])

        segmentTree[4] /= 5  // 50 / 5 = 10
        assertEquals(10, segmentTree[4])
        assertEquals(120, segmentTree[0, 4])

        segmentTree[3] %= 13  // 40 % 13 = 1
        assertEquals(1, segmentTree[3])
        assertEquals(81, segmentTree[0, 4])

        segmentTree[2] += 10  // 35 + 10 = 45
        segmentTree[2] *= 2   // 45 * 2 = 90
        assertEquals(90, segmentTree[2])
        assertEquals(136, segmentTree[0, 4])
    }
}
