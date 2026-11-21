/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class GridExtensionsTest {

    private fun abcDef(): Grid<Char> = listOf("abc", "def").toGrid()

    private fun numbers4x3(): Grid<Int> = Grid(4, 3) { it.x + 10 * it.y }

    private fun doubledUnlessNegative(grid: Grid<Int>): Grid<Int>? {
        return grid.map { value ->
            if (value < 0) return null
            value * 2
        }
    }

    private fun coordinateSumsUnlessWall(grid: Grid<Char>): Grid<Int>? {
        return grid.mapIndexed { position, value ->
            if (value == '#') return null
            position.x + position.y
        }
    }

    @Test
    fun `neighbors of an inner cell are all four in Direction entries order`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(
            listOf(Position(1, 0), Position(2, 1), Position(1, 2), Position(0, 1)),
            grid.neighbors(Position(1, 1)),
        )
    }

    @Test
    fun `neighbors of a corner leave out the positions outside`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(listOf(Position(1, 0), Position(0, 1)), grid.neighbors(Position(0, 0)))
        assertEquals(listOf(Position(2, 1), Position(1, 2)), grid.neighbors(Position(2, 2)))
    }

    @Test
    fun `neighbors in a non square grid respect both sizes`() {
        val grid = numbers4x3()
        assertEquals(listOf(Position(3, 1), Position(2, 2)), grid.neighbors(Position(3, 2)))
        assertEquals(listOf(Position(1, 0), Position(0, 1)), grid.neighbors(Position(0, 0)))
        assertEquals(
            listOf(Position(3, 0), Position(3, 2), Position(2, 1)),
            grid.neighbors(Position(3, 1)),
        )
    }

    @Test
    fun `neighbors of a position outside are the adjacent cells inside`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(listOf(Position(0, 1)), grid.neighbors(Position(-1, 1)))
        assertEquals(listOf(Position(1, 2)), grid.neighbors(Position(1, 3)))
        assertEquals(emptyList(), grid.neighbors(Position(-1, -1)))
        assertEquals(emptyList(), grid.neighbors(Position(10, 10)))
    }

    @Test
    fun `neighbors never include the position itself`() {
        val grid = Grid(3, 3) { 0 }
        for (position in grid.positions) {
            assertFalse(position in grid.neighbors(position))
            assertFalse(position in grid.neighbors8(position))
        }
    }

    @Test
    fun `neighbors in a single cell grid and in an empty grid are empty`() {
        assertEquals(emptyList(), Grid(1, 1) { 0 }.neighbors(Position(0, 0)))
        assertEquals(emptyList(), Grid(0, 0) { 0 }.neighbors(Position(0, 0)))
        assertEquals(emptyList(), Grid(0, 2) { 0 }.neighbors(Position(0, 1)))
        assertEquals(emptyList(), Grid(1, 1) { 0 }.neighbors8(Position(0, 0)))
        assertEquals(emptyList(), Grid(0, 0) { 0 }.neighbors8(Position(0, 0)))
        assertEquals(emptyList(), Grid(0, 2) { 0 }.neighbors8(Position(-1, 1)))
    }

    @Test
    fun `neighbors8 of an inner cell are all eight in Direction8 entries order`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(
            listOf(
                Position(1, 0), Position(2, 0), Position(2, 1), Position(2, 2),
                Position(1, 2), Position(0, 2), Position(0, 1), Position(0, 0),
            ),
            grid.neighbors8(Position(1, 1)),
        )
    }

    @Test
    fun `neighbors8 of a corner leave out the positions outside`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(
            listOf(Position(1, 0), Position(1, 1), Position(0, 1)),
            grid.neighbors8(Position(0, 0)),
        )
        assertEquals(
            listOf(Position(2, 1), Position(1, 2), Position(1, 1)),
            grid.neighbors8(Position(2, 2)),
        )
    }

    @Test
    fun `neighbors8 of a position outside are the adjacent cells inside`() {
        val grid = Grid(3, 3) { 0 }
        assertEquals(
            listOf(Position(0, 0), Position(0, 1), Position(0, 2)),
            grid.neighbors8(Position(-1, 1)),
        )
        assertEquals(listOf(Position(0, 0)), grid.neighbors8(Position(-1, -1)))
        assertEquals(emptyList(), grid.neighbors8(Position(-2, 1)))
    }

    @Test
    fun `neighbors8 in a non square grid respect both sizes`() {
        val grid = numbers4x3()
        assertEquals(
            listOf(Position(3, 1), Position(2, 2), Position(2, 1)),
            grid.neighbors8(Position(3, 2)),
        )
    }

    @Test
    fun `ray goes from the next cell to the edge nearest first`() {
        val grid = numbers4x3()
        assertEquals(listOf(Position(2, 1), Position(3, 1)), grid.ray(Position(1, 1), Direction.RIGHT))
        assertEquals(listOf(Position(1, 0)), grid.ray(Position(1, 1), Direction.UP))
        assertEquals(listOf(Position(1, 2)), grid.ray(Position(1, 1), Direction.DOWN))
        assertEquals(listOf(Position(0, 1)), grid.ray(Position(1, 1), Direction.LEFT))
        assertEquals(
            listOf(Position(2, 2), Position(1, 2), Position(0, 2)),
            grid.ray(Position(3, 2), Direction.LEFT),
        )
    }

    @Test
    fun `ray excludes its start`() {
        val grid = numbers4x3()
        val start = Position(1, 1)
        for (direction in Direction.entries) assertFalse(start in grid.ray(start, direction))
        for (direction in Direction8.entries) assertFalse(start in grid.ray(start, direction))
    }

    @Test
    fun `ray ends with the edge cell`() {
        val grid = numbers4x3()
        assertEquals(Position(3, 1), grid.ray(Position(0, 1), Direction.RIGHT).last())
        assertEquals(Position(2, 2), grid.ray(Position(2, 0), Direction.DOWN).last())
        assertEquals(Position(0, 2), grid.ray(Position(3, 2), Direction.LEFT).last())
        assertEquals(Position(1, 0), grid.ray(Position(1, 2), Direction.UP).last())
        assertEquals(Position(2, 2), grid.ray(Position(0, 0), Direction8.DOWN_RIGHT).last())
    }

    @Test
    fun `ray from a cell on the edge towards the outside is empty`() {
        val grid = numbers4x3()
        assertEquals(emptyList(), grid.ray(Position(3, 1), Direction.RIGHT))
        assertEquals(emptyList(), grid.ray(Position(0, 0), Direction.UP))
        assertEquals(emptyList(), grid.ray(Position(0, 0), Direction.LEFT))
        assertEquals(emptyList(), grid.ray(Position(2, 2), Direction.DOWN))
        assertEquals(emptyList(), grid.ray(Position(3, 0), Direction8.UP_RIGHT))
        assertEquals(emptyList(), grid.ray(Position(1, 2), Direction8.DOWN_LEFT))
    }

    @Test
    fun `ray with a diagonal direction stops at the first edge it reaches`() {
        val grid = numbers4x3()
        assertEquals(listOf(Position(1, 1), Position(2, 2)), grid.ray(Position(0, 0), Direction8.DOWN_RIGHT))
        assertEquals(listOf(Position(2, 1), Position(1, 2)), grid.ray(Position(3, 0), Direction8.DOWN_LEFT))
        assertEquals(listOf(Position(2, 1), Position(1, 0)), grid.ray(Position(3, 2), Direction8.UP_LEFT))
        assertEquals(listOf(Position(1, 1), Position(2, 0)), grid.ray(Position(0, 2), Direction8.UP_RIGHT))
    }

    @Test
    fun `ray with an orthogonal Direction8 matches the Direction overload`() {
        val grid = numbers4x3()
        val start = Position(1, 1)
        assertEquals(grid.ray(start, Direction.UP), grid.ray(start, Direction8.UP))
        assertEquals(grid.ray(start, Direction.RIGHT), grid.ray(start, Direction8.RIGHT))
        assertEquals(grid.ray(start, Direction.DOWN), grid.ray(start, Direction8.DOWN))
        assertEquals(grid.ray(start, Direction.LEFT), grid.ray(start, Direction8.LEFT))
    }

    @Test
    fun `ray from just outside the grid crosses the whole row or column`() {
        val grid = numbers4x3()
        assertEquals(
            listOf(Position(0, 1), Position(1, 1), Position(2, 1), Position(3, 1)),
            grid.ray(Position(-1, 1), Direction.RIGHT),
        )
        assertEquals(
            listOf(Position(3, 1), Position(2, 1), Position(1, 1), Position(0, 1)),
            grid.ray(Position(4, 1), Direction.LEFT),
        )
        assertEquals(
            listOf(Position(2, 0), Position(2, 1), Position(2, 2)),
            grid.ray(Position(2, -1), Direction.DOWN),
        )
        assertEquals(
            listOf(Position(2, 2), Position(2, 1), Position(2, 0)),
            grid.ray(Position(2, 3), Direction.UP),
        )
        assertEquals(
            listOf(Position(0, 0), Position(1, 0), Position(2, 0), Position(3, 0)),
            grid.ray(Position(-1, 0), Direction8.RIGHT),
        )
    }

    @Test
    fun `ray from a negative diagonal start enters the grid at the corner`() {
        val grid = numbers4x3()
        assertEquals(
            listOf(Position(0, 0), Position(1, 1), Position(2, 2)),
            grid.ray(Position(-1, -1), Direction8.DOWN_RIGHT),
        )
    }

    @Test
    fun `ray is empty when the first step is outside`() {
        val grid = numbers4x3()
        assertEquals(emptyList(), grid.ray(Position(-2, 1), Direction.RIGHT))
        assertEquals(emptyList(), grid.ray(Position(1, -2), Direction.DOWN))
        assertEquals(emptyList(), grid.ray(Position(-1, 1), Direction.LEFT))
        assertEquals(emptyList(), grid.ray(Position(5, 1), Direction.LEFT))
        assertEquals(emptyList(), grid.ray(Position(-2, -2), Direction8.DOWN_RIGHT))
        assertEquals(emptyList(), grid.ray(Position(-1, 1), Direction8.UP_LEFT))
    }

    @Test
    fun `ray size and indexing agree with iteration`() {
        val grid = numbers4x3()
        val rays = listOf(
            grid.ray(Position(-1, 1), Direction.RIGHT),
            grid.ray(Position(2, 3), Direction.UP),
            grid.ray(Position(1, 1), Direction.LEFT),
            grid.ray(Position(3, 1), Direction.RIGHT),
            grid.ray(Position(-1, -1), Direction8.DOWN_RIGHT),
            grid.ray(Position(3, 0), Direction8.DOWN_LEFT),
        )
        for (ray in rays) {
            val iterated = mutableListOf<Position>()
            for (position in ray) iterated += position
            assertEquals(iterated.size, ray.size)
            assertEquals(iterated.isEmpty(), ray.isEmpty())
            for (index in iterated.indices) assertEquals(iterated[index], ray[index])
        }
    }

    @Test
    fun `ray throws IndexOutOfBoundsException for an index outside`() {
        val grid = numbers4x3()
        val ray = grid.ray(Position(-1, 1), Direction.RIGHT)
        assertEquals(4, ray.size)
        assertFailsWith<IndexOutOfBoundsException> { ray[ray.size] }
        assertFailsWith<IndexOutOfBoundsException> { ray[-1] }

        val diagonal = grid.ray(Position(0, 0), Direction8.DOWN_RIGHT)
        assertEquals(2, diagonal.size)
        assertFailsWith<IndexOutOfBoundsException> { diagonal[diagonal.size] }

        val empty = grid.ray(Position(3, 1), Direction.RIGHT)
        assertFailsWith<IndexOutOfBoundsException> { empty[0] }
    }

    @Test
    fun `ray from extreme Int coordinates does not overflow into the grid`() {
        val grid = numbers4x3()
        val extremes = listOf(
            Position(Int.MAX_VALUE, 1),
            Position(Int.MIN_VALUE, 1),
            Position(1, Int.MAX_VALUE),
            Position(1, Int.MIN_VALUE),
            Position(Int.MAX_VALUE, Int.MAX_VALUE),
            Position(Int.MIN_VALUE, Int.MIN_VALUE),
            Position(Int.MAX_VALUE, Int.MIN_VALUE),
            Position(Int.MIN_VALUE, Int.MAX_VALUE),
        )
        for (from in extremes) {
            for (direction in Direction.entries) {
                val ray = grid.ray(from, direction)
                assertEquals(emptyList(), ray, "from $from towards $direction")
                assertEquals(0, ray.size)
            }
            for (direction in Direction8.entries) {
                val ray = grid.ray(from, direction)
                assertEquals(emptyList(), ray, "from $from towards $direction")
                assertEquals(0, ray.size)
            }
        }
    }

    @Test
    fun `ray in an empty or zero width grid is empty`() {
        val empty = Grid(0, 0) { 0 }
        val zeroWidth = Grid(0, 2) { 0 }
        val zeroHeight = Grid(3, 0) { 0 }
        for (direction in Direction.entries) {
            assertEquals(emptyList(), empty.ray(Position(0, 0), direction))
            assertEquals(emptyList(), empty.ray(Position(-1, -1), direction))
        }
        for (direction in Direction8.entries) assertEquals(emptyList(), empty.ray(Position(0, 0), direction))
        assertEquals(emptyList(), zeroWidth.ray(Position(-1, 0), Direction.RIGHT))
        assertEquals(emptyList(), zeroWidth.ray(Position(0, -1), Direction.DOWN))
        assertEquals(emptyList(), zeroHeight.ray(Position(-1, 0), Direction.RIGHT))
        assertEquals(emptyList(), zeroHeight.ray(Position(1, -1), Direction8.DOWN))
    }

    @Test
    fun `ray equals the generateSequence form for every direction and start`() {
        val grid = numbers4x3()
        val starts = listOf(
            Position(1, 1), Position(2, 1),
            Position(0, 0), Position(3, 0), Position(0, 2), Position(3, 2), Position(1, 0), Position(3, 1),
            Position(-1, 1), Position(4, 1), Position(1, -1), Position(1, 3), Position(-1, -1), Position(4, 3),
            Position(-2, 1), Position(10, -7), Position(-1, 3),
            Position(Int.MAX_VALUE, 0), Position(Int.MIN_VALUE, 0),
            Position(0, Int.MAX_VALUE), Position(0, Int.MIN_VALUE),
        )
        for (from in starts) {
            for (d in Direction.entries) {
                val expected = generateSequence(from + d) { it + d }.takeWhile { it in grid }.toList()
                assertEquals(expected, grid.ray(from, d), "from $from towards $d")
            }
            for (d in Direction8.entries) {
                val expected = generateSequence(from + d) { it + d }.takeWhile { it in grid }.toList()
                assertEquals(expected, grid.ray(from, d), "from $from towards $d")
            }
        }
    }

    @Test
    fun `wrap onto a grid uses floor modulo of width and height`() {
        val grid = numbers4x3()
        assertEquals(Position(1, 2), Position(5, -1).wrap(grid))
        assertEquals(Position(3, 1), Position(-5, 7).wrap(grid))
        assertEquals(Position(0, 0), Position(-4, -6).wrap(grid))
        assertEquals(Position(2, 1), Position(2, 1).wrap(grid))
        assertEquals(Position(0, 0), Position(4, 3).wrap(grid))
    }

    @Test
    fun `wrap onto a grid reads the repeated pattern`() {
        val grid = numbers4x3()
        assertEquals(21, grid[Position(9, -1).wrap(grid)])
        assertEquals(3, grid[Position(-1, -3).wrap(grid)])
    }

    @Test
    fun `wrap onto a grid equals wrap with its width and height`() {
        val grid = numbers4x3()
        for (x in -9..9) {
            for (y in -7..7) {
                val position = Position(x, y)
                val wrapped = position.wrap(grid)
                assertEquals(position.wrap(4, 3), wrapped)
                assertTrue(wrapped in grid)
            }
        }
    }

    @Test
    fun `wrap onto an empty grid throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> { Position(0, 0).wrap(Grid(0, 0) { 0 }) }
        assertFailsWith<IllegalArgumentException> { Position(0, 0).wrap(Grid(0, 2) { 0 }) }
        assertFailsWith<IllegalArgumentException> { Position(0, 0).wrap(Grid(3, 0) { 0 }) }
    }

    @Test
    fun `count returns the number of matching cells`() {
        val grid = listOf("#.#", "..#").toGrid()
        assertEquals(3, grid.count { it == '#' })
        assertEquals(3, grid.count { it == '.' })
        assertEquals(0, grid.count { it == 'x' })
        assertEquals(6, grid.count { it == '#' || it == '.' })
    }

    @Test
    fun `count in an empty grid is zero`() {
        assertEquals(0, Grid(0, 0) { '#' }.count { it == '#' })
        assertEquals(0, Grid(3, 0) { '#' }.count { it == '#' })
    }

    @Test
    fun `forEachIndexed visits every cell in reading order with its value`() {
        val grid = Grid(2, 3) { it.x + 10 * it.y }
        val seen = mutableListOf<Pair<Position, Int>>()
        grid.forEachIndexed { position, value -> seen += position to value }
        assertEquals(
            listOf(
                Position(0, 0) to 0, Position(1, 0) to 1,
                Position(0, 1) to 10, Position(1, 1) to 11,
                Position(0, 2) to 20, Position(1, 2) to 21,
            ),
            seen,
        )
    }

    @Test
    fun `forEachIndexed passes the value stored at the position`() {
        val grid = abcDef()
        var calls = 0
        grid.forEachIndexed { position, value ->
            assertEquals(grid[position], value)
            calls += 1
        }
        assertEquals(6, calls)
    }

    @Test
    fun `forEachIndexed never calls the action for an empty grid`() {
        var calls = 0
        Grid(0, 0) { 0 }.forEachIndexed { _, _ -> calls += 1 }
        Grid(0, 2) { 0 }.forEachIndexed { _, _ -> calls += 1 }
        Grid(3, 0) { 0 }.forEachIndexed { _, _ -> calls += 1 }
        assertEquals(0, calls)
    }

    @Test
    fun `getOrElse returns the value inside without calling the lambda`() {
        val grid = numbers4x3()
        var called = false
        val value = grid.getOrElse(Position(1, 1)) {
            called = true
            -1
        }
        assertEquals(11, value)
        assertFalse(called)
    }

    @Test
    fun `getOrElse calls the lambda with the position outside`() {
        val grid = numbers4x3()
        assertEquals(698, grid.getOrElse(Position(7, -2)) { it.x * 100 + it.y })
        assertEquals(-100, grid.getOrElse(Position(-1, 0)) { it.x * 100 + it.y })
        assertEquals(400, grid.getOrElse(Position(4, 0)) { it.x * 100 + it.y })
        assertEquals(3, grid.getOrElse(Position(0, 3)) { it.x * 100 + it.y })
    }

    @Test
    fun `getOrElse keeps a null cell inside null`() {
        val grid = Grid<Char?>(2, 1) { if (it.x == 0) null else 'a' }
        assertNull(grid.getOrElse(Position(0, 0)) { 'z' })
        assertEquals('a', grid.getOrElse(Position(1, 0)) { 'z' })
        assertEquals('z', grid.getOrElse(Position(2, 0)) { 'z' })
    }

    @Test
    fun `getOrElse on an empty grid always calls the lambda`() {
        val grid = Grid(0, 0) { 'a' }
        assertEquals('z', grid.getOrElse(Position(0, 0)) { 'z' })
    }

    @Test
    fun `map applies the transform to every cell and keeps the size`() {
        val grid = numbers4x3()
        val mapped = grid.map { it * 2 }
        assertEquals(4, mapped.width)
        assertEquals(3, mapped.height)
        assertEquals(Grid(4, 3) { 2 * (it.x + 10 * it.y) }, mapped)
        assertEquals(22, mapped[Position(1, 1)])
        assertEquals(46, mapped[Position(3, 2)])
    }

    @Test
    fun `map can change the value type`() {
        val mapped = abcDef().map { it.uppercaseChar().toString() + "!" }
        assertEquals(listOf("A!", "B!", "C!", "D!", "E!", "F!"), mapped.values)
        assertEquals("A!B!C!\nD!E!F!", mapped.toString())
    }

    @Test
    fun `map leaves the receiver unchanged`() {
        val source = listOf("abc", "def").toMutableGrid()
        val mapped = source.map { it.uppercaseChar() }
        assertEquals(listOf("abc", "def").toGrid(), source)
        assertEquals(listOf("ABC", "DEF").toGrid(), mapped)
    }

    @Test
    fun `map result is a read only grid independent of the source`() {
        val source = listOf("abc", "def").toMutableGrid()
        val mapped: Grid<Char> = source.map { it }
        assertFalse(mapped is MutableGrid<*>)
        source[Position(0, 0)] = 'z'
        assertEquals('a', mapped[Position(0, 0)])
    }

    @Test
    fun `map of an empty grid keeps its size`() {
        val zeroHeight = Grid(3, 0) { 0 }.map { it + 1 }
        assertEquals(3, zeroHeight.width)
        assertEquals(0, zeroHeight.height)
        val zeroWidth = Grid(0, 2) { 0 }.map { it + 1 }
        assertEquals(0, zeroWidth.width)
        assertEquals(2, zeroWidth.height)
        assertEquals(Grid(0, 0) { "" }, Grid(0, 0) { 0 }.map { it.toString() })
    }

    @Test
    fun `map supports a non local return from the transform`() {
        assertNull(doubledUnlessNegative(Grid(2, 2) { if (it == Position(1, 1)) -1 else 1 }))
        assertEquals(Grid(2, 2) { 2 }, doubledUnlessNegative(Grid(2, 2) { 1 }))
    }

    @Test
    fun `mapIndexed passes each position with its value`() {
        val mapped = abcDef().mapIndexed { position, value -> "$value${position.x}${position.y}" }
        assertEquals(3, mapped.width)
        assertEquals(2, mapped.height)
        assertEquals(listOf("a00", "b10", "c20", "d01", "e11", "f21"), mapped.values)
        assertEquals("f21", mapped[Position(2, 1)])
    }

    @Test
    fun `mapIndexed leaves the receiver unchanged`() {
        val source = listOf("abc", "def").toMutableGrid()
        val mapped = source.mapIndexed { position, value -> if (position.y == 0) value.uppercaseChar() else value }
        assertEquals(listOf("abc", "def").toGrid(), source)
        assertEquals(listOf("ABC", "def").toGrid(), mapped)
    }

    @Test
    fun `mapIndexed result is a read only grid independent of the source`() {
        val source = listOf("abc", "def").toMutableGrid()
        val mapped: Grid<Char> = source.mapIndexed { _, value -> value }
        assertFalse(mapped is MutableGrid<*>)
        source[Position(2, 1)] = 'z'
        assertEquals('f', mapped[Position(2, 1)])
    }

    @Test
    fun `mapIndexed of an empty grid keeps its size`() {
        val mapped = Grid(3, 0) { 'a' }.mapIndexed { position, _ -> position.x }
        assertEquals(3, mapped.width)
        assertEquals(0, mapped.height)
        assertEquals(Grid(0, 0) { 0 }, Grid(0, 0) { 'a' }.mapIndexed { position, _ -> position.x })
    }

    @Test
    fun `mapIndexed supports a non local return from the transform`() {
        assertNull(coordinateSumsUnlessWall(listOf("..", ".#").toGrid()))
        assertEquals(Grid(3, 2) { it.x + it.y }, coordinateSumsUnlessWall(listOf("...", "...").toGrid()))
    }

    @Test
    fun `transposed moves the value at x y to y x`() {
        val source = abcDef()
        val transposed = source.transposed()
        assertEquals(2, transposed.width)
        assertEquals(3, transposed.height)
        for (position in source.positions) {
            assertEquals(source[position], transposed[Position(position.y, position.x)])
        }
        assertEquals(listOf("ad", "be", "cf").toGrid(), transposed)
        assertEquals("ad\nbe\ncf", transposed.toString())
    }

    @Test
    fun `transposed turns rows into columns`() {
        val source = abcDef()
        val transposed = source.transposed()
        assertEquals(source.rows, transposed.columns)
        assertEquals(source.columns, transposed.rows)
    }

    @Test
    fun `transposed of a single row is a single column`() {
        assertEquals(listOf("a", "b", "c").toGrid(), listOf("abc").toGrid().transposed())
    }

    @Test
    fun `rotatedClockwise turns the top row into the right column`() {
        val source = abcDef()
        val rotated = source.rotatedClockwise()
        assertEquals(2, rotated.width)
        assertEquals(3, rotated.height)
        assertEquals(source.rows[0], rotated.columns[rotated.width - 1])
        assertEquals(source.rows[1], rotated.columns[0])
        assertEquals(listOf("da", "eb", "fc").toGrid(), rotated)
    }

    @Test
    fun `rotatedClockwise of a single row is a single column read top to bottom`() {
        assertEquals(listOf("a", "b", "c").toGrid(), listOf("abc").toGrid().rotatedClockwise())
    }

    @Test
    fun `rotatedClockwise twice turns the grid upside down`() {
        assertEquals(listOf("fed", "cba").toGrid(), abcDef().rotatedClockwise().rotatedClockwise())
    }

    @Test
    fun `rotatedCounterclockwise turns the right column into the top row`() {
        val source = abcDef()
        val rotated = source.rotatedCounterclockwise()
        assertEquals(2, rotated.width)
        assertEquals(3, rotated.height)
        assertEquals(source.columns[source.width - 1], rotated.rows[0])
        assertEquals(source.columns[0], rotated.rows[rotated.height - 1])
        assertEquals(listOf("cf", "be", "ad").toGrid(), rotated)
    }

    @Test
    fun `rotatedCounterclockwise of a single row is a single column read bottom to top`() {
        assertEquals(listOf("c", "b", "a").toGrid(), listOf("abc").toGrid().rotatedCounterclockwise())
    }

    @Test
    fun `four clockwise turns give the original grid`() {
        val source = abcDef()
        assertEquals(source, source.rotatedClockwise().rotatedClockwise().rotatedClockwise().rotatedClockwise())
        val numbers = numbers4x3()
        assertEquals(numbers, numbers.rotatedClockwise().rotatedClockwise().rotatedClockwise().rotatedClockwise())
    }

    @Test
    fun `four counterclockwise turns give the original grid`() {
        val numbers = numbers4x3()
        assertEquals(
            numbers,
            numbers.rotatedCounterclockwise().rotatedCounterclockwise().rotatedCounterclockwise()
                .rotatedCounterclockwise(),
        )
    }

    @Test
    fun `a clockwise and a counterclockwise turn cancel out in either order`() {
        val source = abcDef()
        assertEquals(source, source.rotatedClockwise().rotatedCounterclockwise())
        assertEquals(source, source.rotatedCounterclockwise().rotatedClockwise())
        val numbers = numbers4x3()
        assertEquals(numbers, numbers.rotatedClockwise().rotatedCounterclockwise())
        assertEquals(numbers, numbers.rotatedCounterclockwise().rotatedClockwise())
    }

    @Test
    fun `two transpositions give the original grid`() {
        val source = abcDef()
        assertEquals(source, source.transposed().transposed())
        val numbers = numbers4x3()
        assertEquals(numbers, numbers.transposed().transposed())
    }

    @Test
    fun `transposed then rotatedClockwise mirrors left to right`() {
        assertEquals(listOf("cba", "fed").toGrid(), abcDef().transposed().rotatedClockwise())
        val numbers = numbers4x3()
        assertEquals(Grid(4, 3) { numbers[Position(3 - it.x, it.y)] }, numbers.transposed().rotatedClockwise())
    }

    @Test
    fun `transposed then rotatedCounterclockwise mirrors top to bottom`() {
        assertEquals(listOf("def", "abc").toGrid(), abcDef().transposed().rotatedCounterclockwise())
        val numbers = numbers4x3()
        assertEquals(Grid(4, 3) { numbers[Position(it.x, 2 - it.y)] }, numbers.transposed().rotatedCounterclockwise())
    }

    @Test
    fun `transforms leave the receiver unchanged`() {
        val source = listOf("abc", "def").toMutableGrid()
        val transposed = source.transposed()
        val clockwise = source.rotatedClockwise()
        val counterclockwise = source.rotatedCounterclockwise()
        assertEquals(listOf("abc", "def").toGrid(), source)
        assertEquals(listOf("ad", "be", "cf").toGrid(), transposed)
        assertEquals(listOf("da", "eb", "fc").toGrid(), clockwise)
        assertEquals(listOf("cf", "be", "ad").toGrid(), counterclockwise)
    }

    @Test
    fun `transforms return read only grids that later writes to the source do not change`() {
        val source = listOf("abc", "def").toMutableGrid()
        val results: List<Grid<Char>> =
            listOf(source.transposed(), source.rotatedClockwise(), source.rotatedCounterclockwise())
        for (result in results) assertFalse(result is MutableGrid<*>)
        source[Position(0, 0)] = 'z'
        assertEquals(listOf("ad", "be", "cf").toGrid(), results[0])
        assertEquals(listOf("da", "eb", "fc").toGrid(), results[1])
        assertEquals(listOf("cf", "be", "ad").toGrid(), results[2])
    }

    @Test
    fun `transforms of a grid with a zero size swap the sizes`() {
        val zeroHeight = Grid(3, 0) { 0 }
        val zeroHeightResults =
            listOf(zeroHeight.transposed(), zeroHeight.rotatedClockwise(), zeroHeight.rotatedCounterclockwise())
        for (result in zeroHeightResults) {
            assertEquals(0, result.width)
            assertEquals(3, result.height)
            assertEquals(Grid(0, 3) { 0 }, result)
        }
        val zeroWidth = Grid(0, 3) { 0 }
        val zeroWidthResults =
            listOf(zeroWidth.transposed(), zeroWidth.rotatedClockwise(), zeroWidth.rotatedCounterclockwise())
        for (result in zeroWidthResults) {
            assertEquals(3, result.width)
            assertEquals(0, result.height)
            assertEquals(Grid(3, 0) { 0 }, result)
        }
        val empty = Grid(0, 0) { 0 }
        for (result in listOf(empty.transposed(), empty.rotatedClockwise(), empty.rotatedCounterclockwise())) {
            assertEquals(empty, result)
            assertEquals("", result.toString())
        }
    }

    // Gathered outside the assertions: power-assert prints every value an assertion mentions, and a
    // 0 x Int.MAX_VALUE grid prints as two billion line breaks.
    private fun sizesOfTransformed(grid: Grid<Int>): List<List<Int>> = listOf(
        grid.transposed(),
        grid.rotatedClockwise(),
        grid.rotatedCounterclockwise(),
        grid.map { it + 1 },
        grid.mapIndexed { position, value -> position.x + value },
    ).map { listOf(it.width, it.height, it.values.size) }

    @Test
    fun `transforms of a grid without cells take no step per row or per column`() {
        // A walk along the Int.MAX_VALUE side takes 8 to 20 seconds on JS, Wasm and Native; the JVM gets through
        // it in milliseconds, so there the time limit proves nothing.
        val (sizes, elapsed) = measureTimedValue {
            sizesOfTransformed(Grid(0, Int.MAX_VALUE) { 0 }) to sizesOfTransformed(Grid(Int.MAX_VALUE, 0) { 0 })
        }
        val (tall, wide) = sizes

        val turnedTall = listOf(Int.MAX_VALUE, 0, 0)
        val turnedWide = listOf(0, Int.MAX_VALUE, 0)
        assertEquals(listOf(turnedTall, turnedTall, turnedTall, turnedWide, turnedWide), tall)
        assertEquals(listOf(turnedWide, turnedWide, turnedWide, turnedTall, turnedTall), wide)
        assertTrue(elapsed < 1.seconds, "transforms of grids without cells took $elapsed")
    }

    @Test
    fun `neighbors of a position with extreme coordinates are empty`() {
        val grid = Grid(3, 3) { 0 }
        val corners = listOf(
            Position(Int.MAX_VALUE, Int.MAX_VALUE),
            Position(Int.MIN_VALUE, Int.MIN_VALUE),
            Position(Int.MAX_VALUE, 0),
            Position(0, Int.MIN_VALUE),
        )

        // A step from such a position overflows, and the result must not land inside the grid
        for (corner in corners) {
            assertEquals(emptyList(), grid.neighbors(corner))
            assertEquals(emptyList(), grid.neighbors8(corner))
        }
    }

    private fun firstNegative(grid: Grid<Int>): Position? {
        grid.forEachIndexed { position, value -> if (value < 0) return position }
        return null
    }

    private fun countUnlessNegative(grid: Grid<Int>): Int? = grid.count { if (it < 0) return null else it > 0 }

    private fun valueOrNullOutside(grid: Grid<Int>, position: Position): Int? = grid.getOrElse(position) { return null }

    @Test
    fun `forEachIndexed count and getOrElse support a non local return from the lambda`() {
        val grid = Grid(2, 2) { if (it == Position(1, 0)) -1 else 1 }
        val positive = Grid(2, 2) { 1 }

        assertEquals(Position(1, 0), firstNegative(grid))
        assertNull(firstNegative(positive))
        assertNull(countUnlessNegative(grid))
        assertEquals(4, countUnlessNegative(positive))
        assertEquals(-1, valueOrNullOutside(grid, Position(1, 0)))
        assertNull(valueOrNullOutside(grid, Position(2, 0)))
    }

    private fun gridsWithPositionsAround(): List<Pair<Grid<Int>, Position>> {
        val result = mutableListOf<Pair<Grid<Int>, Position>>()
        for ((width, height) in listOf(1 to 1, 1 to 3, 3 to 1, 2 to 2, 3 to 4, 4 to 3, 0 to 2, 2 to 0)) {
            val grid = Grid(width, height) { 0 }
            for (y in -2..height + 1) {
                for (x in -2..width + 1) result += grid to Position(x, y)
            }
        }
        return result
    }

    @Test
    fun `neighbors are the steps in every direction that stay inside for any position`() {
        for ((grid, position) in gridsWithPositionsAround()) {
            val expected = Direction.entries.map { position + it }.filter { it in grid }
            val expected8 = Direction8.entries.map { position + it }.filter { it in grid }
            val message = "$position in a ${grid.width} x ${grid.height} grid"

            assertEquals(expected, grid.neighbors(position), message)
            assertEquals(expected8, grid.neighbors8(position), message)
        }
    }

    @Test
    fun `neighbors size and indexing agree with iteration`() {
        for ((grid, position) in gridsWithPositionsAround()) {
            for (neighbors in listOf(grid.neighbors(position), grid.neighbors8(position))) {
                val iterated = mutableListOf<Position>()
                for (neighbor in neighbors) iterated += neighbor

                assertEquals(iterated.size, neighbors.size)
                assertEquals(iterated.isEmpty(), neighbors.isEmpty())
                for (index in iterated.indices) assertEquals(iterated[index], neighbors[index])
            }
        }
    }

    @Test
    fun `neighbors compare and hash as lists and answer contains and indexOf`() {
        val grid = Grid(3, 3) { 0 }
        val corner = grid.neighbors(Position(0, 0))
        val expected = listOf(Position(1, 0), Position(0, 1))

        assertTrue(corner == expected)
        assertTrue(expected == corner)
        assertEquals(expected.hashCode(), corner.hashCode())
        assertEquals(expected.toString(), corner.toString())
        assertTrue(Position(0, 1) in corner)
        assertFalse(Position(0, 0) in corner)
        assertFalse(Position(-1, 0) in corner)
        assertEquals(1, corner.indexOf(Position(0, 1)))
        assertEquals(-1, corner.indexOf(Position(1, 1)))
        assertEquals(listOf(Position(1, 0), Position(1, 1), Position(0, 1)), grid.neighbors8(Position(0, 0)))
    }

    @Test
    fun `neighbors throw IndexOutOfBoundsException for an index outside`() {
        val grid = Grid(3, 3) { 0 }
        val corner = grid.neighbors(Position(0, 0))
        assertEquals(2, corner.size)
        assertFailsWith<IndexOutOfBoundsException> { corner[2] }
        assertFailsWith<IndexOutOfBoundsException> { corner[-1] }

        val inner = grid.neighbors8(Position(1, 1))
        assertEquals(8, inner.size)
        assertFailsWith<IndexOutOfBoundsException> { inner[8] }

        val none = Grid(1, 1) { 0 }.neighbors8(Position(0, 0))
        assertFailsWith<IndexOutOfBoundsException> { none[0] }
    }

    @Test
    fun `neighbors and ray iterators throw NoSuchElementException after the last position`() {
        val grid = Grid(3, 3) { 0 }
        val lists = listOf(
            grid.neighbors(Position(0, 0)),
            grid.neighbors(Position(1, 1)),
            grid.neighbors8(Position(2, 2)),
            grid.neighbors8(Position(5, 5)),
            grid.ray(Position(0, 0), Direction.RIGHT),
            grid.ray(Position(2, 0), Direction.RIGHT),
            grid.ray(Position(-1, -1), Direction8.DOWN_RIGHT),
        )
        for (list in lists) {
            val iterator = list.iterator()
            var count = 0
            while (iterator.hasNext()) {
                assertEquals(list[count], iterator.next())
                count++
            }
            assertEquals(list.size, count)
            assertFalse(iterator.hasNext())
            assertFailsWith<NoSuchElementException> { iterator.next() }
        }
    }

    @Test
    fun `neighbors of a MutableGrid stay the same when cells are written`() {
        val grid = MutableGrid(2, 2) { '.' }
        val neighbors = grid.neighbors8(Position(0, 0))

        grid[Position(1, 1)] = '#'

        assertEquals(listOf(Position(1, 0), Position(1, 1), Position(0, 1)), neighbors)
    }
}
