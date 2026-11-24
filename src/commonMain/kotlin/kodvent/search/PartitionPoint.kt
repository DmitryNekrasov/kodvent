/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.search

/**
 * Binary-searches the range `[[fromIndex], [toIndex])` for the partition point: the first index where the
 * monotonic [predicate] turns from `true` to `false`, or [toIndex] if it is `true` throughout.
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
        // high - low overflows for a range wider than Int.MAX_VALUE. Read as unsigned, it is still the exact width.
        val mid = low + (high - low).ushr(1)
        if (predicate(mid)) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}

/**
 * Binary-searches the range `[[fromIndex], [toIndex])` for the partition point: the first index where the
 * monotonic [predicate] turns from `true` to `false`, or [toIndex] if it is `true` throughout.
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
        // high - low overflows for a range wider than Long.MAX_VALUE. Read as unsigned, it is still the exact width.
        val mid = low + (high - low).ushr(1)
        if (predicate(mid)) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}
