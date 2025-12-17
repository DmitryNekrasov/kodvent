/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.boundsearch

public fun partitionPoint(fromIndex: Int, toIndex: Int, predicate: (Int) -> Boolean): Int =
    lowerBound(fromIndex, toIndex, true, predicate)

public fun partitionPoint(fromIndex: Long, toIndex: Long, predicate: (Long) -> Boolean): Long =
    lowerBound(fromIndex, toIndex, true, predicate)

public inline fun <T : Comparable<T>> lowerBound(
    fromIndex: Int,
    toIndex: Int,
    element: T,
    crossinline selector: (Int) -> T
): Int = lowerBound(
    fromIndex = fromIndex.toLong(),
    toIndex = toIndex.toLong(),
    element = element,
    selector = { selector(it.toInt()) }
).toInt()

public inline fun <T : Comparable<T>> lowerBound(
    fromIndex: Long,
    toIndex: Long,
    element: T,
    crossinline selector: (Long) -> T
): Long {
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
