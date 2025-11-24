/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.strings

public fun CharSequence.prefixFunction(): IntArray = prefixFunction(length, ::get)

public inline fun <T> prefixFunction(length: Int, at: (Int) -> T): IntArray {
    val pi = IntArray(length)
    for (i in 1..<length) {
        var j = pi[i - 1]
        while (j > 0 && at(i) != at(j)) j = pi[j - 1]
        pi[i] = if (at(i) == at(j)) j + 1 else j
    }
    return pi
}

public fun CharSequence.findAll(needle: CharSequence, delimiter: Char = '#'): List<Int> {
    val pi = "$needle$delimiter$this".prefixFunction()
    val result = mutableListOf<Int>()
    for (i in (needle.length + 1)..pi.lastIndex) {
        if (pi[i] == needle.length) {
            result.add(i - 2 * needle.length)
        }
    }
    return result
}
