/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.math.sqr
import kotlin.test.Test
import kotlin.test.assertEquals

class SqrSamples {

    @Test
    fun sqrBasicUsage() {
        val result1 = 5.sqr()
        assertEquals(25, result1)

        val result2 = 10L.sqr()
        assertEquals(100L, result2)

        val result3 = 3.5.sqr()
        assertEquals(12.25, result3)
    }

    @Test
    fun sqrDistanceCalculation() {
        val x1 = 3
        val y1 = 4
        val x2 = 6
        val y2 = 8

        val dx = x2 - x1
        val dy = y2 - y1
        val distanceSquared = dx.sqr() + dy.sqr()

        assertEquals(25, distanceSquared)
    }

    @Test
    fun sqrAreaCalculation() {
        val sideLength = 7
        val area = sideLength.sqr()

        assertEquals(49, area)
    }
}
