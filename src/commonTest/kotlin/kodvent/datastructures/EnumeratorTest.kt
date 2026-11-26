/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals

class EnumeratorTest {

    private data class Device(val name: String)

    @Test
    fun `new elements get the numbers from 0 in the order of their first call`() {
        val enumerator = Enumerator<String>()

        val numbers = listOf("c", "a", "b", "a", "c", "d", "b").map { enumerator[it] }

        assertEquals(listOf(0, 1, 2, 1, 0, 3, 2), numbers)
    }

    @Test
    fun `equal elements that are different objects share a number`() {
        val enumerator = Enumerator<Any>()

        assertEquals(0, enumerator[Device("you")])
        assertEquals(1, enumerator[listOf(1, 2)])
        assertEquals(2, enumerator[Device("out")])
        assertEquals(3, enumerator[listOf(2, 1)])

        assertEquals(0, enumerator[Device("you")])
        assertEquals(1, enumerator[listOf(1, 2)])
    }

    @Test
    fun `null is an element like any other`() {
        val enumerator = Enumerator<String?>()

        assertEquals(0, enumerator["a"])
        assertEquals(1, enumerator[null])
        assertEquals(2, enumerator["b"])
        assertEquals(1, enumerator[null])
    }

    @Test
    fun `two enumerators number their elements independently`() {
        val first = Enumerator<String>()
        val second = Enumerator<String>()

        assertEquals(0, first["a"])
        assertEquals(1, first["b"])

        assertEquals(0, second["b"])
        assertEquals(1, second["a"])
        assertEquals(1, first["b"])
    }
}
