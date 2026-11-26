/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnumeratorTest {

    private class SameHash(val id: Int) {
        override fun equals(other: Any?): Boolean = other is SameHash && other.id == id

        override fun hashCode(): Int = 0
    }

    @Test
    fun `a new value gets the next number and a repeated value keeps its own`() {
        val enumerator = Enumerator<String>()

        val numbers = listOf("d", "a", "d", "c", "a", "a", "b", "d").map { enumerator[it] }

        assertEquals(listOf(0, 1, 0, 2, 1, 1, 3, 0), numbers)
    }

    @Test
    fun `equal values that are different objects have one number`() {
        val enumerator = Enumerator<List<Int>>()

        assertEquals(0, enumerator[listOf(1, 2)])
        assertEquals(1, enumerator[listOf(2, 1)])
        assertEquals(0, enumerator[mutableListOf(1, 2)])
    }

    @Test
    fun `values that are not equal have different numbers when their hash codes are the same`() {
        val enumerator = Enumerator<SameHash>()

        assertEquals(listOf(0, 1, 0, 2, 1), listOf(1, 2, 1, 3, 2).map { enumerator[SameHash(it)] })
    }

    @Test
    fun `an array is the same value only as itself`() {
        val enumerator = Enumerator<IntArray>()
        val array = intArrayOf(1, 2)

        assertEquals(0, enumerator[array])
        assertEquals(1, enumerator[intArrayOf(1, 2)])
        assertEquals(0, enumerator[array])
    }

    @Test
    fun `null is a value like any other`() {
        val enumerator = Enumerator<String?>()

        assertEquals(listOf(0, 1, 0, 2, 1), listOf("a", null, "a", "b", null).map { enumerator[it] })
        assertEquals(3, enumerator.size)
    }

    @Test
    fun `size counts the distinct values seen so far`() {
        val enumerator = Enumerator<String>()
        assertEquals(0, enumerator.size)

        val numbersAndSizes = listOf("a", "b", "a", "c", "b").map { enumerator[it] to enumerator.size }

        assertEquals(listOf(0 to 1, 1 to 2, 0 to 2, 2 to 3, 1 to 3), numbersAndSizes)
    }

    @Test
    fun `get compares values a constant number of times per call`() {
        var comparisons = 0

        class Counted(val id: Int) {
            override fun equals(other: Any?): Boolean {
                comparisons++
                return other is Counted && other.id == id
            }

            override fun hashCode(): Int = id
        }

        val n = 4096
        val enumerator = Enumerator<Counted>()

        // New values, then the same values again
        repeat(2) { assertEquals((0..<n).toList(), List(n) { enumerator[Counted(it)] }) }

        // Measured: 4096 comparisons on the JVM and 4150 on JS, Wasm and Native. A search through a list of the
        // seen values takes n * n, about 16.8 million.
        assertTrue(comparisons < 8 * n)
    }

    @Test
    fun `every enumerator numbers from 0 on its own`() {
        val first = Enumerator<Int>()
        val second = Enumerator<Int>()

        assertEquals(listOf(0, 1), listOf(7, 8).map { first[it] })
        assertEquals(listOf(0, 1), listOf(8, 7).map { second[it] })
        assertEquals(1, first[8])
    }
}
