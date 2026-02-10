/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.boundsearch

/**
 * Finds the partition point in the range `[[fromIndex], [toIndex])` where a predicate transitions from `true` to `false`.
 *
 * This function uses binary search to efficiently locate the first index in the range where the [predicate]
 * returns `false`. The predicate must be monotonic: if it returns `false` for some index, it must return `false`
 * for all later indices in the range.
 *
 * @param fromIndex The start of the range (inclusive). Must not be greater than [toIndex].
 * @param toIndex The end of the range (exclusive). Must not be less than [fromIndex].
 * @param predicate A monotonic predicate function that takes an index and returns `true` for all indices
 *                  before the partition point and `false` from the partition point onwards.
 *
 * @return The partition point: the first index where [predicate] returns `false`, or [toIndex] if [predicate]
 *         is `true` for all indices in the range.
 *
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.PartitionPointSamples.insertionPointInSortedArray
 * @sample samples.PartitionPointSamples.findFirstElementGreaterOrEqual
 * @sample samples.PartitionPointSamples.lowerBoundAndUpperBoundWithDuplicates
 * @sample samples.PartitionPointSamples.findFirstElementGreaterOrEqualNotFound
 * @sample samples.PartitionPointSamples.longRangeSearch
 * @sample samples.PartitionPointSamples.findFirstNegativeInFunction
 * @sample samples.PartitionPointSamples.findFirstFailingTest
 * @sample samples.PartitionPointSamples.searchInRange
 * @sample samples.PartitionPointSamples.maximumMinimumDistanceBetweenBalls
 */
@Suppress("DuplicatedCode")
public inline fun partitionPoint(fromIndex: Int, toIndex: Int, predicate: (Int) -> Boolean): Int {
    require(fromIndex <= toIndex) { "fromIndex ($fromIndex) is greater than toIndex ($toIndex)." }
    var low = fromIndex
    var high = toIndex
    while (low < high) {
        val mid = low + (high - low).shr(1)
        if (predicate(mid)) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}

/**
 * Finds the partition point in the range `[[fromIndex], [toIndex])` where a predicate transitions from `true` to `false`.
 *
 * This function uses binary search to efficiently locate the first index in the range where the [predicate]
 * returns `false`. The predicate must be monotonic: if it returns `false` for some index, it must return `false`
 * for all later indices in the range.
 *
 * @param fromIndex The start of the range (inclusive). Must not be greater than [toIndex].
 * @param toIndex The end of the range (exclusive). Must not be less than [fromIndex].
 * @param predicate A monotonic predicate function that takes an index and returns `true` for all indices
 *                  before the partition point and `false` from the partition point onwards.
 *
 * @return The partition point: the first index where [predicate] returns `false`, or [toIndex] if [predicate]
 *         is `true` for all indices in the range.
 *
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex].
 *
 * @sample samples.PartitionPointSamples.squareRootFloor
 */
@Suppress("DuplicatedCode")
public inline fun partitionPoint(fromIndex: Long, toIndex: Long, predicate: (Long) -> Boolean): Long {
    require(fromIndex <= toIndex) { "fromIndex ($fromIndex) is greater than toIndex ($toIndex)." }
    var low = fromIndex
    var high = toIndex
    while (low < high) {
        val mid = low + (high - low).shr(1)
        if (predicate(mid)) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}

/**
 * Finds the argument that maximizes [f] on the interval [[left], [right]]
 * using ternary search.
 *
 * The function [f] must be **unimodal** on the given interval,
 * i.e., strictly increasing then strictly decreasing.
 *
 * @param left the left bound of the search interval.
 * @param right the right bound of the search interval; must be >= [left].
 * @param f the unimodal function to maximize.
 *
 * @return the argument `x` in [[left], [right]] at which [f] attains its maximum,
 *         with precision up to `1e-9`.
 *
 * @throws IllegalArgumentException if [left] > [right].
 *
 * @sample samples.TernarySearchSamples.maximizeQuadratic
 * @sample samples.TernarySearchSamples.maximizeSinFunction
 * @sample samples.TernarySearchSamples.minimizeByNegation
 * @sample samples.TernarySearchSamples.maximumAreaRectangleWithFixedPerimeter
 */
public inline fun ternarySearch(left: Double, right: Double, f: (Double) -> Double): Double {
    require(left <= right) { "left bound ($left) is greater than right bound ($right)." }
    val eps = 1e-9
    var l = left
    var r = right
    while (r - l > eps) {
        val m1 = l + (r - l) / 3
        val m2 = r - (r - l) / 3
        if (f(m1) < f(m2)) {
            l = m1
        } else {
            r = m2
        }
    }
    return l
}
