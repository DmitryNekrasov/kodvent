/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PositionTest {

    @Test
    fun `destructuring gives x then y`() {
        val (x, y) = Position(x = 6, y = -9)

        assertEquals(6, x)
        assertEquals(-9, y)
    }

    @Test
    fun `hashCode is distinct for all coordinates from minus 500 to 500`() {
        // Sorted in an IntArray: a set of a million boxed hash codes is slow on JS, Wasm and Native
        val hashes = IntArray(1001 * 1001) { Position(it % 1001 - 500, it / 1001 - 500).hashCode() }
        hashes.sort()

        assertTrue((1..hashes.lastIndex).none { hashes[it] == hashes[it - 1] })
    }

    @Test
    fun `plus adds the coordinates`() {
        assertEquals(Position(4, 6), Position(1, 2) + Position(3, 4))
        assertEquals(Position(-2, 1), Position(1, -3) + Position(-3, 4))
    }

    @Test
    fun `minus gives the offset from the other position to this one`() {
        assertEquals(Position(4, -5), Position(5, 2) - Position(1, 7))
        assertEquals(Position(0, 0), Position(3, 3) - Position(3, 3))
    }

    @Test
    fun `times multiplies both coordinates by the factor`() {
        assertEquals(Position(8, -12), Position(2, -3) * 4)
        assertEquals(Position(-2, 3), Position(2, -3) * -1)
        assertEquals(Position(0, 0), Position(2, -3) * 0)
        assertEquals(Position(2, -3), Position(2, -3) * 1)
    }

    @Test
    fun `plus a direction moves one step in that direction`() {
        val p = Position(3, 3)

        assertEquals(Position(3, 2), p + Direction.UP)
        assertEquals(Position(4, 3), p + Direction.RIGHT)
        assertEquals(Position(3, 4), p + Direction.DOWN)
        assertEquals(Position(2, 3), p + Direction.LEFT)
    }

    @Test
    fun `plus a direction then minus it returns to the start`() {
        val starts = listOf(Position(0, 0), Position(3, 7), Position(-4, -9))
        for (p in starts) {
            for (d in Direction.entries) {
                assertEquals(p, p + d - d)
                assertEquals(p, p - d + d)
            }
        }
    }

    @Test
    fun `plus a Direction8 moves one step in that direction`() {
        val p = Position(3, 3)

        assertEquals(Position(3, 2), p + Direction8.UP)
        assertEquals(Position(4, 2), p + Direction8.UP_RIGHT)
        assertEquals(Position(4, 3), p + Direction8.RIGHT)
        assertEquals(Position(4, 4), p + Direction8.DOWN_RIGHT)
        assertEquals(Position(3, 4), p + Direction8.DOWN)
        assertEquals(Position(2, 4), p + Direction8.DOWN_LEFT)
        assertEquals(Position(2, 3), p + Direction8.LEFT)
        assertEquals(Position(2, 2), p + Direction8.UP_LEFT)
    }

    @Test
    fun `plus a Direction8 then minus it returns to the start`() {
        val starts = listOf(Position(0, 0), Position(3, 7), Position(-4, -9))
        for (p in starts) {
            for (d in Direction8.entries) {
                assertEquals(p, p + d - d)
                assertEquals(p, p - d + d)
            }
        }
    }

    @Test
    fun `manhattanDistanceTo sums the absolute coordinate differences`() {
        assertEquals(7, Position(1, 2).manhattanDistanceTo(Position(4, -2)))
        assertEquals(10, Position(-3, -4).manhattanDistanceTo(Position(2, 1)))
        assertEquals(5, Position(0, 0).manhattanDistanceTo(Position(-5, 0)))
        assertEquals(0, Position(4, -4).manhattanDistanceTo(Position(4, -4)))
    }

    @Test
    fun `manhattanDistanceTo overflows for positions more than the largest Int apart`() {
        assertEquals(Int.MAX_VALUE, Position(Int.MAX_VALUE, 0).manhattanDistanceTo(Position(0, 0)))
        assertEquals(Int.MIN_VALUE, Position(Int.MAX_VALUE, 0).manhattanDistanceTo(Position(-1, 0)))
    }

    @Test
    fun `wrap is floor modulo of width for x and of height for y`() {
        assertEquals(Position(4, 2), Position(4, 2).wrap(5, 3))
        assertEquals(Position(0, 0), Position(5, 3).wrap(5, 3))
        assertEquals(Position(1, 2), Position(16, 11).wrap(5, 3))
        assertEquals(Position(4, 2), Position(-1, -1).wrap(5, 3))
        assertEquals(Position(0, 0), Position(-5, -3).wrap(5, 3))
        assertEquals(Position(3, 2), Position(-7, -4).wrap(5, 3))
        assertEquals(Position(2, 1), Position(-7, -4).wrap(3, 5))
        assertEquals(Position(0, 0), Position(-13, 42).wrap(1, 1))
    }

    @Test
    fun `wrap throws IllegalArgumentException for a width or height that is not positive`() {
        assertFailsWith<IllegalArgumentException> { Position(1, 1).wrap(0, 3) }
        assertFailsWith<IllegalArgumentException> { Position(1, 1).wrap(3, 0) }
        assertFailsWith<IllegalArgumentException> { Position(1, 1).wrap(-1, 3) }
        assertFailsWith<IllegalArgumentException> { Position(1, 1).wrap(3, -2) }
        assertFailsWith<IllegalArgumentException> { Position(1, 1).wrap(-5, -3) }
    }
}
