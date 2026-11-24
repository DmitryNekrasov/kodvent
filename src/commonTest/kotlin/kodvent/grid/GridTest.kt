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

    private fun throwsIndexOutOfBounds(block: () -> Any?): Boolean =
        runCatching(block).exceptionOrNull() is IndexOutOfBoundsException

    private fun throwsIllegalArgument(block: () -> Any?): Boolean =
        runCatching(block).exceptionOrNull() is IllegalArgumentException

    private fun <E> iterated(list: List<E>): List<E> {
        val result = mutableListOf<E>()
        for (element in list) result += element
        return result
    }

    private fun <E> indexed(list: List<E>): List<E> = List(list.size) { list[it] }

    @Test
    fun `factories call init once for every position in reading order and store what it returns`() {
        val calls = mutableListOf<Position>()
        val init: (Position) -> Int = {
            calls += it
            it.x + 10 * it.y
        }

        for (grid in listOf(Grid(3, 2, init), MutableGrid(3, 2, init))) {
            assertEquals(3, grid.width)
            assertEquals(2, grid.height)
            assertEquals(listOf(0, 1, 2, 10, 11, 12), readingOrder3x2.map { grid[it] })
        }
        assertEquals(readingOrder3x2 + readingOrder3x2, calls)
    }

    @Test
    fun `a grid with a zero width or height has no cells and its factory never calls init`() {
        var calls = 0
        val init: (Position) -> Char = {
            calls++
            '.'
        }

        for ((width, height) in listOf(0 to 0, 0 to 2, 3 to 0)) {
            for (grid in listOf(Grid(width, height, init), MutableGrid(width, height, init))) {
                assertEquals(width, grid.width)
                assertEquals(height, grid.height)
                assertTrue(grid.positions.isEmpty())
                assertTrue(grid.values.isEmpty())
                assertEquals(List(height) { emptyList<Char>() }, grid.rows)
                assertEquals(List(width) { emptyList<Char>() }, grid.columns)
            }
        }
        assertEquals(0, calls)
    }

    @Test
    fun `factories throw IllegalArgumentException for a negative width or height`() {
        val sizes = listOf(-1 to 2, 2 to -1, -1 to 0, 0 to -1, -2 to -3)

        assertEquals(emptyList(), sizes.filterNot { (w, h) -> throwsIllegalArgument { Grid(w, h) { '.' } } })
        assertEquals(emptyList(), sizes.filterNot { (w, h) -> throwsIllegalArgument { MutableGrid(w, h) { '.' } } })
    }

    @Test
    fun `factories reject more cells than an Int can count`() {
        // 65536 * 65536 wraps to exactly 0 in Int arithmetic, so only a check in Long catches it
        assertFailsWith<IllegalArgumentException> { Grid(65536, 65536) { 0 } }
        assertFailsWith<IllegalArgumentException> { MutableGrid(65536, 65536) { 0 } }

        assertEquals(0, Grid(Int.MAX_VALUE, 0) { 0 }.values.size)
    }

    @Test
    fun `everything returned as a Grid is not a MutableGrid`() {
        val mutable = MutableGrid(2, 2) { 0 }

        assertFalse(isMutable(Grid(2, 2) { 0 }))
        assertFalse(isMutable(mutable.toGrid()))
        assertFalse(isMutable(listOf("ab", "cd").toGrid()))
        assertFalse(isMutable(listOf("12", "34").toGrid { it.digitToInt() }))
        assertFalse(isMutable(mutable.map { it + 1 }))
        assertFalse(isMutable(mutable.mapIndexed { position, value -> position.x + value }))
        assertFalse(isMutable(mutable.transposed()))
        assertFalse(isMutable(mutable.rotatedClockwise()))
        assertFalse(isMutable(mutable.rotatedCounterclockwise()))
    }

    @Test
    fun `get outside the grid throws IndexOutOfBoundsException by position and by x and y`() {
        val grid = charGrid("abc", "def")

        assertEquals(emptyList(), outside3x2.filterNot { throwsIndexOutOfBounds { grid[it] } })
        assertEquals(emptyList(), outside3x2.filterNot { throwsIndexOutOfBounds { grid[it.x, it.y] } })
    }

    @Test
    fun `set overwrites one cell and leaves the others`() {
        val grid = MutableGrid(3, 2) { 0 }

        grid[Position(1, 0)] = 5
        grid[Position(0, 1)] += 7
        grid[Position(2, 1)]++

        assertEquals(5, grid[Position(1, 0)])
        assertEquals(7, grid[Position(0, 1)])
        assertEquals(listOf(0, 5, 0, 7, 0, 1), grid.values.toList())
    }

    @Test
    fun `set outside the grid throws IndexOutOfBoundsException and writes nothing by position and by x and y`() {
        val grid = MutableGrid(3, 2) { 0 }

        assertEquals(emptyList(), outside3x2.filterNot { throwsIndexOutOfBounds { grid[it] = 1 } })
        assertEquals(emptyList(), outside3x2.filterNot { throwsIndexOutOfBounds { grid[it.x, it.y] = 1 } })
        assertEquals(listOf(0, 0, 0, 0, 0, 0), grid.values)
    }

    @Test
    fun `get by x and y reads the cell that get by position reads`() {
        for ((width, height) in listOf(3 to 2, 2 to 3, 1 to 1, 1 to 4, 4 to 1)) {
            val grid = Grid(width, height) { it.x + 10 * it.y }
            for (position in grid.positions) assertEquals(grid[position], grid[position.x, position.y])
        }
        assertEquals('f', charGrid("abc", "def")[2, 1])
        assertEquals('d', charGrid("abc", "def")[0, 1])
    }

    @Test
    fun `get and set by x and y fail with the message of get and set by position`() {
        val grid = MutableGrid(3, 2) { 0 }

        for ((x, y) in listOf(3 to 1, 0 to 2)) {
            val expected = "Position(x=$x, y=$y) is outside the 3 x 2 grid"

            assertEquals(expected, assertFailsWith<IndexOutOfBoundsException> { grid[Position(x, y)] }.message)
            assertEquals(expected, assertFailsWith<IndexOutOfBoundsException> { grid[x, y] }.message)
            assertEquals(expected, assertFailsWith<IndexOutOfBoundsException> { grid[Position(x, y)] = 1 }.message)
            assertEquals(expected, assertFailsWith<IndexOutOfBoundsException> { grid[x, y] = 1 }.message)
        }
    }

    @Test
    fun `get and set by x and y outside a grid taller than wide throw IndexOutOfBoundsException`() {
        // A wide grid cannot show width and height swapped in the bounds test of these overloads: a cell that the
        // swapped test lets through lies past the last one, and there the list of cells throws the same exception
        val tall = MutableGrid(2, 3) { 0 }

        assertFailsWith<IndexOutOfBoundsException> { tall[2, 0] }
        assertFailsWith<IndexOutOfBoundsException> { tall[2, 0] = 1 }
        assertEquals(listOf(0, 0, 0, 0, 0, 0), tall.values)
    }

    @Test
    fun `set by x and y writes the cell that get by position reads`() {
        val grid = MutableGrid(3, 2) { 0 }

        grid[1, 0] = 5
        grid[0, 1] += 7
        grid[2, 1]++

        assertEquals(5, grid[Position(1, 0)])
        assertEquals(7, grid[Position(0, 1)])
        assertEquals(listOf(0, 5, 0, 7, 0, 1), grid.values)
    }

    @Test
    fun `position in grid is true for every cell and false for every position outside`() {
        val grid = charGrid("abc", "def")

        for (position in readingOrder3x2) assertTrue(position in grid)
        for (position in outside3x2) assertFalse(position in grid)
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
    fun `position in grid never looks at the values`() {
        val positions = Grid(2, 2) { Position(it.x + 10, it.y + 10) }
        val withNull = nullThenA()

        assertTrue(Position(1, 1) in positions)
        assertFalse(Position(1, 1) in positions.values)
        assertFalse(Position(11, 11) in positions)
        assertTrue(Position(11, 11) in positions.values)
        assertNull(withNull[Position(0, 0)])
        assertTrue(Position(0, 0) in withNull)
    }

    @Test
    fun `getOrNull getOrDefault and getOrElse return the value inside and fall back outside`() {
        val grid = charGrid("abc", "def")
        val received = mutableListOf<Position>()
        val fallback: (Position) -> Char = {
            received += it
            '.'
        }

        for (position in readingOrder3x2) {
            assertEquals(grid[position], grid.getOrNull(position))
            assertEquals(grid[position], grid.getOrDefault(position, '.'))
            assertEquals(grid[position], grid.getOrElse(position, fallback))
        }
        assertEquals(emptyList(), received)

        for (position in outside3x2) {
            assertNull(grid.getOrNull(position))
            assertEquals('.', grid.getOrDefault(position, '.'))
            assertEquals('.', grid.getOrElse(position, fallback))
        }
        assertEquals(outside3x2, received)
    }

    @Test
    fun `getOrDefault and getOrElse keep a null cell inside null`() {
        val grid = nullThenA()

        assertNull(grid.getOrDefault(Position(0, 0), 'z'))
        assertEquals('a', grid.getOrDefault(Position(1, 0), 'z'))
        assertEquals('z', grid.getOrDefault(Position(2, 0), 'z'))
        assertNull(grid.getOrElse(Position(0, 0)) { 'z' })
    }

    @Test
    fun `positionOf and positionOfOrNull return the first match in reading order`() {
        val grid = charGrid(".#.", "#.#")

        assertEquals(Position(1, 0), grid.positionOf('#'))
        assertEquals(Position(0, 0), grid.positionOf('.'))
        assertEquals(Position(1, 0), grid.positionOfOrNull('#'))
        assertEquals(Position(0, 0), grid.positionOfOrNull('.'))
    }

    @Test
    fun `positionOf throws NoSuchElementException when no cell holds the value`() {
        val grid = charGrid(".#.", "#.#")

        assertFailsWith<NoSuchElementException> { grid.positionOf('@') }
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
    fun `positions contains indexOf and lastIndexOf follow the bounds`() {
        val positions = charGrid("abc", "def").positions
        val zeroWidth = Grid(0, 2) { '.' }.positions
        val zeroHeight = Grid(3, 0) { '.' }.positions

        for ((index, position) in readingOrder3x2.withIndex()) {
            assertTrue(position in positions)
            assertEquals(index, positions.indexOf(position))
            assertEquals(index, positions.lastIndexOf(position))
        }
        for (position in outside3x2) {
            assertFalse(position in positions)
            assertEquals(-1, positions.indexOf(position))
            assertEquals(-1, positions.lastIndexOf(position))
        }
        assertFalse(Position(0, 0) in zeroWidth)
        assertEquals(-1, zeroWidth.indexOf(Position(0, 1)))
        assertFalse(Position(0, 0) in zeroHeight)
        assertEquals(-1, zeroHeight.indexOf(Position(2, 0)))
    }

    @Test
    fun `values rows and columns are live views of a MutableGrid`() {
        val grid = mutableCharGrid("ab", "cd")
        val values = grid.values
        val bottom = grid.rows[1]
        val right = grid.columns[1]
        val iteratedRows = iterated(grid.rows)

        grid[Position(1, 1)] = 'x'

        assertEquals(listOf('a', 'b', 'c', 'x'), values)
        assertEquals(listOf('c', 'x'), bottom)
        assertEquals(listOf('b', 'x'), right)
        assertEquals(listOf(listOf('a', 'b'), listOf('c', 'x')), iteratedRows)
    }

    @Test
    fun `a row and a column have List equality and hashCode`() {
        val grid = charGrid("abc", "def")
        val row = grid.rows[1]
        val column = grid.columns[1]

        assertTrue(row == listOf('d', 'e', 'f'))
        assertTrue(listOf('d', 'e', 'f') == row)
        assertEquals(listOf('d', 'e', 'f').hashCode(), row.hashCode())
        assertTrue(column == listOf('b', 'e'))
        assertTrue(listOf('b', 'e') == column)
        assertEquals(listOf('b', 'e').hashCode(), column.hashCode())
        assertFalse(row == listOf('d', 'e', 'x'))
    }

    @Test
    fun `every view throws IndexOutOfBoundsException for an index outside`() {
        val grids = listOf(
            charGrid("abc", "def"), charGrid("ab", "cd", "ef"),
            Grid(0, 2) { '.' }, Grid(3, 0) { '.' }, Grid(0, 0) { '.' },
        )

        for (grid in grids) {
            val views: List<List<Any>> = listOf(grid.positions, grid.values, grid.rows, grid.columns) +
                grid.rows + grid.columns
            for (view in views) {
                assertFailsWith<IndexOutOfBoundsException> { view[-1] }
                assertFailsWith<IndexOutOfBoundsException> { view[view.size] }
            }
        }
    }

    @Test
    fun `a read only grid and a mutable grid with the same values are equal in both directions`() {
        val readOnly = numbers3x2()
        val mutable = MutableGrid(3, 2) { it.x + 10 * it.y }

        assertTrue(readOnly == mutable)
        assertTrue(mutable == readOnly)
    }

    @Test
    fun `grids that differ in one value are not equal`() {
        val grid = charGrid("abc", "def")

        assertNotEquals(grid, charGrid("xbc", "def"))
        assertNotEquals(grid, charGrid("abc", "dxf"))
        assertNotEquals(grid, charGrid("abc", "dex"))
        assertNotEquals(charGrid("abc", "dex"), grid)
        assertNotEquals<Grid<Any>>(Grid(1, 1) { "1" }, Grid(1, 1) { 1 }) // both print as 1
    }

    @Test
    fun `a mutable grid compares by its current values`() {
        val grid = MutableGrid(2, 2) { 0 }
        val blank = Grid(2, 2) { 0 }
        val target = Grid(2, 2) { if (it == Position(1, 0)) 5 else 0 }
        assertNotEquals(target, grid)
        assertEquals(blank.hashCode(), grid.hashCode())
        assertEquals(blank.toString(), grid.toString())

        grid[Position(1, 0)] = 5

        assertEquals(target, grid)
        assertEquals(target.hashCode(), grid.hashCode())
        assertEquals(target.toString(), grid.toString())
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
    fun `toString joins the rows with line feeds`() {
        assertEquals("abc\ndef", charGrid("abc", "def").toString())
        assertEquals("a\nb\nc", charGrid("a", "b", "c").toString())
        assertEquals("", Grid(0, 0) { '.' }.toString())
        assertEquals("", Grid(3, 0) { '.' }.toString())
        assertEquals("\n", Grid(0, 2) { '.' }.toString())
    }

    @Test
    fun `toString writes each value as its toString`() {
        val words = listOf<CharSequence>("ab", StringBuilder("cd"), "", "e")
        // StringBuilder.append(CharSequence) writes such a value as its characters on JVM, Native and wasmWasi
        // and as its toString() on JS and wasmJs
        val cell = object : CharSequence by "characters" {
            override fun toString(): String = "toString"
        }

        assertEquals("012\n101112", numbers3x2().toString())
        assertEquals("nulla", nullThenA().toString())
        assertEquals("abcd\ne", Grid(2, 2) { words[it.y * 2 + it.x] }.toString())
        assertEquals("toString", Grid<CharSequence>(1, 1) { cell }.toString())
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
    fun `toGrid of a MutableGrid seen as a Grid is still a snapshot`() {
        val mutable = MutableGrid(2, 2) { 0 }
        val seenAsGrid: Grid<Int> = mutable
        val snapshot = seenAsGrid.toGrid()

        mutable[Position(0, 0)] = 1

        assertEquals(0, snapshot[Position(0, 0)])
        assertEquals(1, seenAsGrid[Position(0, 0)])
    }

    @Test
    fun `toGrid of a grid that is not a MutableGrid returns that grid`() {
        val grid = charGrid("ab", "cd")
        val snapshot = mutableCharGrid("ab", "cd").toGrid()

        assertSame(grid, grid.toGrid())
        assertSame(snapshot, snapshot.toGrid())
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
    fun `writes to a toMutableGrid copy do not change the source`() {
        for (source in listOf(charGrid("ab", "cd"), mutableCharGrid("ab", "cd"))) {
            val copy = source.toMutableGrid()

            copy[Position(1, 0)] = 'x'

            assertEquals("ax\ncd", copy.toString())
            assertEquals("ab\ncd", source.toString())
        }
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
        grid.toGrid().width - grid.width,
        grid.toGrid().height - grid.height,
        grid.toMutableGrid().width - grid.width,
        grid.toMutableGrid().height - grid.height,
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

        assertEquals(List(4) { List(12) { 0 } }, answers)
        assertTrue(elapsed < 1.seconds)
    }

    @Test
    fun `views are RandomAccess`() {
        val grid = MutableGrid(4, 3) { it.x + 10 * it.y }
        val views: List<List<Any>> = listOf(
            grid.positions, grid.values, grid.rows, grid.columns, grid.rows[1], grid.columns[2],
            grid.neighbors(Position(1, 1)), grid.neighbors8(Position(0, 0)), grid.ray(Position(0, 1), Direction.RIGHT),
        )

        for (view in views) assertTrue(view is RandomAccess)
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
    fun `iterating a view gives the same elements as indexing it`() {
        for ((width, height) in listOf(4 to 3, 3 to 4, 1 to 1, 1 to 5, 5 to 1, 0 to 0, 0 to 3, 3 to 0)) {
            val grid = MutableGrid(width, height) { it.x + 10 * it.y }

            assertEquals(indexed(grid.positions), iterated(grid.positions))
            assertEquals(indexed(grid.values), iterated(grid.values))
            assertEquals(indexed(grid.rows), iterated(grid.rows))
            assertEquals(indexed(grid.columns), iterated(grid.columns))
            for (row in grid.rows) assertEquals(indexed(row), iterated(row))
            for (column in grid.columns) assertEquals(indexed(column), iterated(column))
        }
    }

    @Test
    fun `views iterate in reading order`() {
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
}
