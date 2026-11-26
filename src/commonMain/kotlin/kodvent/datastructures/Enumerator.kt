/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * Gives distinct values consecutive numbers from 0, in the order in which [get] first sees them, so that
 * names from the input can be indices of an array. Values are compared as the keys of a map, so they need
 * stable `equals` and `hashCode`, and an array is compared by identity.
 *
 * @sample samples.EnumeratorSamples.basicUsage
 * @sample samples.EnumeratorSamples.namesInDisjointSetUnion
 */
public class Enumerator<T> {
    private val cache = mutableMapOf<T, Int>()

    /**
     * The number of distinct values that [get] has seen, so their numbers are `0..<size`. O(1).
     *
     * @sample samples.EnumeratorSamples.namesInDisjointSetUnion
     */
    public val size: Int
        get() = cache.size

    /**
     * Returns the number given to [x], numbering [x] first if it is new. O(1) amortized.
     *
     * @sample samples.EnumeratorSamples.basicUsage
     */
    public operator fun get(x: T): Int = cache.getOrPut(x) { cache.size }
}
