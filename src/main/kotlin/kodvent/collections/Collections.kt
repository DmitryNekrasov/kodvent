/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.collections

import kodvent.boundsearch.partitionPoint

public fun <T : Comparable<T>> List<T>.lowerBound(element: T, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(size, fromIndex, toIndex)
    return partitionPoint(fromIndex, toIndex) { get(it) < element }
}

private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
    when {
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }
}
