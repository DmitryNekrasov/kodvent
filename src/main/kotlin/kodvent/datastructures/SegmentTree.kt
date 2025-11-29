/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * A [segment tree data structure](https://en.wikipedia.org/wiki/Segment_tree)
 * that supports efficient range queries and point updates.
 *
 * A segment tree is a binary tree used for storing intervals or segments. It allows querying
 * which of the stored segments contain a given point and supports efficient range queries
 * using an associative binary operation.
 *
 * ## Time Complexity
 * - Construction: O(n)
 * - Range query: O(log n)
 * - Point update: O(log n)
 *
 * ## Space Complexity
 * - O(4n) internal storage (O(n) in Big-O notation) where n is the size of the [source] list
 *
 * @param T the type of elements in the segment tree
 * @param source the initial list of elements to build the segment tree from
 * @param operation an associative binary operation (e.g., sum, min, max, gcd) to combine elements.
 *        Must be associative: `operation(a, operation(b, c)) == operation(operation(a, b), c)`
 *
 * @sample samples.SegmentTreeSamples.basicRangeSumQuery
 * @sample samples.SegmentTreeSamples.rangeMinimumQuery
 * @sample samples.SegmentTreeSamples.rangeMaximumQuery
 * @sample samples.SegmentTreeSamples.rangeGCDQuery
 * @sample samples.SegmentTreeSamples.stockPriceAnalysis
 * @sample samples.SegmentTreeSamples.competitiveProgrammingScenario
 * @sample samples.SegmentTreeSamples.slidingWindowMinimum
 */
public class SegmentTree<T>(source: List<T>, private val operation: (T, T) -> T) {
    private val size = source.size
    private val tree = MutableList<T?>(size * 4) { null }

    init {
        if (source.isNotEmpty()) {
            build(source)
        }
    }

    /**
     * Queries the result of applying the operation over the range [[start], [end]] (inclusive).
     *
     * This operator function allows using bracket notation for range queries.
     *
     * @param start the starting index of the range (inclusive); must be non-negative
     * @param end the ending index of the range (inclusive); must be less than the tree size
     *
     * @return the result of applying the operation to all elements in the range [[start], [end]]
     *
     * @throws IllegalArgumentException if the tree is empty or [start] > [end]
     * @throws IndexOutOfBoundsException if [start] is negative or [end] is out of bounds
     */
    public operator fun get(start: Int, end: Int): T {
        require(size > 0) { "Cannot query empty segment tree" }
        require(start <= end) { "Start index $start is greater than end index $end" }
        if (start < 0) throw IndexOutOfBoundsException("Start index $start is negative. Valid range: [0, ${size - 1}]")
        if (end >= size) throw IndexOutOfBoundsException("End index $end is out of bounds. Valid range: [0, ${size - 1}]")
        return queryRange(1, 0, size - 1, start, end)
            ?: error("Query returned null for valid range [$start, $end]")
    }

    /**
     * Queries the value at a single [index] position.
     *
     * This is a convenience operator function that allows using bracket notation with a single index
     * to query individual elements. It is equivalent to calling `get(index, index)`.
     *
     * @param index the index of the element to query; must be in range [0, [size])
     *
     * @return the value at the specified [index]
     *
     * @throws IllegalArgumentException if the tree is empty
     * @throws IndexOutOfBoundsException if [index] is out of bounds
     */
    public operator fun get(index: Int): T = get(index, index)

    /**
     * Queries the result of applying the operation over the range [[start], [end]] (inclusive),
     * returning null if the indices are invalid.
     *
     * This is a safe version of [get] that returns null instead of throwing an exception
     * when the indices are out of bounds or invalid.
     *
     * @param start the starting index of the range (inclusive)
     * @param end the ending index of the range (inclusive)
     *
     * @return the result of applying the operation to all elements in the range, or null if
     *         [start] < 0, [end] >= [size], or [start] > [end]
     *
     * @sample samples.SegmentTreeSamples.safeQueryMethods
     */
    public fun getOrNull(start: Int, end: Int): T? {
        if (start < 0 || end >= size || start > end) return null
        return queryRange(1, 0, size - 1, start, end)
    }

    /**
     * Queries the result of applying the operation over the range [[start], [end]] (inclusive),
     * returning a [defaultValue] if the indices are invalid.
     *
     * This is a safe version of [get] that returns a [defaultValue] instead of throwing an exception
     * when the indices are out of bounds or invalid.
     *
     * @param start the starting index of the range (inclusive)
     * @param end the ending index of the range (inclusive)
     * @param defaultValue the value to return if the indices are invalid
     *
     * @return the result of applying the operation to all elements in the range, or [defaultValue] if
     *         [start] < 0, [end] >= [size], or [start] > [end]
     *
     * @sample samples.SegmentTreeSamples.safeQueryMethods
     */
    public fun getOrDefault(start: Int, end: Int, defaultValue: T): T {
        return getOrNull(start, end) ?: defaultValue
    }

    /**
     * Updates the value at the specified [index] to a [value].
     *
     * This operator function allows using assignment syntax to update elements.
     * The operation updates a single element in the segment tree and propagates
     * the change up through all affected nodes in O(log n) time.
     *
     * Since both [get] and [set] operators are defined, compound assignment operators
     * (`+=`, `-=`, `*=`, `/=`, `%=`) work automatically by combining get and set operations.
     *
     * @param index the index of the element to update; must be in range [0, [size])
     * @param value the new value to set at the specified index
     *
     * @throws IllegalArgumentException if the tree is empty
     * @throws IndexOutOfBoundsException if [index] is out of bounds
     *
     * @sample samples.SegmentTreeSamples.competitiveProgrammingScenario
     * @sample samples.SegmentTreeSamples.compoundAssignmentOperators
     */
    public operator fun set(index: Int, value: T) {
        require(size > 0) { "Cannot update empty segment tree" }
        if (index !in 0..<size) throw IndexOutOfBoundsException("Index $index is out of bounds. Valid range: [0, ${size - 1}]")
        update(1, 0, size - 1, index, value)
    }

    private fun build(source: List<T>, nodeIndex: Int = 1, start: Int = 0, end: Int = size - 1) {
        if (start == end) {
            tree[nodeIndex] = source[start]
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

    private fun update(nodeIndex: Int, start: Int, end: Int, index: Int, newValue: T) {
        if (start == end) {
            tree[nodeIndex] = newValue
        } else {
            val mid = start + (end - start) / 2
            val leftNodeIndex = nodeIndex * 2
            val rightNodeIndex = nodeIndex * 2 + 1

            if (index <= mid) {
                update(leftNodeIndex, start, mid, index, newValue)
            } else {
                update(rightNodeIndex, mid + 1, end, index, newValue)
            }

            tree[nodeIndex] = operation(tree[leftNodeIndex]!!, tree[rightNodeIndex]!!)
        }
    }
}
