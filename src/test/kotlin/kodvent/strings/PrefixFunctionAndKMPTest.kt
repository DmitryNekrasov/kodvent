/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.strings

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PrefixFunctionAndKMPTest {

    // Tests for CharSequence.prefixFunction()

    @Test
    fun `prefixFunction on empty string returns empty array`() {
        val result = "".prefixFunction()
        assertContentEquals(intArrayOf(), result)
    }

    @Test
    fun `prefixFunction on single character returns zero`() {
        val result = "a".prefixFunction()
        assertContentEquals(intArrayOf(0), result)
    }

    @Test
    fun `prefixFunction on string with no repeating patterns`() {
        val result = "abcdef".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 0, 0, 0, 0), result)
    }

    @Test
    fun `prefixFunction on string with all same characters`() {
        val result = "aaaa".prefixFunction()
        assertContentEquals(intArrayOf(0, 1, 2, 3), result)
    }

    @Test
    fun `prefixFunction on alternating pattern`() {
        val result = "ababab".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 1, 2, 3, 4), result)
    }

    @Test
    fun `prefixFunction on complex pattern with partial matches`() {
        val result = "abcabc".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 0, 1, 2, 3), result)
    }

    @Test
    fun `prefixFunction on classic example aabaaab`() {
        val result = "aabaaab".prefixFunction()
        assertContentEquals(intArrayOf(0, 1, 0, 1, 2, 2, 3), result)
    }

    @Test
    fun `prefixFunction on pattern with prefix-suffix overlap`() {
        val result = "abacaba".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 1, 0, 1, 2, 3), result)
    }

    @Test
    fun `prefixFunction on pattern aabaabaa`() {
        val result = "aabaabaa".prefixFunction()
        assertContentEquals(intArrayOf(0, 1, 0, 1, 2, 3, 4, 5), result)
    }

    // Tests for generic prefixFunction(length, at)

    @Test
    fun `generic prefixFunction on list of integers`() {
        val list = listOf(1, 2, 1, 2, 1, 2)
        val result = prefixFunction(list.size) { list[it] }
        assertContentEquals(intArrayOf(0, 0, 1, 2, 3, 4), result)
    }

    @Test
    fun `generic prefixFunction on list with no pattern`() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = prefixFunction(list.size) { list[it] }
        assertContentEquals(intArrayOf(0, 0, 0, 0, 0), result)
    }

    @Test
    fun `generic prefixFunction on list of strings`() {
        val list = listOf("a", "b", "a", "b")
        val result = prefixFunction(list.size) { list[it] }
        assertContentEquals(intArrayOf(0, 0, 1, 2), result)
    }

    @Test
    fun `generic prefixFunction on empty sequence`() {
        val result = prefixFunction(0) { throw IllegalStateException("Should not be called") }
        assertContentEquals(intArrayOf(), result)
    }

    // Tests for CharSequence.allIndicesOf()

    @Test
    fun `allIndicesOf returns empty list when needle not found`() {
        val result = "abcdef".allIndicesOf("xyz")
        assertEquals(emptyList(), result)
    }

    @Test
    fun `allIndicesOf finds single occurrence`() {
        val result = "hello world".allIndicesOf("world")
        assertEquals(listOf(6), result)
    }

    @Test
    fun `allIndicesOf finds multiple non-overlapping occurrences`() {
        val result = "abc abc abc".allIndicesOf("abc")
        assertEquals(listOf(0, 4, 8), result)
    }

    @Test
    fun `allIndicesOf finds overlapping occurrences`() {
        val result = "aaaa".allIndicesOf("aa")
        assertEquals(listOf(0, 1, 2), result)
    }

    @Test
    fun `allIndicesOf finds needle at the beginning`() {
        val result = "abcdef".allIndicesOf("abc")
        assertEquals(listOf(0), result)
    }

    @Test
    fun `allIndicesOf finds needle at the end`() {
        val result = "abcdef".allIndicesOf("def")
        assertEquals(listOf(3), result)
    }

    @Test
    fun `allIndicesOf when needle equals the whole string`() {
        val result = "test".allIndicesOf("test")
        assertEquals(listOf(0), result)
    }

    @Test
    fun `allIndicesOf returns empty list when needle is longer than text`() {
        val result = "abc".allIndicesOf("abcdef")
        assertEquals(emptyList(), result)
    }

    @Test
    fun `allIndicesOf with single character needle`() {
        val result = "banana".allIndicesOf("a")
        assertEquals(listOf(1, 3, 5), result)
    }

    @Test
    fun `allIndicesOf with repeated pattern`() {
        val result = "abababab".allIndicesOf("abab")
        assertEquals(listOf(0, 2, 4), result)
    }

    @Test
    fun `allIndicesOf on empty text returns empty list`() {
        val result = "".allIndicesOf("abc")
        assertEquals(emptyList(), result)
    }

    @Test
    fun `allIndicesOf with empty needle returns indices for all positions`() {
        // When the needle is empty, the algorithm returns all positions in the text
        // This is the actual behavior of the KMP algorithm for empty patterns
        val result = "abcdef".allIndicesOf("")
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
    }

    @Test
    fun `allIndicesOf with custom delimiter`() {
        val result = "abc#def#abc".allIndicesOf("abc", delimiter = '$')
        assertEquals(listOf(0, 8), result)
    }

    @Test
    fun `allIndicesOf handles text containing default delimiter`() {
        val result = "a#b#c#a#b".allIndicesOf("a#b", delimiter = '$')
        assertEquals(listOf(0, 6), result)
    }

    @Test
    fun `allIndicesOf complex case with multiple overlapping matches`() {
        val result = "aabaabaabaab".allIndicesOf("aabaab")
        assertEquals(listOf(0, 3, 6), result)
    }
}
