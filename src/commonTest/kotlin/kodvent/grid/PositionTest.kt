/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PositionTest {

    @Test
    fun `x is the column and y is the input line`() {
        val grid = listOf("ab", "cd").toGrid()

        assertEquals('a', grid[Position(0, 0)])
        assertEquals('b', grid[Position(1, 0)])
        assertEquals('c', grid[Position(0, 1)])
        assertEquals('d', grid[Position(1, 1)])
    }

    @Test
    fun `position x y reads the cell at line y and index x of a non square input`() {
        val lines = listOf("abc", "def")
        val grid = lines.toGrid()

        for (y in 0..<2) {
            for (x in 0..<3) {
                assertEquals(lines[y][x], grid[Position(x, y)])
            }
        }
    }

    @Test
    fun `y grows downward`() {
        assertEquals(Position(4, 1), Position(4, 0) + Direction.DOWN)
        assertEquals(Position(4, -1), Position(4, 0) + Direction.UP)
    }

    @Test
    fun `x grows to the right`() {
        assertEquals(Position(5, 2), Position(4, 2) + Direction.RIGHT)
        assertEquals(Position(3, 2), Position(4, 2) + Direction.LEFT)
    }

    @Test
    fun `constructor keeps x and y as given`() {
        val position = Position(7, -3)

        assertEquals(7, position.x)
        assertEquals(-3, position.y)
    }

    @Test
    fun `positions with the same coordinates are equal`() {
        assertEquals(Position(3, 4), Position(3, 4))
        assertEquals(Position(-2, -5), Position(-2, -5))
        assertEquals(Position(3, 4).hashCode(), Position(3, 4).hashCode())
    }

    @Test
    fun `positions with swapped or different coordinates are not equal`() {
        assertNotEquals(Position(1, 2), Position(2, 1))
        assertNotEquals(Position(1, 2), Position(1, 3))
        assertNotEquals(Position(1, 2), Position(-1, 2))
    }

    @Test
    fun `toString shows both coordinates by name`() {
        assertEquals("Position(x=3, y=4)", Position(3, 4).toString())
        assertEquals("Position(x=-1, y=-2)", Position(-1, -2).toString())
        assertEquals("Position(x=0, y=0)", Position(0, 0).toString())
    }

    @Test
    fun `destructuring gives x then y`() {
        val (x, y) = Position(6, -9)

        assertEquals(6, x)
        assertEquals(-9, y)
    }

    @Test
    fun `hashCode is distinct for all positions of a 200 by 200 grid`() {
        val hashes = (0..<200).flatMap { y -> (0..<200).map { x -> Position(x, y).hashCode() } }.toSet()

        assertEquals(200 * 200, hashes.size)
    }

    @Test
    fun `hashCode is distinct for all coordinates from minus 500 to 500`() {
        // Sorted in an IntArray: a set of a million boxed hash codes is slow on JS, Wasm and Native
        val hashes = IntArray(1001 * 1001) { Position(it % 1001 - 500, it / 1001 - 500).hashCode() }
        hashes.sort()

        assertTrue((1..hashes.lastIndex).none { hashes[it] == hashes[it - 1] })
    }

    @Test
    fun `positions work as set elements and map keys`() {
        val set = setOf(Position(1, 2), Position(1, 2), Position(2, 1), Position(-1, -2))
        assertEquals(3, set.size)

        val map = mapOf(Position(0, -1) to "up", Position(1, 0) to "right")
        assertEquals("up", map[Position(0, -1)])
        assertEquals("right", map[Position(1, 0)])
        assertNull(map[Position(-1, 0)])
    }

    @Test
    fun `plus adds the coordinates`() {
        assertEquals(Position(4, 6), Position(1, 2) + Position(3, 4))
        assertEquals(Position(-2, 1), Position(1, -3) + Position(-3, 4))
    }

    @Test
    fun `plus of the zero offset is the same position`() {
        assertEquals(Position(5, -7), Position(5, -7) + Position(0, 0))
    }

    @Test
    fun `minus gives the offset from the other position to this one`() {
        assertEquals(Position(4, -5), Position(5, 2) - Position(1, 7))
        assertEquals(Position(0, 0), Position(3, 3) - Position(3, 3))
    }

    @Test
    fun `adding the difference to the other position gives this position`() {
        val a = Position(8, -2)
        val b = Position(-3, 5)

        assertEquals(a, b + (a - b))
    }

    @Test
    fun `times multiplies both coordinates by the factor`() {
        assertEquals(Position(8, -12), Position(2, -3) * 4)
        assertEquals(Position(-2, 3), Position(2, -3) * -1)
        assertEquals(Position(0, 0), Position(2, -3) * 0)
        assertEquals(Position(2, -3), Position(2, -3) * 1)
    }

    @Test
    fun `an offset times a factor extends a line of positions`() {
        val a = Position(5, 5)
        val b = Position(3, 4)

        assertEquals(Position(9, 7), a + (a - b) * 2)
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
    fun `minus a direction moves one step against that direction`() {
        val p = Position(3, 3)

        assertEquals(Position(3, 4), p - Direction.UP)
        assertEquals(Position(2, 3), p - Direction.RIGHT)
        assertEquals(Position(3, 2), p - Direction.DOWN)
        assertEquals(Position(4, 3), p - Direction.LEFT)
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
    fun `plus a direction may leave the non negative quadrant`() {
        assertEquals(Position(0, -1), Position(0, 0) + Direction.UP)
        assertEquals(Position(-1, 0), Position(0, 0) + Direction.LEFT)
    }

    @Test
    fun `direction times steps added to a position moves that far`() {
        val p = Position(1, 1)

        assertEquals(Position(4, 1), p + Direction.RIGHT * 3)
        assertEquals(Position(1, -4), p + Direction.UP * 5)
        assertEquals(Position(1, 1), p + Direction.DOWN * 0)
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
    fun `minus a Direction8 moves one step against that direction`() {
        val p = Position(3, 3)

        assertEquals(Position(3, 4), p - Direction8.UP)
        assertEquals(Position(2, 4), p - Direction8.UP_RIGHT)
        assertEquals(Position(2, 3), p - Direction8.RIGHT)
        assertEquals(Position(2, 2), p - Direction8.DOWN_RIGHT)
        assertEquals(Position(3, 2), p - Direction8.DOWN)
        assertEquals(Position(4, 2), p - Direction8.DOWN_LEFT)
        assertEquals(Position(4, 3), p - Direction8.LEFT)
        assertEquals(Position(4, 4), p - Direction8.UP_LEFT)
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
    fun `Direction8 times steps added to a position moves that far diagonally`() {
        val p = Position(0, 0)

        assertEquals(Position(3, -3), p + Direction8.UP_RIGHT * 3)
        assertEquals(Position(-2, 2), p + Direction8.DOWN_LEFT * 2)
    }

    @Test
    fun `manhattanDistanceTo sums the absolute coordinate differences`() {
        assertEquals(7, Position(1, 2).manhattanDistanceTo(Position(4, -2)))
        assertEquals(10, Position(-3, -4).manhattanDistanceTo(Position(2, 1)))
        assertEquals(5, Position(0, 0).manhattanDistanceTo(Position(-5, 0)))
    }

    @Test
    fun `manhattanDistanceTo is symmetric`() {
        val a = Position(6, -1)
        val b = Position(-2, 3)

        assertEquals(12, a.manhattanDistanceTo(b))
        assertEquals(12, b.manhattanDistanceTo(a))
    }

    @Test
    fun `manhattanDistanceTo the same position is zero`() {
        assertEquals(0, Position(4, -4).manhattanDistanceTo(Position(4, -4)))
    }

    @Test
    fun `manhattanDistanceTo a diagonal neighbor is two`() {
        val p = Position(2, 2)

        assertEquals(2, p.manhattanDistanceTo(p + Direction8.UP_LEFT))
        assertEquals(1, p.manhattanDistanceTo(p + Direction8.UP))
    }

    @Test
    fun `wrap keeps a position that is already inside`() {
        assertEquals(Position(0, 0), Position(0, 0).wrap(5, 3))
        assertEquals(Position(4, 2), Position(4, 2).wrap(5, 3))
    }

    @Test
    fun `wrap brings a position past the far edges back to the start`() {
        assertEquals(Position(0, 0), Position(5, 3).wrap(5, 3))
        assertEquals(Position(2, 1), Position(7, 4).wrap(5, 3))
        assertEquals(Position(1, 2), Position(16, 11).wrap(5, 3))
    }

    @Test
    fun `wrap is floor modulo for negative coordinates`() {
        assertEquals(Position(4, 2), Position(-1, -1).wrap(5, 3))
        assertEquals(Position(0, 0), Position(-5, -3).wrap(5, 3))
        assertEquals(Position(4, 0), Position(-6, 3).wrap(5, 3))
        assertEquals(Position(2, 2), Position(7, -1).wrap(5, 3))
    }

    @Test
    fun `wrap uses width for x and height for y`() {
        assertEquals(Position(1, 6), Position(3, 6).wrap(2, 7))
        assertEquals(Position(3, 0), Position(3, 6).wrap(7, 2))
    }

    @Test
    fun `wrap onto a one by one area gives the origin`() {
        assertEquals(Position(0, 0), Position(-13, 42).wrap(1, 1))
    }

    @Test
    fun `wrap throws IllegalArgumentException for a zero width or height`() {
        assertFailsWith<IllegalArgumentException> {
            Position(1, 1).wrap(0, 3)
        }
        assertFailsWith<IllegalArgumentException> {
            Position(1, 1).wrap(3, 0)
        }
        assertFailsWith<IllegalArgumentException> {
            Position(0, 0).wrap(0, 0)
        }
    }

    @Test
    fun `wrap throws IllegalArgumentException for a negative width or height`() {
        assertFailsWith<IllegalArgumentException> {
            Position(1, 1).wrap(-1, 3)
        }
        assertFailsWith<IllegalArgumentException> {
            Position(1, 1).wrap(3, -2)
        }
    }
}
