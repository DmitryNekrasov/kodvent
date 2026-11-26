/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * Numbers distinct elements from 0 in the order in which it first sees them: `enumerator[x]` is the next
 * free number when `x` is new, and the number `x` already has otherwise. This turns names or other keys
 * into indices of an array or into elements of a [DisjointSetUnion].
 *
 * An element is any value with stable `equals` and `hashCode`, such as a string, a data class or a list,
 * but not an array, which compares by identity.
 *
 * @sample samples.EnumeratorSamples.basicUsage
 * @sample samples.EnumeratorSamples.namesInDisjointSetUnion
 */
public class Enumerator<T> {
    private val cache = mutableMapOf<T, Int>()

    /**
     * Returns the number of [x]. A new [x] gets the count of the elements seen before it. O(1).
     *
     * @sample samples.EnumeratorSamples.basicUsage
     */
    public operator fun get(x: T): Int = cache.getOrPut(x) { cache.size }
}
