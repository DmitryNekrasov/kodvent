/*
 * Copyright 2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.boundsearch.ternarySearch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertTrue

class TernarySearchSamples {

    private val tolerance = 1e-6

    private fun assertApprox(expected: Double, actual: Double) {
        assertTrue(abs(expected - actual) < tolerance, "Expected $expected but got $actual")
    }

    @Test
    fun maximizeQuadratic() {
        // Find the peak of a downward-opening parabola f(x) = -(x - 3)^2 + 9
        val x = ternarySearch(0.0, 10.0) { x -> -(x - 3) * (x - 3) + 9 }

        assertApprox(3.0, x)
    }

    @Test
    fun maximizeSinFunction() {
        // Find the maximum of sin(x) on [0, π], which occurs at x = π/2
        val x = ternarySearch(0.0, Math.PI) { x -> sin(x) }

        assertApprox(Math.PI / 2, x)
    }

    @Test
    fun minimizeByNegation() {
        // Ternary search finds the maximum, but we can minimize f(x)
        // by maximizing -f(x).
        // Find the x that minimizes f(x) = (x - 4)^2 + 1 on [0, 10]
        val x = ternarySearch(0.0, 10.0) { x -> -((x - 4) * (x - 4) + 1) }

        assertApprox(4.0, x)
    }

    @Test
    fun maximumAreaRectangleWithFixedPerimeter() {
        // A rectangle has a fixed perimeter P = 40.
        // Side lengths: x and (P/2 - x). Area = x * (20 - x).
        // Find x that maximizes area, with x in [0, 20].
        val perimeter = 40.0
        val halfP = perimeter / 2

        val x = ternarySearch(0.0, halfP) { x -> x * (halfP - x) }

        // Maximum area when the rectangle is a square: x = 10
        assertApprox(10.0, x)
    }
}
