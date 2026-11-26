/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `null is an element like any other`() {
        val enumerator = Enumerator<Int?>()

        assertEquals(listOf(0, 1, 0, 2, 1), listOf(7, null, 7, 0, null).map { enumerator[it] })
    }
}
