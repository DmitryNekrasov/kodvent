/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
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
        // The last element is 3: "aab" is the longest proper prefix of "aabaaab" that is also its suffix
        assertContentEquals(intArrayOf(0, 1, 0, 1, 2, 2, 3), "aabaaab".prefixFunction())
    }

    @Test
    fun genericPrefixFunctionWithIntegers() {
        val numbers = listOf(1, 2, 1, 2, 1, 2)

        assertContentEquals(intArrayOf(0, 0, 1, 2, 3, 4), prefixFunction(numbers.size, numbers::get))
    }

    @Test
    fun allIndicesOfBasicUsage() {
        assertEquals(listOf(1, 3), "banana".allIndicesOf("ana")) // occurrences may overlap
        assertEquals(emptyList(), "banana".allIndicesOf("xyz"))
    }
}
