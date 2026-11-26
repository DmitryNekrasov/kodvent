/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnumeratorTest {

    @Test
    fun `new elements get the numbers from 0 in the order they come`() {
        val enumerator = Enumerator<String>()

        assertEquals(listOf(0, 1, 2, 3), listOf("d", "a", "c", "b").map { enumerator[it] })
    }

    @Test
    fun `a repeated element keeps its number and takes no new one`() {
        val enumerator = Enumerator<Char>()

        assertEquals(listOf(0, 1, 0, 0, 2, 1, 3, 2), "abaacbdc".map { enumerator[it] })
    }

    @Test
    fun `elements are compared by equals and not by identity`() {
        val enumerator = Enumerator<List<Int>>()

        assertEquals(0, enumerator[listOf(1, 2)])
        assertEquals(1, enumerator[listOf(2, 1)])
        assertEquals(0, enumerator[mutableListOf(1, 2)])
    }

    @Test
    fun `unequal elements with the same hash code get different numbers`() {
        val first = 0 to 31
        val second = 1 to 0
        assertEquals(first.hashCode(), second.hashCode())

        val enumerator = Enumerator<Pair<Int, Int>>()

        assertEquals(listOf(0, 1, 0, 1), listOf(first, second, first, second).map { enumerator[it] })
    }

    @Test
    fun `null is an element like any other`() {
        val enumerator = Enumerator<Int?>()

        assertEquals(listOf(0, 1, 0, 2, 1), listOf(7, null, 7, 0, null).map { enumerator[it] })
    }

    @Test
    fun `size grows with a new element and stays with a repeated one`() {
        val enumerator = Enumerator<String>()
        assertEquals(0, enumerator.size)

        assertEquals(0, enumerator["a"])
        assertEquals(1, enumerator.size)

        assertEquals(1, enumerator["b"])
        assertEquals(0, enumerator["a"])
        assertEquals(2, enumerator.size)
    }

    @Test
    fun `get takes a constant number of comparisons on average`() {
        val n = 1024
        var comparisons = 0

        class Key(val id: Int) {
            override fun equals(other: Any?): Boolean {
                comparisons++
                return other is Key && other.id == id
            }

            override fun hashCode(): Int = id
        }

        // A search through a list of the elements takes n * n comparisons for these 2n calls. The map takes 1024
        // on the JVM and 1078 on JS, Wasm and Native.
        val enumerator = Enumerator<Key>()
        repeat(2) { for (i in 0..<n) assertEquals(i, enumerator[Key(i)]) }

        assertTrue(comparisons < 2 * n)
    }
}
