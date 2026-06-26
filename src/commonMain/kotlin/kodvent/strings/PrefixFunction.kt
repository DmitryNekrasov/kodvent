/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.strings

/**
 * Computes the prefix function for this character sequence, returning an array whose i-th element is
 * the length of the longest proper prefix of `this[0..i]` that is also a suffix of `this[0..i]`.
 *
 * The prefix function π_i is defined as the length of the longest proper prefix
 * of the substring `s[0..i]` that is also a suffix of this substring.
 * This is a fundamental component of the
 * [Knuth-Morris-Pratt (KMP) string matching algorithm](https://cp-algorithms.com/string/prefix-function.html).
 *
 * ## Complexity
 * - Time: O(n) where n is the length of the character sequence
 * - Space: O(n) for the result array
 *
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionBasicUsage
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionUnderstandingTheResult
 *
 * @see CharSequence.allIndicesOf
 */
public fun CharSequence.prefixFunction(): IntArray = prefixFunction(length, ::get)

/**
 * Computes the prefix function for a generic sequence of [length] elements of type [T], where [at]
 * returns the element at a given index. Returns an array whose i-th element is the length of the
 * longest proper prefix that is also a suffix for the subsequence `[0..i]`.
 *
 * This is a generalized version of the prefix function that works with any type of sequence
 * where elements can be accessed by index and compared for equality. The prefix function π_i
 * represents the length of the longest proper prefix that is also a suffix for the sequence up to position i.
 *
 * ## Complexity
 * - Time: O(n) where n is the length parameter
 * - Space: O(n) for the result array
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
 * It concatenates the needle, the [delimiter] character, and the text, then uses the prefix function to
 * identify matches. The [delimiter] (default `'#'`) must not appear in either the needle or the text.
 *
 * ## Complexity
 * - Time: O(n + m) where n is the length of this sequence and m is the length of the needle
 * - Space: O(n + m) for the concatenated string and prefix array
 *
 * @throws IllegalArgumentException if the delimiter appears in the needle or in this text
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
    if (needle.isEmpty()) return (0..<length).toList()

    require(delimiter !in needle) { "Delimiter '$delimiter' must not appear in the needle string" }
    require(delimiter !in this) { "Delimiter '$delimiter' must not appear in the text" }

    val pi = "$needle$delimiter$this".prefixFunction()
    val result = mutableListOf<Int>()
    for (i in (needle.length + 1)..pi.lastIndex) {
        if (pi[i] == needle.length) {
            result.add(i - 2 * needle.length)
        }
    }
    return result
}
