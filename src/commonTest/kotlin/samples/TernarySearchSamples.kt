/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.search.ternarySearch
import kotlin.test.Test
import kotlin.test.assertEquals

class TernarySearchSamples {

    @Test
    fun maximizeQuadratic() {
        val x = ternarySearch(0.0, 10.0) { x -> -(x - 3) * (x - 3) + 9 }

        assertEquals(3.0, x, absoluteTolerance = 1e-6)
    }

    @Test
    fun minimizeByNegation() {
        // To find a minimum, maximize the negated function: (x - 4)^2 + 1 is smallest at x = 4
        val x = ternarySearch(0.0, 10.0) { x -> -((x - 4) * (x - 4) + 1) }

        assertEquals(4.0, x, absoluteTolerance = 1e-6)
    }
}
