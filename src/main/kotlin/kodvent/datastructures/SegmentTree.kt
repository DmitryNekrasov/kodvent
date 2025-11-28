/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

public class SegmentTree<T>(source: Collection<T>, private val operation: (T, T) -> T) {
    private val size = source.size
    private val tree = MutableList<T?>(size * 4) { null }

    init {
        if (source.isNotEmpty()) {
            build(source)
        }
    }

    public operator fun get(start: Int, end: Int): T {
        require(start >= 0) { "Start index $start is negative. Valid range: [0, ${size - 1}]" }
        require(end < size) { "End index $end is out of bounds. Valid range: [0, ${size - 1}]" }
        require(start <= end) { "Start index $start is greater than end index $end" }

        return queryRange(1, 0, size - 1, start, end)
            ?: error("Query returned null for valid range [$start, $end]")
    }

    public fun getOrNull(start: Int, end: Int): T? {
        if (start < 0 || end >= size || start > end) return null
        return queryRange(1, 0, size - 1, start, end)
    }

    public fun getOrDefault(start: Int, end: Int, defaultValue: T): T {
        return getOrNull(start, end) ?: defaultValue
    }

    private fun build(source: Collection<T>, nodeIndex: Int = 1, start: Int = 0, end: Int = size - 1) {
        if (start == end) {
            tree[nodeIndex] = source.elementAt(start)
        } else {
            val mid = start + (end - start) / 2
            val leftNodeIndex = nodeIndex * 2
            val rightNodeIndex = nodeIndex * 2 + 1

            build(source, leftNodeIndex, start, mid)
            build(source, rightNodeIndex, mid + 1, end)

            tree[nodeIndex] = operation(tree[leftNodeIndex]!!, tree[rightNodeIndex]!!)
        }
    }

    private fun queryRange(nodeIndex: Int, start: Int, end: Int, queryStart: Int, queryEnd: Int): T? {
        return when {
            queryStart > queryEnd -> null
            queryStart == start && queryEnd == end -> tree[nodeIndex]
            else -> {
                val mid = start + (end - start) / 2
                val leftResult = queryRange(nodeIndex * 2, start, mid, queryStart, minOf(queryEnd, mid))
                val rightResult = queryRange(nodeIndex * 2 + 1, mid + 1, end, maxOf(queryStart, mid + 1), queryEnd)
                when {
                    leftResult == null -> rightResult
                    rightResult == null -> leftResult
                    else -> operation(leftResult, rightResult)
                }
            }
        }
    }
}
