/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.math.sqr
import kotlin.test.Test
import kotlin.test.assertEquals

class SqrSamples {

    @Test
    fun sqrBasicUsage() {
        assertEquals(25, 5.sqr())
        assertEquals(100L, 10L.sqr())
        assertEquals(12.25, 3.5.sqr())
    }
}
