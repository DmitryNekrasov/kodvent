/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.strings

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PrefixFunctionAndKMPTest {

    @Test
    fun `prefixFunction returns the longest border of every prefix`() {
        val expected = listOf(
            "" to intArrayOf(),
            "a" to intArrayOf(0),
            "abcdef" to intArrayOf(0, 0, 0, 0, 0, 0),
            "ababab" to intArrayOf(0, 0, 1, 2, 3, 4),
            "aabaabaa" to intArrayOf(0, 1, 0, 1, 2, 3, 4, 5),
            "aabaaab" to intArrayOf(0, 1, 0, 1, 2, 2, 3),
            "aA" to intArrayOf(0, 0),
        )
        for ((text, pi) in expected) assertContentEquals(pi, text.prefixFunction())
    }

    @Test
    fun `prefixFunction falls back through shorter borders`() {
        assertContentEquals(intArrayOf(0, 1, 2, 0), "aaab".prefixFunction())
        assertContentEquals(intArrayOf(0, 0, 1, 2, 0), "ababb".prefixFunction())
    }

    @Test
    fun `generic prefixFunction compares elements by equality and not by identity`() {
        val list = listOf(listOf(1), listOf(2), listOf(1), listOf(2))
        val result = prefixFunction(list.size) { list[it] }
        assertContentEquals(intArrayOf(0, 0, 1, 2), result)
    }

    @Test
    fun `generic prefixFunction throws when length is negative`() {
        assertFailsWith<IllegalArgumentException> { prefixFunction(-1) { 0 } }
    }

    @Test
    fun `generic prefixFunction does not call at when length is zero`() {
        assertContentEquals(intArrayOf(), prefixFunction(0) { error("at must not be called") })
    }

    @Test
    fun `allIndicesOf returns an empty list when the needle does not occur`() {
        for ((text, needle) in listOf("abcdef" to "xyz", "abc" to "abcdef", "" to "abc", "ABC" to "b")) {
            assertEquals(emptyList(), text.allIndicesOf(needle))
        }
    }

    @Test
    fun `allIndicesOf finds the needle at the beginning and at the end`() {
        assertEquals(listOf(0, 3), "abca".allIndicesOf("a"))
        assertEquals(listOf(0), "test".allIndicesOf("test"))
        assertEquals(listOf(0, 2), " a ".allIndicesOf(" "))
    }

    @Test
    fun `allIndicesOf finds an empty needle at every index before the length`() {
        assertEquals(listOf(0, 1, 2, 3, 4, 5), "abcdef".allIndicesOf(""))
        assertEquals(emptyList(), "".allIndicesOf(""))
    }

    @Test
    fun `allIndicesOf reports overlapping occurrences`() {
        assertEquals(listOf(0, 3, 6), "aabaabaabaab".allIndicesOf("aabaab"))
        assertEquals(listOf(0, 1, 2), "aaaaa".allIndicesOf("aaa"))
    }

    @Test
    fun `allIndicesOf falls back through shorter borders of the needle`() {
        assertEquals(listOf(1), "aaab".allIndicesOf("aab"))
        assertEquals(emptyList(), "ababbaba".allIndicesOf("ababa"))
        assertEquals(listOf(2, 3), "babbb".allIndicesOf("bb"))
    }

    @Test
    fun `allIndicesOf treats a hash like any other character`() {
        assertEquals(listOf(1, 2, 4), "#..#.#".allIndicesOf("."))
        assertEquals(listOf(0, 2), "#.#.#".allIndicesOf("#.#"))
    }

    private class Chars(text: String) : CharSequence by text {
        override fun toString(): String = "Chars"
    }

    @Test
    fun `allIndicesOf reads the characters of a CharSequence and not its toString`() {
        assertEquals(listOf(1), Chars("aaab").allIndicesOf("aab"))
        assertEquals(listOf(1), "aaab".allIndicesOf(Chars("aab")))
    }

    private class Counted(private val text: String) : CharSequence by text {
        var reads = 0

        override fun get(index: Int): Char {
            reads++
            return text[index]
        }
    }

    @Test
    fun `allIndicesOf reads a linear number of characters`() {
        val n = 4000
        val m = 400
        // The worst case of a naive search: every start matches m - 1 characters and fails on the last one
        val text = Counted("a".repeat(n))
        val needle = Counted("a".repeat(m - 1) + "b")

        assertEquals(emptyList(), text.allIndicesOf(needle))

        assertTrue(text.reads <= 4 * n)
        assertTrue(needle.reads <= 4 * (n + m)) // with the reads of its prefix function
    }
}
