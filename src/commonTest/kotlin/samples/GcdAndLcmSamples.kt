/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.math.gcd
import kodvent.math.lcm
import kotlin.test.Test
import kotlin.test.assertEquals

class GcdAndLcmSamples {

    @Test
    fun gcdBasicUsage() {
        // Find the greatest common divisor of two numbers
        val result1 = gcd(48, 18)
        assertEquals(6, result1)

        val result2 = gcd(15, 25)
        assertEquals(5, result2)

        // GCD of coprime numbers is 1
        val result3 = gcd(7, 13)
        assertEquals(1, result3)
    }

    @Test
    fun gcdSimplifyingFractions() {
        // Use GCD to simplify a fraction
        val numerator = 48
        val denominator = 180

        val divisor = gcd(numerator, denominator)
        val simplifiedNumerator = numerator / divisor
        val simplifiedDenominator = denominator / divisor

        // 48/180 simplifies to 4/15
        assertEquals(4, simplifiedNumerator)
        assertEquals(15, simplifiedDenominator)
    }

    @Test
    fun gcdReducingRatios() {
        // Use GCD to reduce a ratio to its simplest form
        val width = 1920
        val height = 1080

        val divisor = gcd(width, height)
        val aspectWidth = width / divisor
        val aspectHeight = height / divisor

        // 1920:1080 reduces to 16:9
        assertEquals(16, aspectWidth)
        assertEquals(9, aspectHeight)
    }

    @Test
    fun lcmBasicUsage() {
        // Find the least common multiple of two numbers
        val result1 = lcm(8, 12)
        assertEquals(24, result1)

        val result2 = lcm(15, 20)
        assertEquals(60, result2)

        // LCM of coprime numbers is their product
        val result3 = lcm(7, 13)
        assertEquals(91, result3)
    }

    @Test
    fun lcmSchedulingProblem() {
        // Two buses arrive at a station: one every 12 minutes, another every 18 minutes
        // Find when they will arrive at the same time again
        val bus1Interval = 12
        val bus2Interval = 18

        val nextSimultaneousArrival = lcm(bus1Interval, bus2Interval)

        // They will both arrive after 36 minutes
        assertEquals(36, nextSimultaneousArrival)
    }
}
