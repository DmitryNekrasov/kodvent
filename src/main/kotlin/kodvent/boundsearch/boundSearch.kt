/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.boundsearch

public fun partitionPoint(fromIndex: Int, toIndex: Int, predicate: (Int) -> Boolean): Int =
    lowerBound(fromIndex, toIndex, true, predicate)

public inline fun <T : Comparable<T>> lowerBound(
    fromIndex: Int,
    toIndex: Int,
    element: T,
    crossinline selector: (Int) -> T
): Int {
    var low = fromIndex
    var high = toIndex - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val cmp = compareValues(selector(mid), element)
        if (cmp < 0) {
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return low
}

/**
 * Checks that `from` and `to` are in
 * the range of [0..size] and throws an appropriate exception, if they aren't.
 */
private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
    when {
        fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }
}
