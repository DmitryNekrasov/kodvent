/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.search

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TernarySearchTest {

    private val tolerance = 1e-6

    @Test
    fun `ternarySearch throws IllegalArgumentException when left is greater than right`() {
        assertFailsWith<IllegalArgumentException> { ternarySearch(10.0, 5.0) { it } }
    }

    @Test
    fun `ternarySearch returns the bound when left equals right`() {
        val result = ternarySearch(3.0, 3.0) { it * it }
        assertEquals(3.0, result, tolerance)
    }

    @Test
    fun `ternarySearch finds a maximum at the left bound`() {
        val result = ternarySearch(0.0, 10.0) { x -> -x }
        assertEquals(0.0, result, tolerance)
    }

    @Test
    fun `ternarySearch finds a maximum at the right bound`() {
        val result = ternarySearch(0.0, 10.0) { x -> x }
        assertEquals(10.0, result, tolerance)
    }

    @Test
    fun `ternarySearch returns for a maximum far from zero`() {
        var calls = 0
        val result = ternarySearch(1e9, 2e9) { x ->
            assertTrue(++calls <= 200)
            -abs(x - 1.5e9)
        }
        assertEquals(1.5e9, result, tolerance)
    }

    @Test
    fun `ternarySearch returns a point between bounds near the largest Double`() {
        var calls = 0
        val nearTop = ternarySearch(1e308, 1.1e308) { x ->
            assertTrue(++calls <= 200)
            -abs(x - 1.05e308)
        }
        assertEquals(1.05e308, nearTop, absoluteTolerance = 1e294)

        val nearBottom = ternarySearch(-1.1e308, -1e308) { x -> -abs(x + 1.05e308) }
        assertEquals(-1.05e308, nearBottom, absoluteTolerance = 1e294)
    }

    @Test
    fun `ternarySearch finds a maximum inside the range`() {
        assertEquals(0.0, ternarySearch(-PI, PI) { x -> cos(x) }, tolerance)
        assertEquals(-5.0, ternarySearch(-10.0, 0.0) { x -> -(x + 5) * (x + 5) }, tolerance)
        assertEquals(999.0, ternarySearch(-1000.0, 1000.0) { x -> -(x - 999) * (x - 999) }, tolerance)
        assertEquals(1.0, ternarySearch(0.0, 2.0) { x -> -x * x * x * x + 2 * x * x }, tolerance)
        assertEquals(50.0, ternarySearch(0.0, 100.0) { x -> -1e12 * (x - 50) * (x - 50) + 1e15 }, tolerance)
    }

    @Test
    fun `ternarySearch finds a sharp maximum to within 1e-9`() {
        for (peak in 1..9) {
            assertEquals(peak.toDouble(), ternarySearch(0.0, 10.0) { x -> -abs(x - peak) }, absoluteTolerance = 1e-9)
        }
    }
}
