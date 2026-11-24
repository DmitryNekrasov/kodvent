/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.SegmentTree
import kodvent.math.gcd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SegmentTreeSamples {

    @Test
    fun basicUsage() {
        val tree = SegmentTree(listOf(1, 3, 5, 7, 9, 11), Int::plus)
        assertEquals(24, tree[1, 4]) // 3 + 5 + 7 + 9

        tree[2] = 15
        assertEquals(34, tree[1, 4]) // 3 + 15 + 7 + 9

        tree[2] += 10
        assertEquals(25, tree[2])
    }

    @Test
    fun otherOperations() {
        val numbers = listOf(12, 18, 8, 30, 36, 42)

        assertEquals(8, SegmentTree(numbers, ::minOf)[0, 3])
        assertEquals(30, SegmentTree(numbers, ::maxOf)[0, 3])
        assertEquals(2, SegmentTree(numbers, ::gcd)[0, 3])
    }

    @Test
    fun safeQueryMethods() {
        val tree = SegmentTree(listOf(5, 10, 15, 20, 25), Int::plus)

        assertEquals(45, tree.getOrNull(1, 3))
        assertNull(tree.getOrNull(0, 10)) // the end is out of bounds
        assertNull(tree.getOrNull(3, 1)) // the start is after the end

        assertEquals(45, tree.getOrDefault(1, 3, 0))
        assertEquals(0, tree.getOrDefault(0, 10, 0))
    }
}
