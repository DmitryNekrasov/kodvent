/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PartitionPointTest {

    @Test
    fun `partitionPoint of Int finds every partition point of every small range`() {
        for (from in -4..4) {
            for (to in from..from + 8) {
                for (expected in from..to) {
                    assertEquals(expected, partitionPoint(from, to) { assertTrue(it in from..<to); it < expected })
                }
                assertEquals(to, partitionPoint(from, to) { true })
                assertEquals(from, partitionPoint(from, to) { false })
            }
        }
    }

    @Test
    fun `partitionPoint of Int needs at most 32 calls at the limits of Int`() {
        val cases = listOf(
            Triple(Int.MAX_VALUE - 100, Int.MAX_VALUE, Int.MAX_VALUE - 50),
            Triple(Int.MIN_VALUE, Int.MIN_VALUE + 100, Int.MIN_VALUE + 50),
            Triple(Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE),
            Triple(Int.MIN_VALUE, Int.MAX_VALUE, 5),
            Triple(Int.MIN_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
        )
        for ((from, to, expected) in cases) {
            var calls = 0
            val result = partitionPoint(from, to) {
                assertTrue(++calls <= 32)
                it < expected
            }
            assertEquals(expected, result)
        }
    }

    @Test
    fun `partitionPoint of Int throws IllegalArgumentException when fromIndex is greater than toIndex`() {
        assertFailsWith<IllegalArgumentException> { partitionPoint(10, 5) { _: Int -> true } }
    }

    @Test
    fun `partitionPoint of Long finds every partition point of every small range`() {
        for (from in -4L..4L) {
            for (to in from..from + 8) {
                for (expected in from..to) {
                    assertEquals(expected, partitionPoint(from, to) { assertTrue(it in from..<to); it < expected })
                }
                assertEquals(to, partitionPoint(from, to) { true })
                assertEquals(from, partitionPoint(from, to) { false })
            }
        }
    }

    @Test
    fun `partitionPoint of Long needs at most 64 calls beyond Int and at the limits of Long`() {
        val cases = listOf(
            Triple(0L, 10_000_000_000L, 5_000_000_000L),
            Triple(Long.MAX_VALUE - 1000L, Long.MAX_VALUE, Long.MAX_VALUE - 500L),
            Triple(Long.MIN_VALUE, Long.MIN_VALUE + 100L, Long.MIN_VALUE + 50L),
            Triple(Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE),
            Triple(Long.MIN_VALUE, Long.MAX_VALUE, 5L),
            Triple(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
        )
        for ((from, to, expected) in cases) {
            var calls = 0
            val result = partitionPoint(from, to) {
                assertTrue(++calls <= 64)
                it < expected
            }
            assertEquals(expected, result)
        }
    }

    @Test
    fun `partitionPoint of Long throws IllegalArgumentException when fromIndex is greater than toIndex`() {
        assertFailsWith<IllegalArgumentException> { partitionPoint(10L, 5L) { true } }
    }

    @Test
    fun `partitionPoint takes two literal bounds with a typed parameter or a Long bound`() {
        assertEquals(8, partitionPoint(0, 100) { i: Int -> i * i < 50 })
        assertEquals(8L, partitionPoint(0, 100) { i: Long -> i * i < 50 })
        assertEquals(8L, partitionPoint(0L, 100) { it * it < 50 })
    }
}
