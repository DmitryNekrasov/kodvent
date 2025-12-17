/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.boundsearch

public inline fun partitionPoint(fromIndex: Int, toIndex: Int, predicate: (Int) -> Boolean): Int =
    partitionPoint(fromIndex.toLong(), toIndex.toLong()) { predicate(it.toInt()) }.toInt()

public inline fun partitionPoint(fromIndex: Long, toIndex: Long, predicate: (Long) -> Boolean): Long {
    var low = fromIndex
    var high = toIndex
    while (low < high) {
        val mid = (low + high).ushr(1)
        if (predicate(mid)) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}
