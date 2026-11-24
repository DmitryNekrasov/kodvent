/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.strings

/**
 * Returns the [prefix function](https://cp-algorithms.com/string/prefix-function.html) of this character
 * sequence. Element `i` of the result is the length of the longest proper prefix of `this[0..i]` that is also
 * its suffix. O(n) time and space.
 *
 * @sample samples.PrefixFunctionAndKMPSamples.prefixFunctionBasicUsage
 *
 * @see CharSequence.allIndicesOf
 */
public fun CharSequence.prefixFunction(): IntArray = prefixFunction(length, ::get)

/**
 * Returns the [prefix function](https://cp-algorithms.com/string/prefix-function.html) of the [length]
 * elements given by [at], which are compared with `==`. Element `i` of the result is the length of the
 * longest proper prefix of elements `0..i` that is also their suffix. O(n) time and space.
 *
 * @throws IllegalArgumentException if [length] is negative
 *
 * @sample samples.PrefixFunctionAndKMPSamples.genericPrefixFunctionWithIntegers
 */
public inline fun <T> prefixFunction(length: Int, at: (Int) -> T): IntArray {
    require(length >= 0) { "Length must not be negative, but was $length" }
    val pi = IntArray(length)
    for (i in 1..<length) {
        var j = pi[i - 1]
        while (j > 0 && at(i) != at(j)) j = pi[j - 1]
        pi[i] = if (at(i) == at(j)) j + 1 else j
    }
    return pi
}

/**
 * Returns the start index of every occurrence of [needle] in this character sequence, overlapping ones
 * included, in ascending order. An empty [needle] is found at every index in `0..<length`. O(n + m).
 *
 * @sample samples.PrefixFunctionAndKMPSamples.allIndicesOfBasicUsage
 */
public fun CharSequence.allIndicesOf(needle: CharSequence): List<Int> {
    if (needle.isEmpty()) return (0..<length).toList()

    val pi = needle.prefixFunction()
    val result = mutableListOf<Int>()
    var j = 0
    for (i in indices) {
        while (j > 0 && this[i] != needle[j]) j = pi[j - 1]
        if (this[i] == needle[j]) j++
        if (j == needle.length) {
            result.add(i - j + 1)
            j = pi[j - 1]
        }
    }
    return result
}
