/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.collections

/**
 * Increments the count for the specified key by 1.
 *
 * If the key does not exist in the map, it will be added with a value of 1.
 * If the key already exists, its value will be increased by 1.
 *
 * @param key The key whose count should be incremented
 *
 * @sample samples.IncrementAndDecrementSamples.incrementBasicUsage
 * @sample samples.IncrementAndDecrementSamples.incrementCountingWords
 */
public fun <T> MutableMap<T, Int>.increment(key: T) {
    this[key] = (this[key] ?: 0) + 1
}

/**
 * Decrements the count for the specified key by 1.
 *
 * If the key's count is greater than 1, it will be decreased by 1.
 * If the key's count is exactly 1, the key will be removed from the map.
 * If the key does not exist in the map, no changes are made.
 *
 * @param key The key whose count should be decremented
 *
 * @return `true` if the key existed and was decremented or removed, `false` if the key was not found
 *
 * @sample samples.IncrementAndDecrementSamples.decrementBasicUsage
 * @sample samples.IncrementAndDecrementSamples.decrementInventoryManagement
 */
public fun <T> MutableMap<T, Int>.decrement(key: T): Boolean {
    val count = this[key] ?: return false
    if (count > 1) {
        this[key] = count - 1
    } else {
        remove(key)
    }
    return true
}
