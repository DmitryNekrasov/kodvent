/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PowTest {

    @Test
    fun `pow raises the base to the power`() {
        assertEquals(1024L, 2L.pow(10L))
        assertEquals(125L, 5L pow 3L)
        assertEquals(1_000_000L, 10L.pow(6L))
        assertEquals(1L, 999L.pow(0L))
        assertEquals(999L, 999L.pow(1L))
        assertEquals(0L, 0L.pow(5L))
        assertEquals(1L, 0L.pow(0L))
        assertEquals(1L, 1L.pow(5L))
        assertEquals(-8L, (-2L).pow(3L))
        assertEquals(16L, (-2L).pow(4L))
    }

    @Test
    fun `pow finishes for a power of 10 to the 18th`() {
        assertEquals(1L, 1L.pow(1_000_000_000_000_000_000L))
        assertEquals(-1L, (-1L).pow(1_000_000_000_000_000_001L))
    }

    @Test
    fun `pow wraps around when the result does not fit`() {
        assertEquals(Long.MIN_VALUE, 2L.pow(63L))
        assertEquals(0L, 2L.pow(64L))
    }

    @Test
    fun `pow with modulo returns the power reduced by the modulo`() {
        assertEquals(4L, 2L.pow(10L, 5L))
        assertEquals(4L, 3L.pow(4L, 7L))
        assertEquals(1L, 999L.pow(0L, 1000L))
        assertEquals(10L, 10L.pow(1L, 13L))
        assertEquals(0L, 0L.pow(5L, 7L))
        assertEquals(1L, 0L.pow(0L, 7L))
        assertEquals(1L, 1L.pow(100L, 7L))
        assertEquals(0L, 7L.pow(3L, 7L))
        assertEquals(1L, 3L.pow(5L, 2L))
        assertEquals(0L, 100L.pow(100L, 1L))
        assertEquals(0L, 5L.pow(0L, 1L))
        assertEquals(976_371_285L, 2L.pow(100L, 1_000_000_007L))
        assertEquals(3_037_000_499L, 3_037_000_499L.pow(3L, 3_037_000_500L))
    }

    @Test
    fun `pow with modulo agrees with repeated multiplication for small arguments`() {
        for (modulo in 1L..12L) {
            for (base in -12L..12L) {
                var expected = 1L.mod(modulo)
                for (power in 0L..12L) {
                    assertEquals(expected, base.pow(power, modulo))
                    expected = (expected * base).mod(modulo)
                }
            }
        }
    }

    @Test
    fun `pow with modulo reduces a base larger than the modulo`() {
        assertEquals(737_564_071L, Long.MAX_VALUE.pow(2L, 1_000_000_007L))
    }

    @Test
    fun `pow with modulo finishes for a power of 10 to the 18th`() {
        assertEquals(719_476_260L, 2L.pow(1_000_000_000_000_000_000L, 1_000_000_007L))
    }

    @Test
    fun `pow with modulo is exact for a modulo above 3037000500`() {
        val prime = 119_315_717_514_047L // the deck size of Advent of Code 2019 day 22
        assertEquals(1L, 2L.pow(prime - 1, prime))
        assertEquals(68_180_410_008_027L, 7L.pow(prime - 2, prime))
        assertEquals(prime - 8, (-2L).pow(3L, prime))
        assertEquals(4L, (-2L).pow(2L, prime))
        assertEquals(1L, 3_037_000_500L.pow(2L, 3_037_000_501L))
        assertEquals(1L, (Long.MAX_VALUE - 1).pow(2L, Long.MAX_VALUE))
        assertEquals(0L, (1L shl 31).pow(2L, 1L shl 62))
        // 2^62 is not a prime: with the power reduced modulo 2^62 - 1, as for a prime, the result would be 3
        assertEquals(1L, 3L.pow(1L shl 62, 1L shl 62))
    }

    @Test
    fun `pow with modulo is not negative for a negative base`() {
        assertEquals(2L, (-2L).pow(3L, 5L))
        assertEquals(2L, (-3L).pow(5L, 7L))
    }

    @Test
    fun `pow throws IllegalArgumentException for a negative power`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L) }
    }

    @Test
    fun `pow with modulo throws IllegalArgumentException for a negative power`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L, 5L) }
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L, 1L) }
    }

    @Test
    fun `pow with modulo throws IllegalArgumentException for a zero modulo`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(3L, 0L) }
    }

    @Test
    fun `pow with modulo throws IllegalArgumentException for a negative modulo`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(3L, -5L) }
    }
}
