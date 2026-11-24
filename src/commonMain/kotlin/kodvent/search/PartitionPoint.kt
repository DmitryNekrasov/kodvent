/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.search

/**
 * Returns the first index in `fromIndex..<toIndex` at which [predicate] is `false`, or [toIndex] if there is
 * none. It is a binary search, so [predicate] must be `true` up to some index and `false` from it on.
 *
 * [predicate] is called at most 32 times, for any range of Int. Two integer literals as bounds fit both
 * overloads, so give the lambda parameter a type, as in `partitionPoint(0, 100) { i: Int -> ... }`, or write
 * a bound as `0L` for the Long overload.
 *
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex]
 *
 * @sample samples.PartitionPointSamples.findFirstElementGreaterOrEqual
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
 * Returns the first index in `fromIndex..<toIndex` at which [predicate] is `false`, or [toIndex] if there is
 * none. It is a binary search, so [predicate] must be `true` up to some index and `false` from it on.
 *
 * [predicate] is called at most 64 times, for any range of Long. Two integer literals as bounds fit both
 * overloads, so write a bound as `0L` or give the lambda parameter a type, as in
 * `partitionPoint(0, 100) { i: Long -> ... }`.
 *
 * @throws IllegalArgumentException if [fromIndex] is greater than [toIndex]
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
