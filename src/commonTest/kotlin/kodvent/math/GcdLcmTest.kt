/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals

class GcdLcmTest {

    @Test
    fun `gcd of ints is the greatest common divisor of the absolute values`() {
        assertEquals(6, gcd(48, 18))
        assertEquals(6, gcd(18, 48))
        assertEquals(1, gcd(17, 19))
        assertEquals(12, gcd(12, 36))
        assertEquals(7, gcd(7, 7))
        assertEquals(5, gcd(5, 0))
        assertEquals(5, gcd(0, 5))
        assertEquals(0, gcd(0, 0))
        assertEquals(2, gcd(4, -6))
        assertEquals(2, gcd(-4, 6))
        assertEquals(2, gcd(-4, -6))
        assertEquals(5, gcd(-5, 0))
        assertEquals(5, gcd(0, -5))
    }

    @Test
    fun `gcd of longs is the greatest common divisor of the absolute values`() {
        assertEquals(6L, gcd(48L, 18L))
        assertEquals(100_000L, gcd(10_000_000_000L, 300_000L))
        assertEquals(100_000L, gcd(300_000L, 10_000_000_000L))
        assertEquals(1_000_000_000L, gcd(7_000_000_000L, 10_000_000_000L))
        assertEquals(1L, gcd(1_000_000_007L, 1_000_000_009L))
        assertEquals(5L, gcd(5L, 0L))
        assertEquals(5L, gcd(0L, 5L))
        assertEquals(0L, gcd(0L, 0L))
        assertEquals(2L, gcd(4L, -6L))
        assertEquals(2L, gcd(-4L, 6L))
        assertEquals(2L, gcd(-4L, -6L))
        assertEquals(5L, gcd(-5L, 0L))
        assertEquals(5L, gcd(0L, -5L))
    }

    @Test
    fun `gcd is negative only when the result does not fit`() {
        assertEquals(2, gcd(Int.MIN_VALUE, 6))
        assertEquals(Int.MIN_VALUE, gcd(Int.MIN_VALUE, 0))
        assertEquals(2L, gcd(Long.MIN_VALUE, 6L))
        assertEquals(Long.MIN_VALUE, gcd(Long.MIN_VALUE, 0L))
    }

    @Test
    fun `lcm of ints is the least common multiple of the absolute values and 0 for a zero argument`() {
        assertEquals(24, lcm(8, 12))
        assertEquals(24, lcm(12, 8))
        assertEquals(323, lcm(17, 19))
        assertEquals(36, lcm(12, 36))
        assertEquals(7, lcm(7, 7))
        assertEquals(7, lcm(1, 7))
        assertEquals(7, lcm(7, 1))
        assertEquals(0, lcm(0, 5))
        assertEquals(0, lcm(5, 0))
        assertEquals(0, lcm(0, 0))
        assertEquals(12, lcm(4, -6))
        assertEquals(12, lcm(-4, 6))
        assertEquals(12, lcm(-4, -6))
        assertEquals(7, lcm(-7, -7))
    }

    @Test
    fun `lcm of longs is the least common multiple of the absolute values and 0 for a zero argument`() {
        assertEquals(24L, lcm(8L, 12L))
        assertEquals(30_000_000_000L, lcm(10_000_000_000L, 300_000L))
        assertEquals(7L, lcm(1L, 7L))
        assertEquals(7L, lcm(7L, 1L))
        assertEquals(0L, lcm(0L, 5L))
        assertEquals(0L, lcm(5L, 0L))
        assertEquals(0L, lcm(0L, 0L))
        assertEquals(12L, lcm(4L, -6L))
        assertEquals(12L, lcm(-4L, 6L))
        assertEquals(12L, lcm(-4L, -6L))
    }

    @Test
    fun `lcm divides before it multiplies`() {
        assertEquals(300_000, lcm(50_000, 60_000))
        assertEquals(12_000_000_000L, lcm(6_000_000_000L, 4_000_000_000L))
    }

    @Test
    fun `lcm wraps around when the result does not fit`() {
        assertEquals(-2_147_432_674, lcm(46_341, 46_342))
        assertEquals(-9_223_372_033_672_301_116L, lcm(3_037_000_500L, 3_037_000_501L))
    }
}
