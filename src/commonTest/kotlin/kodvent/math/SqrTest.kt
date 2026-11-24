/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals

class SqrTest {

    @Test
    fun `sqr multiplies the value by itself`() {
        assertEquals(144, 12.sqr())
        assertEquals(144, (-12).sqr())
        assertEquals(0, 0.sqr())
        assertEquals(9_000_000_000_000_000_000L, 3_000_000_000L.sqr())
        assertEquals(9_000_000_000_000_000_000L, (-3_000_000_000L).sqr())
        assertEquals(2.25, 1.5.sqr())
        assertEquals(2.25, (-1.5).sqr())
        assertEquals(1_000_002_000_001.0, 1_000_001.0.sqr())
        assertEquals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.sqr())
        assertEquals(0.0, (-0.0).sqr())
    }

    @Test
    fun `sqr wraps around only when the result does not fit`() {
        assertEquals(2_147_395_600, 46_340.sqr())
        assertEquals(-2_147_479_015, 46_341.sqr())
        assertEquals(9_223_372_030_926_249_001L, 3_037_000_499L.sqr())
        assertEquals(-9_223_372_036_709_301_616L, 3_037_000_500L.sqr())
    }
}
