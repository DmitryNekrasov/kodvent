/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.strings.allIndicesOf
import kodvent.strings.prefixFunction
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PrefixFunctionAndKMPSamples {

    @Test
    fun prefixFunctionBasicUsage() {
        // Compute the prefix function for a string
        // The prefix function π[i] is the length of the longest proper prefix
        // of s[0..i] that is also a suffix of s[0..i]
        val result1 = "abcabc".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 0, 1, 2, 3), result1)

        val result2 = "aaaa".prefixFunction()
        assertContentEquals(intArrayOf(0, 1, 2, 3), result2)

        val result3 = "abacaba".prefixFunction()
        assertContentEquals(intArrayOf(0, 0, 1, 0, 1, 2, 3), result3)
    }

    @Test
    fun prefixFunctionUnderstandingTheResult() {
        // For string "aabaaab", let's understand the prefix function values
        val text = "aabaaab"
        val pi = text.prefixFunction()

        // pi[0] = 0: "a" has no proper prefix
        assertEquals(0, pi[0])

        // pi[1] = 1: "aa" has the longest proper prefix-suffix "a" (length 1)
        assertEquals(1, pi[1])

        // pi[2] = 0: "aab" has no proper prefix that is also a suffix
        assertEquals(0, pi[2])

        // pi[3] = 1: "aaba" has proper prefix-suffix "a" (length 1)
        assertEquals(1, pi[3])

        // pi[4] = 2: "aabaa" has proper prefix-suffix "aa" (length 2)
        assertEquals(2, pi[4])

        // pi[5] = 2: "aabaaa" has proper prefix-suffix "aa" (length 2)
        assertEquals(2, pi[5])

        // pi[6] = 3: "aabaaab" has proper prefix-suffix "aab" (length 3)
        assertEquals(3, pi[6])
    }

    @Test
    fun genericPrefixFunctionWithIntegers() {
        // Use the generic prefix function with a list of integers
        val sequence = listOf(1, 2, 1, 2, 1, 2)
        val result = prefixFunction(sequence.size, sequence::get)

        // The pattern "1, 2" repeats three times
        assertContentEquals(intArrayOf(0, 0, 1, 2, 3, 4), result)
    }

    @Test
    fun genericPrefixFunctionWithCustomObjects() {
        // Use the generic prefix function with custom objects
        data class Point(val x: Int, val y: Int)

        val sequence = listOf(
            Point(1, 2),
            Point(3, 4),
            Point(1, 2),
            Point(3, 4)
        )

        val result = prefixFunction(sequence.size, sequence::get)

        // The pattern repeats at positions 2 and 3
        assertContentEquals(intArrayOf(0, 0, 1, 2), result)
    }

    @Test
    fun allIndicesOfBasicUsage() {
        // Find all occurrences of a substring in a text
        val text1 = "hello world"
        val result1 = text1.allIndicesOf("world")
        assertEquals(listOf(6), result1)

        val text2 = "abc abc abc"
        val result2 = text2.allIndicesOf("abc")
        assertEquals(listOf(0, 4, 8), result2)

        val text3 = "banana"
        val result3 = text3.allIndicesOf("ana")
        // Finds overlapping occurrences at positions 1 and 3
        assertEquals(listOf(1, 3), result3)
    }

    @Test
    fun allIndicesOfFindingOverlappingPatterns() {
        // KMP algorithm finds all occurrences, including overlapping ones
        val text = "aaaa"
        val pattern = "aa"

        val indices = text.allIndicesOf(pattern)

        // Finds "aa" at positions 0, 1, and 2
        assertEquals(listOf(0, 1, 2), indices)
    }

    @Test
    fun allIndicesOfCountingOccurrences() {
        // Count how many times a word appears in a sentence
        val sentence = "the quick brown fox jumps over the lazy dog"
        val wordToFind = "the"

        val occurrences = sentence.allIndicesOf(wordToFind)

        // "the" appears twice at positions 0 and 31
        assertEquals(2, occurrences.size)
        assertEquals(listOf(0, 31), occurrences)
    }

    @Test
    fun allIndicesOfSearchInDNA() {
        // Search for a DNA sequence pattern in a genome
        val genome = "ACGTACGTACGT"
        val pattern = "ACGT"

        val positions = genome.allIndicesOf(pattern)

        // Pattern appears at positions 0, 4, and 8
        assertEquals(listOf(0, 4, 8), positions)
    }

    @Test
    fun allIndicesOfWithCustomDelimiter() {
        // If your text or pattern contains '#', use a different delimiter
        val text = "a#b#c#a#b"
        val pattern = "a#b"

        // Use '$' as a delimiter since '#' appears in the text
        val result = text.allIndicesOf(pattern, delimiter = '$')

        assertEquals(listOf(0, 6), result)
    }

    @Test
    fun allIndicesOfRepeatedPatternDetection() {
        // Detect a repeated pattern in a string
        val text = "abababab"
        val pattern = "abab"

        val occurrences = text.allIndicesOf(pattern)

        // Overlapping occurrences at positions 0, 2, and 4
        assertEquals(listOf(0, 2, 4), occurrences)
        assertEquals(3, occurrences.size)
    }

    @Test
    fun allIndicesOfEdgeCases() {
        // Pattern not found returns an empty list
        val result1 = "hello world".allIndicesOf("xyz")
        assertEquals(emptyList(), result1)

        // Single character search
        val result2 = "banana".allIndicesOf("a")
        assertEquals(listOf(1, 3, 5), result2)

        // Pattern equals entire text
        val result3 = "test".allIndicesOf("test")
        assertEquals(listOf(0), result3)

        // Empty text returns an empty list
        val result4 = "".allIndicesOf("abc")
        assertEquals(emptyList(), result4)
    }

    @Test
    fun combiningPrefixFunctionAndKMP() {
        // Understanding how the prefix function powers the KMP algorithm
        val needle = "abc"
        val text = "xabcyabcz"

        // The algorithm concatenates needle + delimiter + text
        val combined = "$needle#$text"
        val pi = combined.prefixFunction()

        // When pi[i] == needle.length, we found a match
        val matches = mutableListOf<Int>()
        for (i in (needle.length + 1)..pi.lastIndex) {
            if (pi[i] == needle.length) {
                // Convert from combined string index to text index
                matches.add(i - 2 * needle.length)
            }
        }

        // This is essentially what allIndicesOf does internally
        assertEquals(listOf(1, 5), matches)
        assertEquals(matches, text.allIndicesOf(needle))
    }
}
