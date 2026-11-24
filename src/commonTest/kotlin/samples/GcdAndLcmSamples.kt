/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
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
        assertEquals(6, gcd(48, 18))
        assertEquals(6, gcd(-48, 18))
        assertEquals(5, gcd(0, 5))
        assertEquals(2_000_000_000L, gcd(6_000_000_000L, 4_000_000_000L))
    }

    @Test
    fun lcmBasicUsage() {
        assertEquals(24, lcm(8, 12))
        assertEquals(24, lcm(-8, 12))
        assertEquals(0, lcm(0, 5))
        assertEquals(12_000_000_000L, lcm(6_000_000_000L, 4_000_000_000L))
    }
}
