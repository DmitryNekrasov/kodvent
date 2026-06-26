/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.collections

import kodvent.boundsearch.partitionPoint

/**
 * Returns the lower bound of [element] in this naturally-sorted list: the index of the first element
 * not less than [element], or [toIndex] if all are smaller. Searches [[fromIndex], [toIndex]) (the whole
 * list by default); always a non-negative insertion point. `null` sorts before any value, and the result
 * is undefined if the list isn't sorted.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.lowerBoundWithDuplicates
 * @sample samples.BoundSearchSamples.countOccurrencesUsingBounds
 * @sample samples.BoundSearchSamples.findRangeInSublist
 * @sample samples.BoundSearchSamples.insertionPointMaintainsSortOrder
 */
public fun <T : Comparable<T>> List<T?>.lowerBound(element: T?, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { compareValues(get(it), element) < 0 }
}

/**
 * Returns the lower bound of [element] in this list sorted by [comparator]: the index of the first
 * element not less than [element], or [toIndex] if all are smaller. Searches [[fromIndex], [toIndex])
 * (the whole list by default); always a non-negative insertion point. `null` sorts before any value, and
 * the result is undefined if the list isn't sorted.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.lowerBoundWithComparator
 */
public fun <T> List<T>.lowerBound(element: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) < 0 }
}

/**
 * Returns the lower bound using a [comparison] function (negative before the target, zero at it, positive
 * after): the index of the first element for which [comparison] is non-negative, or [toIndex] if it is
 * negative for all. Searches [[fromIndex], [toIndex]) (the whole list by default); always a non-negative
 * insertion point. The result is undefined unless [comparison]'s sign is non-decreasing over the list.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.lowerBoundWithComparisonFunction
 */
public fun <T> List<T>.lowerBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) < 0 }
}

/**
 * Returns the lower bound of [key] in this list, sorted by the keys from [selector]: the index of the
 * first element whose key is not less than [key], or [toIndex] if all keys are smaller. Searches
 * [[fromIndex], [toIndex]) (the whole list by default); always a non-negative insertion point. `null`
 * sorts before any value, and the result is undefined if the list isn't sorted by key.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.lowerBoundByWithSelector
 */
public inline fun <T, K : Comparable<K>> List<T>.lowerBoundBy(
    key: K?,
    fromIndex: Int = 0,
    toIndex: Int = size,
    crossinline selector: (T) -> K?
): Int = lowerBound(fromIndex, toIndex) { compareValues(selector(it), key) }

/**
 * Returns the upper bound of [element] in this naturally-sorted list: the index of the first element
 * greater than [element], or [toIndex] if none is. Searches [[fromIndex], [toIndex]) (the whole list by
 * default); always a non-negative insertion point. `null` sorts before any value, and the result is
 * undefined if the list isn't sorted.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.upperBoundWithDuplicates
 * @sample samples.BoundSearchSamples.countOccurrencesUsingBounds
 * @sample samples.BoundSearchSamples.upperBoundWithNulls
 * @sample samples.BoundSearchSamples.findRangeInSublist
 */
public fun <T : Comparable<T>> List<T?>.upperBound(element: T?, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { compareValues(get(it), element) <= 0 }
}

/**
 * Returns the upper bound of [element] in this list sorted by [comparator]: the index of the first
 * element greater than [element], or [toIndex] if none is. Searches [[fromIndex], [toIndex]) (the whole
 * list by default); always a non-negative insertion point. `null` sorts before any value, and the result
 * is undefined if the list isn't sorted.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.upperBoundWithComparator
 */
public fun <T> List<T>.upperBound(element: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) <= 0 }
}

/**
 * Returns the upper bound using a [comparison] function (negative before the target, zero at it, positive
 * after): the index of the first element for which [comparison] is positive, or [toIndex] if it is
 * non-positive for all. Searches [[fromIndex], [toIndex]) (the whole list by default); always a
 * non-negative insertion point. The result is undefined unless [comparison]'s sign is non-decreasing over the list.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.upperBoundWithComparisonFunction
 */
public fun <T> List<T>.upperBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) <= 0 }
}

/**
 * Returns the upper bound of [key] in this list, sorted by the keys from [selector]: the index of the
 * first element whose key is greater than [key], or [toIndex] if none is. Searches [[fromIndex], [toIndex])
 * (the whole list by default); always a non-negative insertion point. `null` sorts before any value, and
 * the result is undefined if the list isn't sorted by key.
 *
 * @throws IndexOutOfBoundsException if [fromIndex] is negative or [toIndex] exceeds the size.
 * @throws IllegalArgumentException if [fromIndex] exceeds [toIndex].
 *
 * @sample samples.BoundSearchSamples.upperBoundByWithSelector
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
