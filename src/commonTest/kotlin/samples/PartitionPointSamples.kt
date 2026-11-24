/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.search.partitionPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class PartitionPointSamples {

    @Test
    fun findFirstElementGreaterOrEqual() {
        val sorted = intArrayOf(5, 10, 15, 20, 25, 30)

        assertEquals(4, partitionPoint(0, sorted.size) { sorted[it] < 22 }) // the index of 25
        assertEquals(6, partitionPoint(0, sorted.size) { sorted[it] < 100 }) // true for every index: toIndex
    }

    @Test
    fun squareRootFloor() {
        val n = 12_345_678_912_345L

        // The last x with x * x <= n is one before the partition point
        val sqrtFloor = partitionPoint(0L, 10_000_000L) { x -> x * x <= n } - 1
        assertEquals(3_513_641L, sqrtFloor)
    }
}
