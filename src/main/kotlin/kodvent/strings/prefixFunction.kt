/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.strings

/**
 * Computes the prefix function for this character sequence.
 *
 * The prefix function π[i] is defined as the length of the longest proper prefix
 * of the substring s[0..i] that is also a suffix of this substring.
 * This is a fundamental component of the
 * [Knuth-Morris-Pratt (KMP) string matching algorithm](https://cp-algorithms.com/string/prefix-function.html).
 *
 * Time complexity: O(n) where n is the length of the character sequence
 * Space complexity: O(n) for the result array
 *
 * @return an array where the i-th element contains the length of the longest proper prefix
 *         of this[0..i] that is also a suffix of this[0..i]
 *
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionBasicUsage
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionUnderstandingTheResult
 *
 * @see CharSequence.allIndicesOf
 */
public fun CharSequence.prefixFunction(): IntArray = prefixFunction(length, ::get)

/**
 * Computes the prefix function for a generic sequence.
 *
 * This is a generalized version of the prefix function that works with any type of sequence
 * where elements can be accessed by index and compared for equality. The prefix function π[i]
 * represents the length of the longest proper prefix that is also a suffix for the sequence up to position i.
 *
 * Time complexity: O(n) where n is the length parameter
 * Space complexity: O(n) for the result array
 *
 * @param T the type of elements in the sequence
 * @param length the length of the sequence
 * @param at a function that returns the element at a given index
 *
 * @return an array where the i-th element contains the length of the longest proper prefix
 *         that is also a suffix for the subsequence [0..i]
 *
 * @sample samples.PrefixFunctionAndKMPSamples.genericPrefixFunctionWithIntegers
 * @sample samples.PrefixFunctionAndKMPSamples.genericPrefixFunctionWithCustomObjects
 *
 * @see CharSequence.allIndicesOf
 */
public inline fun <T> prefixFunction(length: Int, at: (Int) -> T): IntArray {
    val pi = IntArray(length)
    for (i in 1..<length) {
        var j = pi[i - 1]
        while (j > 0 && at(i) != at(j)) j = pi[j - 1]
        pi[i] = if (at(i) == at(j)) j + 1 else j
    }
    return pi
}

/**
 * Finds all starting indices where the [needle] substring occurs in this character sequence.
 *
 * This function uses the
 * [Knuth-Morris-Pratt (KMP) algorithm](https://cp-algorithms.com/string/prefix-function.html)
 * to efficiently find all occurrences of the needle string within the text.
 * It concatenates the needle, a delimiter, and the text, then uses the prefix function to identify matches.
 *
 * Time complexity: O(n + m) where n is the length of this sequence and m is the length of the needle
 * Space complexity: O(n + m) for the concatenated string and prefix array
 *
 * @param needle the substring to search for
 * @param delimiter a character used to separate the needle from the text in the internal representation.
 *                  Must not appear in either the needle or the text. Defaults to '#'.
 *
 * @return a list of all starting indices where the needle occurs in this character sequence
 *
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfBasicUsage
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfFindingOverlappingPatterns
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfCountingOccurrences
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfSearchInDNA
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfWithCustomDelimiter
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfRepeatedPatternDetection
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfEdgeCases
 * @sample samples.PrefixFunctionAndKMPSamples.combiningPrefixFunctionAndKMP
 *
 * @see prefixFunction
 */
public fun CharSequence.allIndicesOf(needle: CharSequence, delimiter: Char = '#'): List<Int> {
    val pi = "$needle$delimiter$this".prefixFunction()
    val result = mutableListOf<Int>()
    for (i in (needle.length + 1)..pi.lastIndex) {
        if (pi[i] == needle.length) {
            result.add(i - 2 * needle.length)
        }
    }
    return result
}
