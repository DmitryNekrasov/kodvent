/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.search

/**
 * Returns the index of the first element in `fromIndex..<toIndex` that is not less than [element], or
 * [toIndex] if there is none. The range must be sorted in natural order with `null` first. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.lowerBoundWithDuplicates
 */
public fun <T : Comparable<T>> List<T?>.lowerBound(element: T?, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { compareValues(get(it), element) < 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` that is not less than [element] by
 * [comparator], or [toIndex] if there is none. The range must be sorted by [comparator]. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.lowerBoundWithComparator
 */
public fun <T> List<T>.lowerBound(
    element: T,
    comparator: Comparator<in T>,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) < 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` for which [comparison] is not negative,
 * or [toIndex] if there is none. Along the range [comparison] must be negative first, then zero, then
 * positive, and any of these three parts may be empty. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.lowerBoundWithComparisonFunction
 */
public fun <T> List<T>.lowerBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) < 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` whose key, given by [selector], is not less
 * than [key], or [toIndex] if there is none. The range must be sorted by that key, `null` first. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.lowerBoundByWithSelector
 */
public inline fun <T, K : Comparable<K>> List<T>.lowerBoundBy(
    key: K?,
    fromIndex: Int = 0,
    toIndex: Int = size,
    crossinline selector: (T) -> K?
): Int = lowerBound(fromIndex, toIndex) { compareValues(selector(it), key) }

/**
 * Returns the index of the first element in `fromIndex..<toIndex` that is greater than [element], or
 * [toIndex] if there is none. The range must be sorted in natural order with `null` first. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.upperBoundWithDuplicates
 */
public fun <T : Comparable<T>> List<T?>.upperBound(element: T?, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { compareValues(get(it), element) <= 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` that is greater than [element] by
 * [comparator], or [toIndex] if there is none. The range must be sorted by [comparator]. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.upperBoundWithComparator
 */
public fun <T> List<T>.upperBound(
    element: T,
    comparator: Comparator<in T>,
    fromIndex: Int = 0,
    toIndex: Int = size
): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) <= 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` for which [comparison] is positive, or
 * [toIndex] if there is none. Along the range [comparison] must be negative first, then zero, then positive,
 * and any of these three parts may be empty. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.upperBoundWithComparisonFunction
 */
public fun <T> List<T>.upperBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) <= 0 }
}

/**
 * Returns the index of the first element in `fromIndex..<toIndex` whose key, given by [selector], is greater
 * than [key], or [toIndex] if there is none. The range must be sorted by that key, `null` first. O(log n).
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex]
 *
 * @sample samples.LowerAndUpperBoundSamples.upperBoundByWithSelector
 */
public inline fun <T, K : Comparable<K>> List<T>.upperBoundBy(
    key: K?,
    fromIndex: Int = 0,
    toIndex: Int = size,
    crossinline selector: (T) -> K?
): Int = upperBound(fromIndex, toIndex) { compareValues(selector(it), key) }

private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
    when {
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }
}
