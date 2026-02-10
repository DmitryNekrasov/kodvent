/*
 * Copyright 2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.boundsearch

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TernarySearchTest {

    private val tolerance = 1e-6

    private fun assertApprox(expected: Double, actual: Double, message: String? = null) {
        assertTrue(abs(expected - actual) < tolerance, message ?: "Expected $expected but got $actual")
    }

    @Test
    fun `throws when left is greater than right`() {
        assertFailsWith<IllegalArgumentException> { ternarySearch(10.0, 5.0) { it } }
    }

    @Test
    fun `degenerate range where left equals right`() {
        val result = ternarySearch(3.0, 3.0) { it * it }
        assertApprox(3.0, result)
    }

    @Test
    fun `finds maximum of negated quadratic`() {
        val result = ternarySearch(0.0, 10.0) { x -> -(x - 5) * (x - 5) + 10 }
        assertApprox(5.0, result)
    }

    @Test
    fun `maximum at left boundary`() {
        val result = ternarySearch(0.0, 10.0) { x -> -x }
        assertApprox(0.0, result)
    }

    @Test
    fun `maximum at right boundary`() {
        val result = ternarySearch(0.0, 10.0) { x -> x }
        assertApprox(10.0, result)
    }

    @Test
    fun `finds maximum of sine function on 0 to pi`() {
        val result = ternarySearch(0.0, PI) { x -> sin(x) }
        assertApprox(PI / 2, result)
    }

    @Test
    fun `finds maximum of cosine function on negative pi to pi`() {
        val result = ternarySearch(-PI, PI) { x -> cos(x) }
        assertApprox(0.0, result)
    }

    @Test
    fun `asymmetric quadratic`() {
        val result = ternarySearch(0.0, 100.0) { x -> -(x - 3) * (x - 3) }
        assertApprox(3.0, result)
    }

    @Test
    fun `negative range`() {
        val result = ternarySearch(-10.0, 0.0) { x -> -(x + 5) * (x + 5) }
        assertApprox(-5.0, result)
    }

    @Test
    fun `very narrow range`() {
        val result = ternarySearch(1.0, 2.0) { x -> -(x - 1.5) * (x - 1.5) }
        assertApprox(1.5, result)
    }

    @Test
    fun `wide range with off-center maximum`() {
        val result = ternarySearch(-1000.0, 1000.0) { x -> -(x - 999) * (x - 999) }
        assertApprox(999.0, result)
    }

    @Test
    fun `cubic-like unimodal function`() {
        val result = ternarySearch(0.0, 2.0) { x -> -x * x * x * x + 2 * x * x }
        assertApprox(1.0, result)
    }

    @Test
    fun `result produces maximum function value`() {
        val f = { x: Double -> -(x - 7.0) * (x - 7.0) + 42.0 }
        val result = ternarySearch(0.0, 15.0, f)
        val maxVal = f(result)
        assertApprox(42.0, maxVal)
    }

    @Test
    fun `constant function returns a point in range`() {
        val result = ternarySearch(0.0, 10.0) { 5.0 }
        assertTrue(result in 0.0..10.0)
    }

    @Test
    fun `fractional maximum`() {
        val peak = 0.123456789
        val result = ternarySearch(-1.0, 1.0) { x -> -(x - peak) * (x - peak) }
        assertApprox(peak, result)
    }

    @Test
    fun `large values in function`() {
        val result = ternarySearch(0.0, 100.0) { x -> -1e12 * (x - 50) * (x - 50) + 1e15 }
        assertApprox(50.0, result)
    }
}
