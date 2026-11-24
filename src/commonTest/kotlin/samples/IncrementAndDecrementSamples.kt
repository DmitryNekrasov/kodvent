/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.collections.decrement
import kodvent.collections.increment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementAndDecrementSamples {

    @Test
    fun incrementCountingWords() {
        val counts = mutableMapOf<String, Int>()
        "the cat and the dog".split(" ").forEach { counts.increment(it) }

        assertEquals(mapOf("the" to 2, "cat" to 1, "and" to 1, "dog" to 1), counts)
    }

    @Test
    fun decrementBasicUsage() {
        val inventory = mutableMapOf("apple" to 2, "banana" to 1)

        inventory.decrement("apple") // 2 becomes 1
        assertTrue(inventory.decrement("banana")) // 1 becomes 0, so the key is removed
        assertFalse(inventory.decrement("orange")) // absent: nothing changes

        assertEquals(mapOf("apple" to 1), inventory)
    }
}
