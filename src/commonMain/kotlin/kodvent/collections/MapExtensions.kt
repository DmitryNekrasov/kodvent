/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.collections

/**
 * Increments the count for [key] by 1, treating a missing [key] as 0.
 *
 * @sample samples.IncrementAndDecrementSamples.incrementBasicUsage
 * @sample samples.IncrementAndDecrementSamples.incrementCountingWords
 */
public fun <T> MutableMap<T, Int>.increment(key: T) {
    this[key] = (this[key] ?: 0) + 1
}

/**
 * Decrements the count for [key] by 1, removing it when the count reaches 0. Returns `true` if [key] was
 * present, `false` otherwise.
 *
 * @sample samples.IncrementAndDecrementSamples.decrementBasicUsage
 * @sample samples.IncrementAndDecrementSamples.decrementInventoryManagement
 */
@IgnorableReturnValue
public fun <T> MutableMap<T, Int>.decrement(key: T): Boolean {
    val count = this[key] ?: return false
    if (count > 1) {
        this[key] = count - 1
    } else {
        remove(key)
    }
    return true
}
