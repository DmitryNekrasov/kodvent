/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqrTest {

    @Test
    fun `sqr should return correct result for positive integers`() {
        assertEquals(1, 1.sqr())
        assertEquals(4, 2.sqr())
        assertEquals(9, 3.sqr())
        assertEquals(16, 4.sqr())
        assertEquals(25, 5.sqr())
        assertEquals(100, 10.sqr())
        assertEquals(144, 12.sqr())
        assertEquals(10000, 100.sqr())
    }

    @Test
    fun `sqr should return zero for zero int`() {
        assertEquals(0, 0.sqr())
    }

    @Test
    fun `sqr should return correct result for negative integers`() {
        assertEquals(1, (-1).sqr())
        assertEquals(4, (-2).sqr())
        assertEquals(9, (-3).sqr())
        assertEquals(25, (-5).sqr())
        assertEquals(100, (-10).sqr())
        assertEquals(10000, (-100).sqr())
    }

    @Test
    fun `sqr should handle large positive integers`() {
        assertEquals(1000000, 1000.sqr())
        assertEquals(4000000, 2000.sqr())
        assertEquals(250000, 500.sqr())
        assertEquals(22500, 150.sqr())
    }

    @Test
    fun `sqr should handle large negative integers`() {
        assertEquals(1000000, (-1000).sqr())
        assertEquals(4000000, (-2000).sqr())
        assertEquals(250000, (-500).sqr())
        assertEquals(22500, (-150).sqr())
    }

    @Test
    fun `sqr should return correct result for positive longs`() {
        assertEquals(1L, 1L.sqr())
        assertEquals(4L, 2L.sqr())
        assertEquals(9L, 3L.sqr())
        assertEquals(25L, 5L.sqr())
        assertEquals(100L, 10L.sqr())
        assertEquals(10000L, 100L.sqr())
        assertEquals(1000000L, 1000L.sqr())
    }

    @Test
    fun `sqr should return zero for zero long`() {
        assertEquals(0L, 0L.sqr())
    }

    @Test
    fun `sqr should return correct result for negative longs`() {
        assertEquals(1L, (-1L).sqr())
        assertEquals(4L, (-2L).sqr())
        assertEquals(9L, (-3L).sqr())
        assertEquals(25L, (-5L).sqr())
        assertEquals(100L, (-10L).sqr())
        assertEquals(10000L, (-100L).sqr())
    }

    @Test
    fun `sqr should handle very large positive longs`() {
        assertEquals(1000000000000L, 1000000L.sqr())
        assertEquals(1000000000000000000L, 1000000000L.sqr())
        assertEquals(4000000000000L, 2000000L.sqr())
    }

    @Test
    fun `sqr should handle very large negative longs`() {
        assertEquals(1000000000000L, (-1000000L).sqr())
        assertEquals(1000000000000000000L, (-1000000000L).sqr())
        assertEquals(4000000000000L, (-2000000L).sqr())
    }

    @Test
    fun `sqr should return correct result for positive doubles`() {
        assertEquals(1.0, 1.0.sqr())
        assertEquals(4.0, 2.0.sqr())
        assertEquals(9.0, 3.0.sqr())
        assertEquals(25.0, 5.0.sqr())
        assertEquals(100.0, 10.0.sqr())
        assertEquals(10000.0, 100.0.sqr())
    }

    @Test
    fun `sqr should return zero for zero double`() {
        assertEquals(0.0, 0.0.sqr())
        assertEquals(0.0, (-0.0).sqr())
    }

    @Test
    fun `sqr should return correct result for negative doubles`() {
        assertEquals(1.0, (-1.0).sqr())
        assertEquals(4.0, (-2.0).sqr())
        assertEquals(9.0, (-3.0).sqr())
        assertEquals(25.0, (-5.0).sqr())
        assertEquals(100.0, (-10.0).sqr())
    }

    @Test
    fun `sqr should handle decimal values`() {
        assertEquals(0.25, 0.5.sqr())
        assertEquals(2.25, 1.5.sqr())
        assertEquals(6.25, 2.5.sqr())
        assertEquals(12.25, 3.5.sqr())
        assertEquals(0.0625, 0.25.sqr())
    }

    @Test
    fun `sqr should handle very small doubles`() {
        assertEquals(1e-10, 1e-5.sqr(), 1e-15)
        assertEquals(1e-20, 1e-10.sqr(), 1e-25)
        assertEquals(4e-6, 0.002.sqr(), 1e-10)
    }

    @Test
    fun `sqr should handle very large doubles`() {
        assertEquals(1e10, 1e5.sqr())
        assertEquals(1e20, 1e10.sqr())
        assertEquals(4e18, 2e9.sqr())
    }

    @Test
    fun `sqr should handle special double values`() {
        assertEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY.sqr())
        assertEquals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.sqr())
        assertTrue(Double.NaN.sqr().isNaN())
    }

    @Test
    fun `sqr should handle edge case double values`() {
        val minPositive = Double.MIN_VALUE
        assertEquals(0.0, minPositive.sqr())

        assertEquals(1.0, 1.0.sqr())
        assertEquals(0.9801, 0.99.sqr(), 1e-10)
        assertEquals(1.0201, 1.01.sqr(), 1e-10)
    }

    @Test
    fun `sqr should handle fractional values correctly`() {
        assertEquals(0.09, 0.3.sqr(), 1e-10)
        assertEquals(0.0001, 0.01.sqr(), 1e-10)
        assertEquals(2.7889, 1.67.sqr(), 1e-10)
        assertEquals(9.8596, 3.14.sqr(), 1e-10)
    }
}
