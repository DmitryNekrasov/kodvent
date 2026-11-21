/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class GridTest {

    // With the factory and not toGrid(), so that these tests do not depend on parsing
    private fun charGrid(vararg lines: String): Grid<Char> =
        Grid(lines[0].length, lines.size) { lines[it.y][it.x] }

    private fun mutableCharGrid(vararg lines: String): MutableGrid<Char> =
        MutableGrid(lines[0].length, lines.size) { lines[it.y][it.x] }

    private fun numbers3x2(): Grid<Int> = Grid(3, 2) { it.x + 10 * it.y }

    private fun nullThenA(): Grid<Char?> = Grid(2, 1) { if (it.x == 0) null else 'a' }

    private fun isMutable(grid: Grid<*>): Boolean = grid is MutableGrid<*>

    // Calls grid.equals(other) on every platform, whatever the static types are.
    private fun gridEquals(grid: Grid<*>, other: Any?): Boolean = grid == other

    private val readingOrder3x2 = listOf(
        Position(0, 0), Position(1, 0), Position(2, 0),
        Position(0, 1), Position(1, 1), Position(2, 1),
    )

    // Positions outside a 3 x 2 grid. The first four catch a check of y * width + x alone: Position(3, 0)
    // would be cell 3, Position(-1, 1) would be cell 2, Position(1, 2) is inside only with x and y swapped,
    // and 3 * 1431655766 overflows to 2.
    private val outside3x2 = listOf(
        Position(3, 0), Position(-1, 1), Position(1, 2), Position(0, 1431655766),
        Position(-1, 0), Position(0, -1), Position(0, 2), Position(3, 1), Position(3, 2), Position(-1, -1),
        Position(Int.MAX_VALUE, 0), Position(Int.MIN_VALUE, 0),
        Position(0, Int.MAX_VALUE), Position(0, Int.MIN_VALUE),
    )

    @Test
    fun `Grid factory creates a grid of the given width and height`() {
        val grid = Grid(3, 2) { '.' }

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
    }

    @Test
    fun `Grid factory stores what init returns for each position`() {
        val grid = Grid(3, 2) { it.x + 10 * it.y }

        assertEquals(0, grid[Position(0, 0)])
        assertEquals(1, grid[Position(1, 0)])
        assertEquals(2, grid[Position(2, 0)])
        assertEquals(10, grid[Position(0, 1)])
        assertEquals(11, grid[Position(1, 1)])
        assertEquals(12, grid[Position(2, 1)])
    }

    @Test
    fun `Grid factory calls init once for every position in reading order`() {
        val calls = mutableListOf<Position>()
        val grid = Grid(3, 2) {
            calls += it
            '.'
        }

        assertEquals(readingOrder3x2, calls)
        assertEquals(6, grid.values.size)
    }

    @Test
    fun `MutableGrid factory creates a grid of the given width and height`() {
        val grid = MutableGrid(3, 2) { '.' }

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
    }

    @Test
    fun `MutableGrid factory stores what init returns for each position`() {
        val grid = MutableGrid(3, 2) { it.x + 10 * it.y }

        assertEquals(0, grid[Position(0, 0)])
        assertEquals(1, grid[Position(1, 0)])
        assertEquals(2, grid[Position(2, 0)])
        assertEquals(10, grid[Position(0, 1)])
        assertEquals(11, grid[Position(1, 1)])
        assertEquals(12, grid[Position(2, 1)])
    }

    @Test
    fun `MutableGrid factory calls init once for every position in reading order`() {
        val calls = mutableListOf<Position>()
        val grid = MutableGrid(3, 2) {
            calls += it
            '.'
        }

        assertEquals(readingOrder3x2, calls)
        assertEquals(6, grid.values.size)
    }

    @Test
    fun `a 0 x 0 grid is valid and has no cells`() {
        val grid = Grid(0, 0) { '.' }

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
        assertTrue(grid.positions.isEmpty())
        assertTrue(grid.values.isEmpty())
        assertTrue(grid.rows.isEmpty())
        assertTrue(grid.columns.isEmpty())
    }

    @Test
    fun `a 0 x 2 grid is valid and has two empty rows and no columns`() {
        val grid = Grid(0, 2) { '.' }

        assertEquals(0, grid.width)
        assertEquals(2, grid.height)
        assertTrue(grid.positions.isEmpty())
        assertTrue(grid.values.isEmpty())
        assertEquals(2, grid.rows.size)
        assertTrue(grid.rows[0].isEmpty())
        assertTrue(grid.rows[1].isEmpty())
        assertTrue(grid.columns.isEmpty())
    }

    @Test
    fun `a 3 x 0 grid is valid and has no rows and three empty columns`() {
        val grid = Grid(3, 0) { '.' }

        assertEquals(3, grid.width)
        assertEquals(0, grid.height)
        assertTrue(grid.positions.isEmpty())
        assertTrue(grid.values.isEmpty())
        assertTrue(grid.rows.isEmpty())
        assertEquals(3, grid.columns.size)
        assertTrue(grid.columns[0].isEmpty())
        assertTrue(grid.columns[1].isEmpty())
        assertTrue(grid.columns[2].isEmpty())
    }

    @Test
    fun `MutableGrid factory accepts zero sizes`() {
        val empty = MutableGrid(0, 0) { '.' }
        val zeroWidth = MutableGrid(0, 2) { '.' }
        val zeroHeight = MutableGrid(3, 0) { '.' }

        assertEquals(0, empty.width)
        assertEquals(0, empty.height)
        assertTrue(empty.values.isEmpty())
        assertEquals(0, zeroWidth.width)
        assertEquals(2, zeroWidth.height)
        assertTrue(zeroWidth.values.isEmpty())
        assertEquals(3, zeroHeight.width)
        assertEquals(0, zeroHeight.height)
        assertTrue(zeroHeight.values.isEmpty())
    }

    @Test
    fun `factories never call init for a grid without cells`() {
        var calls = 0
        val countingInit: (Position) -> Char = {
            calls++
            '.'
        }

        val grids = listOf(
            Grid(0, 0, countingInit),
            Grid(0, 2, countingInit),
            Grid(3, 0, countingInit),
            MutableGrid(0, 2, countingInit),
            MutableGrid(3, 0, countingInit),
        )

        assertEquals(0, calls)
        assertTrue(grids.all { it.values.isEmpty() })
    }

    @Test
    fun `Grid factory throws IllegalArgumentException for a negative width`() {
        assertFailsWith<IllegalArgumentException> { Grid(-1, 2) { '.' } }
    }

    @Test
    fun `Grid factory throws IllegalArgumentException for a negative height`() {
        assertFailsWith<IllegalArgumentException> { Grid(2, -1) { '.' } }
    }

    @Test
    fun `Grid factory rejects a negative size next to a zero or another negative size`() {
        assertFailsWith<IllegalArgumentException> { Grid(-1, 0) { '.' } }
        assertFailsWith<IllegalArgumentException> { Grid(0, -1) { '.' } }
        assertFailsWith<IllegalArgumentException> { Grid(-2, -3) { '.' } }
    }

    @Test
    fun `MutableGrid factory throws IllegalArgumentException for a negative width`() {
        assertFailsWith<IllegalArgumentException> { MutableGrid(-1, 2) { '.' } }
    }

    @Test
    fun `MutableGrid factory throws IllegalArgumentException for a negative height`() {
        assertFailsWith<IllegalArgumentException> { MutableGrid(2, -1) { '.' } }
    }

    @Test
    fun `MutableGrid factory rejects a negative size next to a zero or another negative size`() {
        assertFailsWith<IllegalArgumentException> { MutableGrid(-1, 0) { '.' } }
        assertFailsWith<IllegalArgumentException> { MutableGrid(0, -1) { '.' } }
        assertFailsWith<IllegalArgumentException> { MutableGrid(-2, -3) { '.' } }
    }

    @Test
    fun `Grid factory returns a grid that is not a MutableGrid`() {
        assertFalse(isMutable(Grid(2, 2) { 0 }))
    }

    @Test
    fun `a MutableGrid stays a MutableGrid when seen as a Grid`() {
        assertTrue(isMutable(MutableGrid(2, 2) { 0 }))
    }

    @Test
    fun `toGrid of lines returns a grid that is not a MutableGrid`() {
        assertFalse(isMutable(listOf("ab", "cd").toGrid()))
        assertFalse(isMutable(listOf("12", "34").toGrid { it.digitToInt() }))
    }

    @Test
    fun `toGrid of a MutableGrid returns a grid that is not a MutableGrid`() {
        val grid = MutableGrid(2, 2) { 0 }

        assertFalse(isMutable(grid.toGrid()))
    }

    @Test
    fun `map and mapIndexed of a MutableGrid return grids that are not MutableGrids`() {
        val grid = MutableGrid(2, 2) { 0 }

        assertFalse(isMutable(grid.map { it + 1 }))
        assertFalse(isMutable(grid.mapIndexed { position, value -> position.x + value }))
    }

    @Test
    fun `transposed and rotated copies of a MutableGrid are not MutableGrids`() {
        val grid = MutableGrid(2, 2) { 0 }

        assertFalse(isMutable(grid.transposed()))
        assertFalse(isMutable(grid.rotatedClockwise()))
        assertFalse(isMutable(grid.rotatedCounterclockwise()))
    }

    @Test
    fun `get takes x as the column and y as the row`() {
        val grid = charGrid("abc", "def")

        assertEquals('a', grid[Position(0, 0)])
        assertEquals('c', grid[Position(2, 0)])
        assertEquals('d', grid[Position(0, 1)])
        assertEquals('e', grid[Position(1, 1)])
        assertEquals('f', grid[Position(2, 1)])
    }

    @Test
    fun `get outside the grid throws IndexOutOfBoundsException`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            assertFailsWith<IndexOutOfBoundsException>("get at $position") { grid[position] }
        }
    }

    @Test
    fun `get on a grid without cells throws IndexOutOfBoundsException`() {
        val empty = Grid(0, 0) { '.' }
        val zeroWidth = Grid(0, 2) { '.' }
        val zeroHeight = Grid(3, 0) { '.' }

        assertFailsWith<IndexOutOfBoundsException> { empty[Position(0, 0)] }
        assertFailsWith<IndexOutOfBoundsException> { zeroWidth[Position(0, 0)] }
        assertFailsWith<IndexOutOfBoundsException> { zeroWidth[Position(0, 1)] }
        assertFailsWith<IndexOutOfBoundsException> { zeroHeight[Position(0, 0)] }
        assertFailsWith<IndexOutOfBoundsException> { zeroHeight[Position(2, 0)] }
    }

    @Test
    fun `set overwrites one cell and leaves the others`() {
        val grid = MutableGrid(3, 2) { 0 }

        grid[Position(1, 0)] = 5
        grid[Position(0, 1)] = 7

        assertEquals(5, grid[Position(1, 0)])
        assertEquals(7, grid[Position(0, 1)])
        assertEquals(listOf(0, 5, 0, 7, 0, 0), grid.values.toList())
    }

    @Test
    fun `set outside the grid throws IndexOutOfBoundsException and writes nothing`() {
        val grid = MutableGrid(3, 2) { 0 }

        for (position in outside3x2) {
            assertFailsWith<IndexOutOfBoundsException>("set at $position") { grid[position] = 1 }
        }
        assertEquals(listOf(0, 0, 0, 0, 0, 0), grid.values.toList())
    }

    @Test
    fun `set on a grid without cells throws IndexOutOfBoundsException`() {
        val empty = MutableGrid(0, 0) { '.' }
        val zeroWidth = MutableGrid(0, 2) { '.' }
        val zeroHeight = MutableGrid(3, 0) { '.' }

        assertFailsWith<IndexOutOfBoundsException> { empty[Position(0, 0)] = '#' }
        assertFailsWith<IndexOutOfBoundsException> { zeroWidth[Position(0, 0)] = '#' }
        assertFailsWith<IndexOutOfBoundsException> { zeroWidth[Position(0, 1)] = '#' }
        assertFailsWith<IndexOutOfBoundsException> { zeroHeight[Position(0, 0)] = '#' }
        assertFailsWith<IndexOutOfBoundsException> { zeroHeight[Position(2, 0)] = '#' }
    }

    @Test
    fun `a cell of a MutableGrid of Int can be incremented in place`() {
        val grid = MutableGrid(2, 2) { 0 }
        val p = Position(1, 0)

        grid[p]++
        assertEquals(1, grid[p])

        grid[p] += 1
        assertEquals(2, grid[p])

        assertEquals(listOf(0, 2, 0, 0), grid.values.toList())
    }

    @Test
    fun `a grid can be read as a grid of a supertype`() {
        val grid: Grid<Any> = charGrid("ab", "cd")

        assertEquals<Any>('c', grid[Position(0, 1)])
        assertEquals(Position(1, 1), grid.positionOf('d'))
    }

    @Test
    fun `get by x and y reads the cell that get by position reads`() {
        for ((width, height) in listOf(3 to 2, 2 to 3, 1 to 1, 1 to 4, 4 to 1)) {
            val grid = Grid(width, height) { it.x + 10 * it.y }
            for (position in grid.positions) {
                assertEquals(grid[position], grid[position.x, position.y], "$position in a $width x $height grid")
            }
        }
        assertEquals('f', charGrid("abc", "def")[2, 1])
        assertEquals('d', charGrid("abc", "def")[0, 1])
    }

    @Test
    fun `get by x and y outside the grid throws IndexOutOfBoundsException`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            assertFailsWith<IndexOutOfBoundsException>("get at $position") { grid[position.x, position.y] }
        }
        assertFailsWith<IndexOutOfBoundsException> { Grid(0, 0) { '.' }[0, 0] }
        assertFailsWith<IndexOutOfBoundsException> { Grid(0, 2) { '.' }[0, 1] }
        assertFailsWith<IndexOutOfBoundsException> { Grid(3, 0) { '.' }[2, 0] }
    }

    @Test
    fun `get by x and y and get by position fail with the same message`() {
        val grid = charGrid("abc", "def")

        val byPosition = assertFailsWith<IndexOutOfBoundsException> { grid[Position(3, 1)] }
        val byCoordinates = assertFailsWith<IndexOutOfBoundsException> { grid[3, 1] }

        assertEquals("Position(x=3, y=1) is outside the 3 x 2 grid", byCoordinates.message)
        assertEquals(byPosition.message, byCoordinates.message)
    }

    @Test
    fun `set by x and y writes the cell that get by position reads`() {
        val grid = MutableGrid(3, 2) { 0 }

        grid[1, 0] = 5
        grid[0, 1] = 7

        assertEquals(5, grid[Position(1, 0)])
        assertEquals(7, grid[Position(0, 1)])
        assertEquals(listOf(0, 5, 0, 7, 0, 0), grid.values)
    }

    @Test
    fun `set by x and y outside the grid throws IndexOutOfBoundsException and writes nothing`() {
        val grid = MutableGrid(3, 2) { 0 }

        for (position in outside3x2) {
            assertFailsWith<IndexOutOfBoundsException>("set at $position") { grid[position.x, position.y] = 1 }
        }
        assertEquals(listOf(0, 0, 0, 0, 0, 0), grid.values)
    }

    @Test
    fun `compound assignment and increment work with x and y`() {
        val grid = MutableGrid(2, 2) { 1 }

        grid[1, 0] += 4
        grid[0, 1]++
        grid[1, 1] = grid[1, 0] * grid[0, 1]

        assertEquals(listOf(1, 5, 2, 10), grid.values)
    }

    @Test
    fun `get by x and y of a MutableGrid shows later writes by position`() {
        val grid = mutableCharGrid("ab", "cd")

        grid[Position(1, 1)] = '#'

        assertEquals('#', grid[1, 1])
    }

    @Test
    fun `position in grid is true for every cell`() {
        val grid = charGrid("abc", "def")

        for (position in readingOrder3x2) {
            assertTrue(position in grid, "$position")
        }
    }

    @Test
    fun `position in grid is false one step past each edge`() {
        val grid = charGrid("abc", "def")

        assertFalse(Position(3, 0) in grid)
        assertFalse(Position(-1, 0) in grid)
        assertFalse(Position(0, 2) in grid)
        assertFalse(Position(0, -1) in grid)
        assertTrue(Position(3, 1) !in grid)
    }

    @Test
    fun `position in grid is false for every position outside`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            assertFalse(position in grid, "$position")
        }
    }

    @Test
    fun `position in grid checks x against width and y against height`() {
        val wide = charGrid("abc", "def")
        val tall = charGrid("ab", "cd", "ef")

        assertTrue(Position(2, 1) in wide)
        assertFalse(Position(1, 2) in wide)
        assertTrue(Position(1, 2) in tall)
        assertFalse(Position(2, 1) in tall)
    }

    @Test
    fun `position in grid is false for a grid without cells`() {
        val empty = Grid(0, 0) { '.' }
        val zeroWidth = Grid(0, 2) { '.' }
        val zeroHeight = Grid(3, 0) { '.' }

        assertFalse(Position(0, 0) in empty)
        assertFalse(Position(0, 0) in zeroWidth)
        assertFalse(Position(0, 1) in zeroWidth)
        assertFalse(Position(0, 0) in zeroHeight)
        assertFalse(Position(2, 0) in zeroHeight)
    }

    @Test
    fun `position in grid tests the bounds even when the values are positions`() {
        val grid = Grid(2, 2) { Position(it.x + 10, it.y + 10) }

        assertTrue(Position(1, 1) in grid)
        assertFalse(Position(1, 1) in grid.values)
        assertFalse(Position(11, 11) in grid)
        assertTrue(Position(11, 11) in grid.values)
    }

    @Test
    fun `position in grid is true for a null cell`() {
        val grid = nullThenA()

        assertNull(grid[Position(0, 0)])
        assertTrue(Position(0, 0) in grid)
    }

    @Test
    fun `getOrNull returns the value inside`() {
        val grid = charGrid("abc", "def")

        assertEquals('a', grid.getOrNull(Position(0, 0)))
        assertEquals('d', grid.getOrNull(Position(0, 1)))
        assertEquals('f', grid.getOrNull(Position(2, 1)))
    }

    @Test
    fun `getOrNull returns null outside`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            assertNull(grid.getOrNull(position), "$position")
        }
    }

    @Test
    fun `getOrDefault returns the value inside`() {
        val grid = charGrid("abc", "def")

        assertEquals('a', grid.getOrDefault(Position(0, 0), '.'))
        assertEquals('d', grid.getOrDefault(Position(0, 1), '.'))
        assertEquals('f', grid.getOrDefault(Position(2, 1), '.'))
    }

    @Test
    fun `getOrDefault returns the default outside`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            assertEquals('.', grid.getOrDefault(position, '.'), "$position")
        }
    }

    @Test
    fun `getOrElse returns the value inside without calling the lambda`() {
        val grid = charGrid("abc", "def")
        var calls = 0

        val value = grid.getOrElse(Position(1, 1)) {
            calls++
            '.'
        }

        assertEquals('e', value)
        assertEquals(0, calls)
    }

    @Test
    fun `getOrElse calls the lambda once with the position outside`() {
        val grid = charGrid("abc", "def")

        for (position in outside3x2) {
            val received = mutableListOf<Position>()
            val value = grid.getOrElse(position) {
                received += it
                '.'
            }

            assertEquals('.', value, "$position")
            assertEquals(listOf(position), received)
        }
    }

    @Test
    fun `getOrNull returns null for a null cell and for a position outside alike`() {
        val grid = nullThenA()

        assertNull(grid.getOrNull(Position(0, 0)))
        assertNull(grid.getOrNull(Position(2, 0)))
        assertEquals('a', grid.getOrNull(Position(1, 0)))
    }

    @Test
    fun `getOrDefault keeps a null cell inside null`() {
        val grid = nullThenA()

        assertNull(grid.getOrDefault(Position(0, 0), 'z'))
        assertEquals('a', grid.getOrDefault(Position(1, 0), 'z'))
        assertEquals('z', grid.getOrDefault(Position(2, 0), 'z'))
    }

    @Test
    fun `getOrElse keeps a null cell inside null without calling the lambda`() {
        val grid = nullThenA()
        var calls = 0

        val value = grid.getOrElse(Position(0, 0)) {
            calls++
            'z'
        }

        assertNull(value)
        assertEquals(0, calls)
    }

    @Test
    fun `safe reads on a grid without cells always fall back`() {
        val grid = Grid(0, 2) { '.' }
        val origin = Position(0, 0)

        assertNull(grid.getOrNull(origin))
        assertEquals('#', grid.getOrDefault(origin, '#'))
        assertEquals('#', grid.getOrElse(origin) { '#' })
    }

    @Test
    fun `positionOf returns the first match in reading order`() {
        val grid = charGrid(".#.", "#.#")

        assertEquals(Position(1, 0), grid.positionOf('#'))
        assertEquals(Position(0, 0), grid.positionOf('.'))
    }

    @Test
    fun `positionOf finds a value held by the last cell only`() {
        val grid = charGrid("...", "..@")

        assertEquals(Position(2, 1), grid.positionOf('@'))
    }

    @Test
    fun `positionOf throws NoSuchElementException when no cell holds the value`() {
        val grid = charGrid(".#.", "#.#")

        assertFailsWith<NoSuchElementException> { grid.positionOf('@') }
    }

    @Test
    fun `positionOf on a grid without cells throws NoSuchElementException`() {
        val empty = Grid(0, 0) { '.' }
        val zeroWidth = Grid(0, 2) { '.' }
        val zeroHeight = Grid(3, 0) { '.' }

        assertFailsWith<NoSuchElementException> { empty.positionOf('.') }
        assertFailsWith<NoSuchElementException> { zeroWidth.positionOf('.') }
        assertFailsWith<NoSuchElementException> { zeroHeight.positionOf('.') }
    }

    @Test
    fun `positionOfOrNull returns the first match in reading order`() {
        val grid = charGrid(".#.", "#.#")

        assertEquals(Position(1, 0), grid.positionOfOrNull('#'))
        assertEquals(Position(0, 0), grid.positionOfOrNull('.'))
    }

    @Test
    fun `positionOfOrNull returns null when no cell holds the value`() {
        val grid = charGrid(".#.", "#.#")
        val zeroWidth = Grid(0, 2) { '.' }

        assertNull(grid.positionOfOrNull('@'))
        assertNull(zeroWidth.positionOfOrNull('.'))
    }

    @Test
    fun `positionsOf returns every match in reading order`() {
        val grid = charGrid(".#.", "#.#")

        assertEquals(listOf(Position(1, 0), Position(0, 1), Position(2, 1)), grid.positionsOf('#'))
        assertEquals(listOf(Position(0, 0), Position(2, 0), Position(1, 1)), grid.positionsOf('.'))
    }

    @Test
    fun `positionsOf returns an empty list when no cell holds the value`() {
        val grid = charGrid(".#.", "#.#")
        val zeroWidth = Grid(0, 2) { '.' }

        assertTrue(grid.positionsOf('@').isEmpty())
        assertTrue(zeroWidth.positionsOf('.').isEmpty())
    }

    @Test
    fun `positionsOf returns a list that later writes do not change`() {
        val grid = mutableCharGrid(".#.", "#.#")
        val before = grid.positionsOf('#')

        grid[Position(0, 0)] = '#'
        grid[Position(1, 0)] = '.'

        assertEquals(listOf(Position(1, 0), Position(0, 1), Position(2, 1)), before)
        assertEquals(listOf(Position(0, 0), Position(0, 1), Position(2, 1)), grid.positionsOf('#'))
    }

    @Test
    fun `positionOf family compares values with equals`() {
        val grid = Grid(2, 2) { Position(it.x + 10, it.y + 10) }

        assertEquals(Position(1, 0), grid.positionOf(Position(11, 10)))
        assertEquals(Position(0, 1), grid.positionOfOrNull(Position(10, 11)))
        assertEquals(listOf(Position(1, 1)), grid.positionsOf(Position(11, 11)))
    }

    @Test
    fun `positionOf family finds a null cell`() {
        val grid = nullThenA()

        assertEquals(Position(0, 0), grid.positionOf(null))
        assertEquals(Position(0, 0), grid.positionOfOrNull(null))
        assertEquals(listOf(Position(0, 0)), grid.positionsOf(null))
    }

    @Test
    fun `positionOf family answers no match for a value of another type`() {
        val grid: Grid<Any> = charGrid("abc", "def")

        assertNull(grid.positionOfOrNull("a"))
        assertTrue(grid.positionsOf(97).isEmpty())
        assertFailsWith<NoSuchElementException> { grid.positionOf("a") }
    }

    @Test
    fun `positions lists every position in reading order`() {
        val grid = charGrid("abc", "def")

        assertEquals(readingOrder3x2, grid.positions.toList())
    }

    @Test
    fun `positions has one element per cell and reads them by index`() {
        val positions = charGrid("abc", "def").positions

        assertEquals(6, positions.size)
        assertEquals(Position(0, 0), positions[0])
        assertEquals(Position(2, 0), positions[2])
        assertEquals(Position(0, 1), positions[3])
        assertEquals(Position(2, 1), positions[5])
    }

    @Test
    fun `positions of a single row go right and of a single column go down`() {
        val row = charGrid("abc")
        val column = charGrid("a", "b", "c")

        assertEquals(listOf(Position(0, 0), Position(1, 0), Position(2, 0)), row.positions.toList())
        assertEquals(listOf(Position(0, 0), Position(0, 1), Position(0, 2)), column.positions.toList())
    }

    @Test
    fun `positions contains and indexOf find a position inside`() {
        val positions = charGrid("abc", "def").positions

        for ((index, position) in readingOrder3x2.withIndex()) {
            assertTrue(position in positions, "$position")
            assertEquals(index, positions.indexOf(position), "$position")
        }
    }

    @Test
    fun `positions contains answers false for a position outside`() {
        val positions = charGrid("abc", "def").positions

        for (position in outside3x2) {
            assertFalse(position in positions, "$position")
        }
    }

    @Test
    fun `positions indexOf answers minus one for a position outside`() {
        val positions = charGrid("abc", "def").positions

        for (position in outside3x2) {
            assertEquals(-1, positions.indexOf(position), "$position")
        }
    }

    @Test
    fun `positions lastIndexOf agrees with indexOf`() {
        val positions = charGrid("abc", "def").positions

        assertEquals(4, positions.lastIndexOf(Position(1, 1)))
        assertEquals(-1, positions.lastIndexOf(Position(3, 0)))
        assertEquals(-1, positions.lastIndexOf(Position(-1, 1)))
    }

    @Test
    fun `positions of a grid without cells find nothing`() {
        val zeroWidth = Grid(0, 2) { '.' }.positions
        val zeroHeight = Grid(3, 0) { '.' }.positions

        assertFalse(Position(0, 0) in zeroWidth)
        assertEquals(-1, zeroWidth.indexOf(Position(0, 1)))
        assertFalse(Position(0, 0) in zeroHeight)
        assertEquals(-1, zeroHeight.indexOf(Position(2, 0)))
    }

    @Test
    fun `positions stay the same when cells are written`() {
        val grid = mutableCharGrid("ab", "cd")
        val positions = grid.positions

        grid[Position(0, 0)] = 'x'

        assertEquals(
            listOf(Position(0, 0), Position(1, 0), Position(0, 1), Position(1, 1)),
            positions.toList(),
        )
    }

    @Test
    fun `values lists every value in reading order`() {
        val values = charGrid("abc", "def").values

        assertEquals(6, values.size)
        assertEquals(listOf('a', 'b', 'c', 'd', 'e', 'f'), values.toList())
    }

    @Test
    fun `values answers questions about the cells`() {
        val grid = charGrid(".#.", "#.#")

        assertTrue('#' in grid.values)
        assertFalse('@' in grid.values)
        assertTrue(grid.values.any { it == '.' })
        assertEquals(3, grid.values.count { it == '#' })
        assertEquals(1, grid.values.indexOf('#'))
        assertEquals(12, numbers3x2().values.max())
    }

    @Test
    fun `values and positions line up index by index`() {
        val grid = numbers3x2()

        for (index in 0..<6) {
            assertEquals(grid[grid.positions[index]], grid.values[index], "index $index")
        }
    }

    @Test
    fun `values is a live view of a MutableGrid`() {
        val grid = mutableCharGrid("ab", "cd")
        val values = grid.values

        grid[Position(1, 0)] = 'x'

        assertEquals(listOf('a', 'x', 'c', 'd'), values.toList())
        assertEquals('x', values[1])
        assertTrue('x' in values)
        assertFalse('b' in values)
    }

    @Test
    fun `rows lists the rows from top to bottom each from left to right`() {
        val rows = charGrid("abc", "def").rows

        assertEquals(2, rows.size)
        assertEquals(listOf(listOf('a', 'b', 'c'), listOf('d', 'e', 'f')), rows.map { it.toList() })
        assertEquals(3, rows[0].size)
        assertEquals('f', rows[1][2])
    }

    @Test
    fun `rows of a Grid of Char join into the lines`() {
        val grid = charGrid("abc", "def")

        assertEquals(listOf("abc", "def"), grid.rows.map { it.joinToString("") })
    }

    @Test
    fun `columns lists the columns from left to right each from top to bottom`() {
        val columns = charGrid("abc", "def").columns

        assertEquals(3, columns.size)
        assertEquals(
            listOf(listOf('a', 'd'), listOf('b', 'e'), listOf('c', 'f')),
            columns.map { it.toList() },
        )
        assertEquals(2, columns[0].size)
        assertEquals('f', columns[2][1])
    }

    @Test
    fun `rows are live views of a MutableGrid`() {
        val grid = mutableCharGrid("ab", "cd")
        val rows = grid.rows
        val bottom = rows[1]

        grid[Position(0, 1)] = 'x'

        assertEquals(listOf('x', 'd'), bottom.toList())
        assertEquals(listOf('x', 'd'), rows[1].toList())
        assertEquals(listOf('a', 'b'), rows[0].toList())
    }

    @Test
    fun `columns are live views of a MutableGrid`() {
        val grid = mutableCharGrid("ab", "cd")
        val columns = grid.columns
        val right = columns[1]

        grid[Position(1, 0)] = 'x'

        assertEquals(listOf('x', 'd'), right.toList())
        assertEquals(listOf('x', 'd'), columns[1].toList())
        assertEquals(listOf('a', 'c'), columns[0].toList())
    }

    @Test
    fun `a row equals a plain list with the same elements in both directions`() {
        val row = charGrid("abc", "def").rows[1]
        val plain = listOf('d', 'e', 'f')

        assertTrue(row == plain)
        assertTrue(plain == row)
    }

    @Test
    fun `a row has the hashCode of a plain list with the same elements`() {
        val row = charGrid("abc", "def").rows[1]

        assertEquals(listOf('d', 'e', 'f').hashCode(), row.hashCode())
    }

    @Test
    fun `a row differs from a plain list with other elements`() {
        val row = charGrid("abc", "def").rows[0]

        assertFalse(row == listOf('a', 'b', 'x'))
        assertFalse(listOf('a', 'b', 'x') == row)
        assertFalse(row == listOf('c', 'b', 'a'))
        assertFalse(row == listOf('a', 'b'))
        assertFalse(row == listOf('a', 'b', 'c', 'd'))
        assertFalse(row == listOf('d', 'e', 'f'))
    }

    @Test
    fun `a column equals a plain list with the same elements in both directions`() {
        val column = charGrid("abc", "def").columns[1]
        val plain = listOf('b', 'e')

        assertTrue(column == plain)
        assertTrue(plain == column)
    }

    @Test
    fun `a column has the hashCode of a plain list with the same elements`() {
        val column = charGrid("abc", "def").columns[1]

        assertEquals(listOf('b', 'e').hashCode(), column.hashCode())
    }

    @Test
    fun `a column differs from a plain list with other elements`() {
        val column = charGrid("abc", "def").columns[0]

        assertFalse(column == listOf('a', 'x'))
        assertFalse(listOf('a', 'x') == column)
        assertFalse(column == listOf('d', 'a'))
        assertFalse(column == listOf('a'))
        assertFalse(column == listOf('a', 'd', 'a'))
        assertFalse(column == listOf('a', 'b'))
    }

    @Test
    fun `a row and a column with the same elements are equal and hash alike`() {
        val grid = charGrid("ab", "bc")
        val row = grid.rows[0]
        val column = grid.columns[0]

        assertTrue(row == column)
        assertTrue(column == row)
        assertEquals(row.hashCode(), column.hashCode())
    }

    @Test
    fun `rows and columns work as set elements`() {
        val grid = charGrid("ab", "cd", "ab")

        assertEquals(2, grid.rows.toSet().size)
        assertTrue(grid.rows[2] in setOf(listOf('a', 'b')))
        assertTrue(listOf('c', 'd') in grid.rows.toSet())
        assertTrue(listOf('a', 'c', 'a') in grid.columns.toSet())
    }

    @Test
    fun `an empty row and an empty column equal an empty list`() {
        val row = Grid(0, 2) { '.' }.rows[0]
        val column = Grid(3, 0) { '.' }.columns[0]
        val plain = emptyList<Char>()

        assertTrue(row == plain)
        assertTrue(plain == row)
        assertEquals(plain.hashCode(), row.hashCode())
        assertTrue(column == plain)
        assertTrue(plain == column)
        assertEquals(plain.hashCode(), column.hashCode())
    }

    @Test
    fun `rows throws IndexOutOfBoundsException for an index outside`() {
        val rows = charGrid("abc", "def").rows

        assertFailsWith<IndexOutOfBoundsException> { rows[2] }
        assertFailsWith<IndexOutOfBoundsException> { rows[-1] }
    }

    @Test
    fun `a row throws IndexOutOfBoundsException for an index outside`() {
        val rows = charGrid("abc", "def").rows

        assertFailsWith<IndexOutOfBoundsException> { rows[0][3] }
        assertFailsWith<IndexOutOfBoundsException> { rows[0][-1] }
        assertFailsWith<IndexOutOfBoundsException> { rows[1][3] }
        assertFailsWith<IndexOutOfBoundsException> { rows[1][-1] }
    }

    @Test
    fun `columns throws IndexOutOfBoundsException for an index outside`() {
        val columns = charGrid("abc", "def").columns

        assertFailsWith<IndexOutOfBoundsException> { columns[3] }
        assertFailsWith<IndexOutOfBoundsException> { columns[-1] }
    }

    @Test
    fun `a column throws IndexOutOfBoundsException for an index outside`() {
        val columns = charGrid("abc", "def").columns

        assertFailsWith<IndexOutOfBoundsException> { columns[0][2] }
        assertFailsWith<IndexOutOfBoundsException> { columns[0][-1] }
        assertFailsWith<IndexOutOfBoundsException> { columns[2][2] }
        assertFailsWith<IndexOutOfBoundsException> { columns[2][-1] }
    }

    @Test
    fun `row and column index limits follow width and height in a non square grid`() {
        val grid = charGrid("ab", "cd", "ef")

        assertEquals('e', grid.columns[0][2])
        assertFailsWith<IndexOutOfBoundsException> { grid.rows[0][2] }
        assertEquals(listOf('e', 'f'), grid.rows[2].toList())
        assertFailsWith<IndexOutOfBoundsException> { grid.columns[2] }
    }

    @Test
    fun `positions throws IndexOutOfBoundsException for an index outside`() {
        val positions = charGrid("abc", "def").positions

        assertFailsWith<IndexOutOfBoundsException> { positions[-1] }
        assertFailsWith<IndexOutOfBoundsException> { positions[6] }
    }

    @Test
    fun `values throws IndexOutOfBoundsException for an index outside`() {
        val values = charGrid("abc", "def").values

        assertFailsWith<IndexOutOfBoundsException> { values[-1] }
        assertFailsWith<IndexOutOfBoundsException> { values[6] }
    }

    @Test
    fun `ray throws IndexOutOfBoundsException for an index outside`() {
        val grid = charGrid("abc", "def")
        val right = grid.ray(Position(0, 0), Direction.RIGHT)
        val diagonal = grid.ray(Position(0, 0), Direction8.DOWN_RIGHT)
        val empty = grid.ray(Position(2, 0), Direction.RIGHT)

        assertEquals(2, right.size)
        assertFailsWith<IndexOutOfBoundsException> { right[2] }
        assertFailsWith<IndexOutOfBoundsException> { right[-1] }
        assertEquals(1, diagonal.size)
        assertFailsWith<IndexOutOfBoundsException> { diagonal[1] }
        assertFailsWith<IndexOutOfBoundsException> { diagonal[-1] }
        assertEquals(0, empty.size)
        assertFailsWith<IndexOutOfBoundsException> { empty[0] }
    }

    @Test
    fun `views of a 0 x 2 grid throw IndexOutOfBoundsException for index zero`() {
        val grid = Grid(0, 2) { '.' }

        assertFailsWith<IndexOutOfBoundsException> { grid.positions[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.values[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.columns[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.rows[0][0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.rows[2] }
    }

    @Test
    fun `views of a 3 x 0 grid throw IndexOutOfBoundsException for index zero`() {
        val grid = Grid(3, 0) { '.' }

        assertFailsWith<IndexOutOfBoundsException> { grid.positions[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.values[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.rows[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.columns[0][0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.columns[3] }
    }

    @Test
    fun `views of a 0 x 0 grid throw IndexOutOfBoundsException for index zero`() {
        val grid = Grid(0, 0) { '.' }

        assertFailsWith<IndexOutOfBoundsException> { grid.positions[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.values[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.rows[0] }
        assertFailsWith<IndexOutOfBoundsException> { grid.columns[0] }
    }

    @Test
    fun `grids with the same size and values are equal`() {
        assertEquals(numbers3x2(), numbers3x2())
    }

    @Test
    fun `a read only grid and a mutable grid with the same values are equal in both directions`() {
        val readOnly = numbers3x2()
        val mutable = MutableGrid(3, 2) { it.x + 10 * it.y }

        assertTrue(readOnly == mutable)
        assertTrue(mutable == readOnly)
    }

    @Test
    fun `two mutable grids with the same values are equal`() {
        assertEquals(mutableCharGrid("ab", "cd"), mutableCharGrid("ab", "cd"))
    }

    @Test
    fun `equal grids have the same hashCode`() {
        val readOnly = numbers3x2()
        val mutable = MutableGrid(3, 2) { it.x + 10 * it.y }

        assertEquals(readOnly.hashCode(), numbers3x2().hashCode())
        assertEquals(readOnly.hashCode(), mutable.hashCode())
    }

    @Test
    fun `grids that differ in one value are not equal`() {
        val grid = charGrid("abc", "def")

        assertNotEquals(grid, charGrid("xbc", "def"))
        assertNotEquals(grid, charGrid("abc", "dxf"))
        assertNotEquals(grid, charGrid("abc", "dex"))
        assertNotEquals(charGrid("abc", "dex"), grid)
    }

    @Test
    fun `a mutable grid compares by its current values`() {
        val grid = MutableGrid(2, 2) { 0 }
        val target = Grid(2, 2) { if (it == Position(1, 0)) 5 else 0 }
        assertNotEquals(target, grid)

        grid[Position(1, 0)] = 5

        assertEquals(target, grid)
        assertEquals(target.hashCode(), grid.hashCode())
    }

    @Test
    fun `a 2 x 3 grid and a 3 x 2 grid with the same values in reading order are not equal`() {
        val tall = Grid(2, 3) { it.x + 2 * it.y }
        val wide = Grid(3, 2) { it.x + 3 * it.y }

        assertEquals(listOf(0, 1, 2, 3, 4, 5), tall.values.toList())
        assertEquals(listOf(0, 1, 2, 3, 4, 5), wide.values.toList())
        assertNotEquals(wide, tall)
        assertNotEquals(tall, wide)
    }

    @Test
    fun `a single row and a single column with the same values are not equal`() {
        assertNotEquals(charGrid("abc"), charGrid("a", "b", "c"))
    }

    @Test
    fun `grids without cells are equal only when width and height match`() {
        val empty = Grid(0, 0) { '.' }
        val zeroWidth = Grid(0, 2) { '.' }
        val zeroHeight = Grid(3, 0) { '.' }

        assertEquals(empty, Grid(0, 0) { '.' })
        assertEquals(zeroWidth, Grid(0, 2) { '.' })
        assertEquals(zeroHeight, Grid(3, 0) { '.' })
        assertEquals(zeroWidth, MutableGrid(0, 2) { '.' })
        assertNotEquals(empty, zeroWidth)
        assertNotEquals(empty, zeroHeight)
        assertNotEquals(zeroWidth, zeroHeight)
        assertNotEquals(zeroWidth, Grid(0, 3) { '.' })
    }

    @Test
    fun `a grid is not equal to something that is not a grid`() {
        val grid = charGrid("ab", "cd")

        assertFalse(gridEquals(grid, grid.values))
        assertFalse(gridEquals(grid, grid.rows))
        assertFalse(gridEquals(grid, "ab\ncd"))
        assertFalse(gridEquals(grid, null))
    }

    @Test
    fun `toString of a Grid of Char prints the lines joined by line feeds`() {
        assertEquals("abc\ndef", charGrid("abc", "def").toString())
    }

    @Test
    fun `toString writes the toString of each value one after another`() {
        assertEquals("012\n101112", numbers3x2().toString())
    }

    @Test
    fun `toString of a single row has no line break`() {
        assertEquals("abc", charGrid("abc").toString())
    }

    @Test
    fun `toString of a single column puts every value on its own line`() {
        assertEquals("a\nb\nc", charGrid("a", "b", "c").toString())
    }

    @Test
    fun `toString of a 0 x 0 grid is empty`() {
        assertEquals("", Grid(0, 0) { '.' }.toString())
    }

    @Test
    fun `toString of a grid without rows is empty`() {
        assertEquals("", Grid(3, 0) { '.' }.toString())
    }

    @Test
    fun `toString of a 0 x 2 grid is the line feed between its two empty rows`() {
        assertEquals("\n", Grid(0, 2) { '.' }.toString())
    }

    @Test
    fun `toString of a MutableGrid shows later writes`() {
        val grid = mutableCharGrid("ab", "cd")

        grid[Position(1, 1)] = '#'

        assertEquals("ab\nc#", grid.toString())
    }

    @Test
    fun `toGrid of a MutableGrid has the same size and values`() {
        val grid = mutableCharGrid("abc", "def")
        val snapshot = grid.toGrid()

        assertEquals(3, snapshot.width)
        assertEquals(2, snapshot.height)
        assertEquals("abc\ndef", snapshot.toString())
        assertEquals(grid, snapshot)
    }

    @Test
    fun `toGrid snapshot is not affected by later writes to the source`() {
        val grid = mutableCharGrid("ab", "cd")
        val snapshot = grid.toGrid()

        grid[Position(0, 0)] = 'x'

        assertEquals('a', snapshot[Position(0, 0)])
        assertEquals(listOf('a', 'b', 'c', 'd'), snapshot.values.toList())
        assertEquals("ab\ncd", snapshot.toString())
        assertEquals("xb\ncd", grid.toString())
        assertNotEquals(grid, snapshot)
    }

    @Test
    fun `toGrid snapshot keeps its hashCode when the source is written`() {
        val grid = MutableGrid(2, 2) { 0 }
        val snapshot = grid.toGrid()
        val hashBefore = snapshot.hashCode()

        grid[Position(1, 1)] = 9

        assertEquals(hashBefore, snapshot.hashCode())
    }

    @Test
    fun `toGrid snapshots work as map keys while the source keeps changing`() {
        val grid = MutableGrid(2, 2) { 0 }
        val seen = mutableMapOf<Grid<Int>, String>()

        seen[grid.toGrid()] = "blank"
        grid[Position(1, 0)] = 5
        seen[grid.toGrid()] = "one five"
        grid[Position(1, 0)] = 0

        assertEquals(2, seen.size)
        assertEquals("blank", seen[grid])
        assertEquals("one five", seen[Grid(2, 2) { if (it == Position(1, 0)) 5 else 0 }])
    }

    @Test
    fun `toGrid of a MutableGrid seen as a Grid is still a snapshot`() {
        val mutable = MutableGrid(2, 2) { 0 }
        val seenAsGrid: Grid<Int> = mutable
        val snapshot = seenAsGrid.toGrid()

        mutable[Position(0, 0)] = 1

        assertEquals(0, snapshot[Position(0, 0)])
        assertEquals(1, seenAsGrid[Position(0, 0)])
    }

    @Test
    fun `toGrid of a read only grid is equal to it`() {
        val grid = charGrid("abc", "def")
        val snapshot = grid.toGrid()

        assertEquals(grid, snapshot)
        assertEquals(grid.hashCode(), snapshot.hashCode())
        assertFalse(isMutable(snapshot))
    }

    @Test
    fun `toMutableGrid copies the size and the values`() {
        val grid = charGrid("abc", "def")
        val copy = grid.toMutableGrid()

        assertEquals(3, copy.width)
        assertEquals(2, copy.height)
        assertEquals("abc\ndef", copy.toString())
        assertEquals(grid, copy)
    }

    @Test
    fun `writes to a toMutableGrid copy do not change a read only source`() {
        val grid = charGrid("ab", "cd")
        val copy = grid.toMutableGrid()

        copy[Position(1, 0)] = 'x'

        assertEquals("ax\ncd", copy.toString())
        assertEquals("ab\ncd", grid.toString())
    }

    @Test
    fun `a toMutableGrid copy of a MutableGrid is independent in both directions`() {
        val grid = mutableCharGrid("ab", "cd")
        val copy = grid.toMutableGrid()

        grid[Position(0, 0)] = 'x'
        copy[Position(1, 1)] = 'y'

        assertEquals("xb\ncd", grid.toString())
        assertEquals("ab\ncy", copy.toString())
    }

    @Test
    fun `snapshots and copies of a grid without cells keep its size`() {
        val zeroWidth = MutableGrid(0, 2) { '.' }
        val zeroHeight = MutableGrid(3, 0) { '.' }

        assertEquals(0, zeroWidth.toGrid().width)
        assertEquals(2, zeroWidth.toGrid().height)
        assertEquals(0, zeroWidth.toMutableGrid().width)
        assertEquals(2, zeroWidth.toMutableGrid().height)
        assertEquals(3, zeroHeight.toGrid().width)
        assertEquals(0, zeroHeight.toGrid().height)
        assertEquals(3, zeroHeight.toMutableGrid().width)
        assertEquals(0, zeroHeight.toMutableGrid().height)
    }

    @Test
    fun `factories reject more than Int MAX_VALUE cells`() {
        // 65536 * 65536 wraps to exactly 0 in Int arithmetic, so only a check in Long catches it
        assertFailsWith<IllegalArgumentException> { Grid(65536, 65536) { 0 } }
        assertFailsWith<IllegalArgumentException> { MutableGrid(65536, 65536) { 0 } }

        assertEquals(0, Grid(Int.MAX_VALUE, 0) { 0 }.values.size)
    }

    // Gathered outside the assertions: power-assert prints every value an assertion mentions, and a
    // 0 x Int.MAX_VALUE grid prints as two billion line breaks.
    private fun answersOfEmpty(grid: Grid<Int>): List<Int> = listOf(
        grid.values.size,
        grid.positions.size,
        grid.positionsOf(0).size,
        if (grid.positionOfOrNull(0) == null) 0 else 1,
        grid.rows.size - grid.height,
        grid.columns.size - grid.width,
        grid.toGrid().values.size,
        grid.toMutableGrid().values.size,
    )

    @Test
    fun `a grid without cells takes no step per row or per column`() {
        // A walk along the Int.MAX_VALUE side takes 6 to 20 seconds on JS, Wasm and Native; the JVM gets through
        // it in under a second, so there the time limit proves nothing.
        val (answers, elapsed) = measureTimedValue {
            listOf(
                answersOfEmpty(Grid(0, Int.MAX_VALUE) { 0 }),
                answersOfEmpty(Grid(Int.MAX_VALUE, 0) { 0 }),
                answersOfEmpty(MutableGrid(0, Int.MAX_VALUE) { 0 }),
                answersOfEmpty(MutableGrid(Int.MAX_VALUE, 0) { 0 }),
            )
        }

        assertEquals(List(4) { List(8) { 0 } }, answers)
        assertTrue(elapsed < 1.seconds, "grids without cells took $elapsed")
    }

    // Equal to every Int, while no Int is equal to it: tells apart which side of a comparison was asked.
    private class EqualToEveryInt {
        override fun equals(other: Any?): Boolean = other is Int || other is EqualToEveryInt

        override fun hashCode(): Int = 0
    }

    @Test
    fun `every search asks the cell whether it equals the value`() {
        val value = EqualToEveryInt()
        val grid = Grid<Any>(2, 1) { if (it.x == 0) 1 else value }

        // value == 1 is true and 1 == value is false, so only the second cell holds the value
        assertEquals(Position(1, 0), grid.positionOf(value))
        assertEquals(Position(1, 0), grid.positionOfOrNull(value))
        assertEquals(listOf(Position(1, 0)), grid.positionsOf(value))
        assertEquals(1, grid.values.indexOf(value))
    }

    @Test
    fun `views are RandomAccess and the functions that index them agree with iteration`() {
        val grid = MutableGrid(4, 3) { it.x + 10 * it.y }
        val views: List<List<Any>> = listOf(
            grid.positions, grid.values, grid.rows, grid.columns, grid.rows[1], grid.columns[2],
            grid.neighbors(Position(1, 1)), grid.neighbors8(Position(0, 0)), grid.ray(Position(0, 1), Direction.RIGHT),
        )

        for (view in views) {
            assertTrue(view is RandomAccess)
            val copy = iterated(view)
            assertEquals(copy.takeLast(2), view.takeLast(2))
            assertEquals(copy.windowed(2), view.windowed(2))
            assertEquals(copy.chunked(2), view.chunked(2))
            assertEquals(copy.asReversed(), view.asReversed())
        }
        assertEquals(listOf(listOf(10, 11), listOf(11, 12), listOf(12, 13)), grid.rows[1].windowed(2))
    }

    @Test
    fun `toGrid of a grid that is not a MutableGrid returns that grid`() {
        val grid = charGrid("ab", "cd")
        val snapshot = mutableCharGrid("ab", "cd").toGrid()

        assertSame(grid, grid.toGrid())
        assertSame(snapshot, snapshot.toGrid())
    }

    @Test
    fun `set by x and y and set by position fail with the same message`() {
        val grid = MutableGrid(3, 2) { 0 }

        val byPosition = assertFailsWith<IndexOutOfBoundsException> { grid[Position(3, 1)] = 1 }
        val byCoordinates = assertFailsWith<IndexOutOfBoundsException> { grid[3, 1] = 1 }

        assertEquals("Position(x=3, y=1) is outside the 3 x 2 grid", byCoordinates.message)
        assertEquals(byPosition.message, byCoordinates.message)
    }

    @Test
    fun `views of a grid are not mutable lists`() {
        for (grid in listOf(Grid(2, 2) { 0 }, MutableGrid(2, 2) { 0 })) {
            assertFalse(grid.values is MutableList<*>)
            assertFalse(grid.rows[0] is MutableList<*>)
            assertFalse(grid.columns[0] is MutableList<*>)
            assertFalse(grid.positions is MutableList<*>)
        }
    }

    @Test
    fun `toString prints a null cell as null`() {
        val grid = Grid<Char?>(2, 1) { if (it.x == 0) null else 'a' }

        assertEquals("nulla", grid.toString())
    }

    private fun <E> iterated(list: List<E>): List<E> {
        val result = mutableListOf<E>()
        for (element in list) result += element
        return result
    }

    private fun <E> indexed(list: List<E>): List<E> = List(list.size) { list[it] }

    @Test
    fun `iterating a view gives the same elements as indexing it`() {
        for ((width, height) in listOf(4 to 3, 3 to 4, 1 to 1, 1 to 5, 5 to 1, 0 to 0, 0 to 3, 3 to 0)) {
            val grid = MutableGrid(width, height) { it.x + 10 * it.y }
            val message = "a $width x $height grid"

            assertEquals(indexed(grid.positions), iterated(grid.positions), message)
            assertEquals(indexed(grid.values), iterated(grid.values), message)
            assertEquals(indexed(grid.rows), iterated(grid.rows), message)
            assertEquals(indexed(grid.columns), iterated(grid.columns), message)
            for (row in grid.rows) assertEquals(indexed(row), iterated(row), message)
            for (column in grid.columns) assertEquals(indexed(column), iterated(column), message)
        }
    }

    @Test
    fun `iterating the positions goes row by row from the top`() {
        assertEquals(readingOrder3x2, iterated(numbers3x2().positions))
        assertEquals(listOf(0, 1, 2, 10, 11, 12), iterated(numbers3x2().values))
        assertEquals(listOf(listOf(0, 1, 2), listOf(10, 11, 12)), iterated(numbers3x2().rows))
        assertEquals(listOf(listOf(0, 10), listOf(1, 11), listOf(2, 12)), iterated(numbers3x2().columns))
    }

    @Test
    fun `view iterators throw NoSuchElementException after the last element`() {
        for (grid in listOf(numbers3x2(), Grid(0, 2) { 0 }, Grid(3, 0) { 0 }, Grid(0, 0) { 0 })) {
            val views = mutableListOf<List<Any>>(grid.positions, grid.values, grid.rows, grid.columns)
            for (row in grid.rows) views += row
            for (column in grid.columns) views += column
            for (view in views) {
                val iterator = view.iterator()
                var count = 0
                while (iterator.hasNext()) {
                    assertEquals(view[count], iterator.next())
                    count++
                }
                assertEquals(view.size, count)
                assertFailsWith<NoSuchElementException> { iterator.next() }
            }
        }
    }

    @Test
    fun `an iterator of a view of a MutableGrid reads the cells as they are when it reaches them`() {
        val grid = MutableGrid(2, 2) { 0 }
        val values = grid.values.iterator()
        val firstRow = grid.rows[0].iterator()

        assertEquals(0, values.next())
        grid[Position(1, 0)] = 7

        assertEquals(7, values.next())
        assertEquals(listOf(0, 7), listOf(firstRow.next(), firstRow.next()))
    }

    @Test
    fun `views are equal on every access and keep showing later writes`() {
        val grid = MutableGrid(2, 2) { 0 }
        val rows = grid.rows
        val columns = grid.columns
        val values = grid.values

        grid[Position(1, 0)] = 5

        assertEquals(listOf(listOf(0, 5), listOf(0, 0)), rows)
        assertEquals(listOf(listOf(0, 0), listOf(5, 0)), columns)
        assertEquals(listOf(0, 5, 0, 0), values)
        assertEquals(rows, grid.rows)
        assertEquals(columns, grid.columns)
        assertEquals(values, grid.values)
        assertEquals(grid.positions, grid.positions)
    }

    @Test
    fun `toString writes String and other CharSequence values as they are`() {
        val cells = listOf<CharSequence>("ab", StringBuilder("cd"), "", "e")
        val grid = Grid(2, 2) { cells[it.y * 2 + it.x] }

        assertEquals("abcd\ne", grid.toString())
    }

    @Test
    fun `toString of a wide Grid of Char keeps every row and its order`() {
        val lines = List(7) { y -> CharArray(11) { x -> 'a' + (x + 3 * y) % 26 }.concatToString() }
        val grid = Grid(11, 7) { lines[it.y][it.x] }

        assertEquals(lines.joinToString("\n"), grid.toString())
        assertEquals(lines.joinToString("\n"), grid.rows.joinToString("\n") { it.joinToString("") })
    }
}
