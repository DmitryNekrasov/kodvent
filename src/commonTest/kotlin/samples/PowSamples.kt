/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

class PowSamples {

    @Test
    fun powBasicUsage() {
        assertEquals(1024L, 2L.pow(10L))
        assertEquals(1024L, 2L pow 10L)
        assertEquals(512L, 2L pow 10L - 1) // not 1023: an infix call binds looser than arithmetic
    }

    @Test
    fun powModuloBasicUsage() {
        assertEquals(976_371_285L, 2L.pow(100L, 1_000_000_007L))
    }
}
