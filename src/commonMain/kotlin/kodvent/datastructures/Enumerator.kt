/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * Numbers distinct elements from 0, in the order in which they are first passed to [get]. With it values
 * like the names of vertices become indices of an array.
 *
 * An element is any value with stable `equals` and `hashCode`, but not an array, which compares by
 * identity.
 *
 * @sample samples.EnumeratorSamples.basicUsage
 * @sample samples.EnumeratorSamples.namedVertices
 */
public class Enumerator<T> {
    private val cache = mutableMapOf<T, Int>()

    /**
     * Returns the number given to [x]. A new [x] gets the next free number. O(1) on average.
     *
     * @sample samples.EnumeratorSamples.basicUsage
     */
    public operator fun get(x: T): Int = cache.getOrPut(x) { cache.size }
}
