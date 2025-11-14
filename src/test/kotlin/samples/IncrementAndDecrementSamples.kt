/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.decrement
import kodvent.increment
import kotlin.test.Test
import kotlin.test.assertEquals

class IncrementAndDecrementSamples {

    @Test
    fun incrementBasicUsage() {
        val wordCounts = mutableMapOf<String, Int>()

        // Adding a new key starts at 1
        wordCounts.increment("hello")
        assertEquals(1, wordCounts["hello"])

        // Incrementing again increases the count
        wordCounts.increment("hello")
        assertEquals(2, wordCounts["hello"])

        // Different keys are tracked independently
        wordCounts.increment("world")
        assertEquals(1, wordCounts["world"])
        assertEquals(2, wordCounts["hello"])
    }

    @Test
    fun incrementCountingWords() {
        val text = "the quick brown fox jumps over the lazy dog"
        val wordFrequency = mutableMapOf<String, Int>()

        text.split(" ").forEach { word ->
            wordFrequency.increment(word)
        }

        assertEquals(2, wordFrequency["the"])
        assertEquals(1, wordFrequency["quick"])
        assertEquals(1, wordFrequency["fox"])
    }

    @Test
    fun decrementBasicUsage() {
        val inventory = mutableMapOf("apple" to 3, "banana" to 1)

        // Decrement reduces the count
        inventory.decrement("apple")
        assertEquals(2, inventory["apple"])

        // Decrement returns true when the key exists
        val removed = inventory.decrement("banana")
        assertEquals(true, removed)

        // When the count reaches 0, the key is removed
        assertEquals(false, inventory.containsKey("banana"))

        // Decrement returns false for non-existent keys
        val found = inventory.decrement("orange")
        assertEquals(false, found)
    }

    @Test
    fun decrementInventoryManagement() {
        val stock = mutableMapOf("laptop" to 5, "mouse" to 10, "keyboard" to 3)

        // Sell 3 laptops
        repeat(3) { stock.decrement("laptop") }
        assertEquals(2, stock["laptop"])

        // Sell all keyboards
        repeat(3) { stock.decrement("keyboard") }
        assertEquals(false, stock.containsKey("keyboard"))

        // Try to sell a non-existent item
        val success = stock.decrement("monitor")
        assertEquals(false, success)
    }
}
