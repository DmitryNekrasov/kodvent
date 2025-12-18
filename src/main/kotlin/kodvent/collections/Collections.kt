/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.collections

import kodvent.boundsearch.partitionPoint

/**
 * Finds the first position in this sorted list or its range where [element] could be inserted while maintaining sorted order.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of its elements,
 * otherwise the result is undefined.
 *
 * This function implements the lower bound algorithm: it returns the index of the first element that is NOT less than [element].
 * If all elements are less than [element], returns the end index ([toIndex]).
 * If the list contains multiple elements equal to [element], returns the index of the first such element.
 *
 * @return the index of the first element that is greater than or equal to [element], or [toIndex] if all elements are less than [element].
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T : Comparable<T>> List<T>.lowerBound(element: T, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { get(it) < element }
}

/**
 * Finds the first position in this sorted list or its range where [element] could be inserted while maintaining sorted order.
 * The list is expected to be sorted into ascending order according to the specified [comparator],
 * otherwise the result is undefined.
 *
 * This function implements the lower bound algorithm: it returns the index of the first element that is NOT less than [element]
 * according to the [comparator].
 * If all elements are less than [element], returns the end index ([toIndex]).
 * If the list contains multiple elements equal to [element], returns the index of the first such element.
 *
 * @return the index of the first element that is greater than or equal to [element] according to [comparator],
 *         or [toIndex] if all elements are less than [element].
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T> List<T>.lowerBound(element: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) < 0 }
}

/**
 * Finds the first position in this sorted list or its range where an element could be inserted while maintaining sorted order,
 * using the binary search algorithm with a custom [comparison] function.
 *
 * The list is expected to be sorted so that the signs of the [comparison] function's return values ascend on the list elements,
 * i.e., negative values come before zero and zeroes come before positive values.
 * Otherwise, the result is undefined.
 *
 * This function implements the lower bound algorithm: it returns the index of the first element for which [comparison] returns
 * a non-negative value (zero or positive).
 * If [comparison] returns negative for all elements, returns the end index ([toIndex]).
 * If the list contains multiple elements for which [comparison] returns zero, returns the index of the first such element.
 *
 * @param comparison function that returns negative values for elements before the insertion point,
 *                   zero for elements equal to the target, and positive values for elements after the target.
 *
 * @return the index of the first element for which [comparison] returns a non-negative value,
 *         or [toIndex] if [comparison] returns negative for all elements.
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T> List<T>.lowerBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) < 0 }
}

/**
 * Finds the first position in this sorted list or its range where an element having the key equal to the provided [key] value
 * could be inserted while maintaining sorted order, using the binary search algorithm.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of keys of its elements,
 * otherwise the result is undefined.
 *
 * This function implements the lower bound algorithm: it returns the index of the first element whose key is NOT less than [key].
 * If all element keys are less than [key], returns the end index ([toIndex]).
 * If the list contains multiple elements with the specified [key], returns the index of the first such element.
 *
 * @return the index of the first element whose key is greater than or equal to [key],
 *         or [toIndex] if all element keys are less than [key].
 *         Unlike [binarySearchBy], this always returns a non-negative insertion point.
 */
public inline fun <T, K : Comparable<K>> List<T>.lowerBoundBy(
    key: K?,
    fromIndex: Int = 0,
    toIndex: Int = size,
    crossinline selector: (T) -> K?
): Int = lowerBound(fromIndex, toIndex) { compareValues(selector(it), key) }

/**
 * Finds the last position in this sorted list or its range where [element] could be inserted while maintaining sorted order.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of its elements,
 * otherwise the result is undefined.
 *
 * This function implements the upper bound algorithm: it returns the index of the first element that is GREATER than [element].
 * If all elements are less than or equal to [element], returns the end index ([toIndex]).
 * If the list contains multiple elements equal to [element], returns the index immediately after the last such element.
 *
 * @return the index of the first element that is strictly greater than [element],
 *         or [toIndex] if all elements are less than or equal to [element].
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T : Comparable<T>> List<T>.upperBound(element: T, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { get(it) <= element }
}

/**
 * Finds the last position in this sorted list or its range where [element] could be inserted while maintaining sorted order.
 * The list is expected to be sorted into ascending order according to the specified [comparator],
 * otherwise the result is undefined.
 *
 * This function implements the upper bound algorithm: it returns the index of the first element that is GREATER than [element]
 * according to the [comparator].
 * If all elements are less than or equal to [element], returns the end index ([toIndex]).
 * If the list contains multiple elements equal to [element], returns the index immediately after the last such element.
 *
 * @return the index of the first element that is strictly greater than [element] according to [comparator],
 *         or [toIndex] if all elements are less than or equal to [element].
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T> List<T>.upperBound(element: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparator.compare(get(it), element) <= 0 }
}

/**
 * Finds the last position in this sorted list or its range where an element could be inserted while maintaining sorted order,
 * using the binary search algorithm with a custom [comparison] function.
 *
 * The list is expected to be sorted so that the signs of the [comparison] function's return values ascend on the list elements,
 * i.e., negative values come before zero and zeroes come before positive values.
 * Otherwise, the result is undefined.
 *
 * This function implements the upper bound algorithm: it returns the index of the first element for which [comparison] returns
 * a positive value.
 * If [comparison] returns non-positive (negative or zero) for all elements, returns the end index ([toIndex]).
 * If the list contains multiple elements for which [comparison] returns zero, returns the index immediately after the last such element.
 *
 * @param comparison function that returns negative values for elements before the target,
 *                   zero for elements equal to the target, and positive values for elements after the target.
 *
 * @return the index of the first element for which [comparison] returns a positive value,
 *         or [toIndex] if [comparison] returns non-positive for all elements.
 *         Unlike [binarySearch], this always returns a non-negative insertion point.
 */
public fun <T> List<T>.upperBound(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { comparison(get(it)) <= 0 }
}

/**
 * Finds the last position in this sorted list or its range where an element having the key equal to the provided [key] value
 * could be inserted while maintaining sorted order, using the binary search algorithm.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of keys of its elements,
 * otherwise the result is undefined.
 *
 * This function implements the upper bound algorithm: it returns the index of the first element whose key is GREATER than [key].
 * If all element keys are less than or equal to [key], returns the end index ([toIndex]).
 * If the list contains multiple elements with the specified [key], returns the index immediately after the last such element.
 *
 * @return the index of the first element whose key is strictly greater than [key],
 *         or [toIndex] if all element keys are less than or equal to [key].
 *         Unlike [binarySearchBy], this always returns a non-negative insertion point.
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
