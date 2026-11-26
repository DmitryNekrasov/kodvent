/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * Gives distinct values consecutive numbers from 0, in the order in which [get] first sees them. This turns
 * names from the input into indices of an array or into elements of a [DisjointSetUnion]. Values are
 * compared with `equals` and `hashCode`, as the keys of a map.
 *
 * @sample samples.EnumeratorSamples.basicUsage
 * @sample samples.EnumeratorSamples.namesInDisjointSetUnion
 */
public class Enumerator<T> {
    private val cache = mutableMapOf<T, Int>()

    /**
     * Returns the number of [x]: the one it already has, or the next free one if [x] is new. O(1).
     *
     * @sample samples.EnumeratorSamples.basicUsage
     */
    public operator fun get(x: T): Int = cache.getOrPut(x) { cache.size }
}
