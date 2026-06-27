/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.strings

/**
 * Computes the [prefix function](https://cp-algorithms.com/string/prefix-function.html) of this
 * character sequence: an array whose i-th element is the length of the longest proper prefix of
 * `this[0..i]` that is also a suffix of it. A building block of the Knuth-Morris-Pratt algorithm.
 * O(n) time and space.
 *
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionBasicUsage
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionUnderstandingTheResult
 *
 * @see CharSequence.allIndicesOf
 */
public fun CharSequence.prefixFunction(): IntArray = prefixFunction(length, ::get)

/**
 * Generic [prefix function](https://cp-algorithms.com/string/prefix-function.html) over [length] elements
 * of type [T] accessed by index via [at] (compared with `==`). Returns an array whose i-th element is the
 * length of the longest proper prefix that is also a suffix of the subsequence `[0..i]`. O(n) time and space.
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
 * Returns the starting indices of every occurrence of [needle] in this character sequence, using the
 * Knuth-Morris-Pratt algorithm (via the prefix function) in O(n + m) time. The [delimiter] (default `'#'`)
 * is used internally and must not appear in [needle] or in this sequence.
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
