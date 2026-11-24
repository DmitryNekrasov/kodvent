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

    private fun <E> iterated(list: List<E>): List<E> {
        val result = mutableListOf<E>()
        for (element in list) result += element
        return result
    }

    private fun <E> indexed(list: List<E>): List<E> = List(list.size) { list[it] }

    private fun gridsWithPositionsAround(): List<Pair<Grid<Int>, Position>> {
        val result = mutableListOf<Pair<Grid<Int>, Position>>()
        for ((width, height) in listOf(1 to 1, 1 to 3, 3 to 1, 2 to 2, 3 to 4, 4 to 3, 0 to 0, 0 to 2, 2 to 0)) {
            val grid = Grid(width, height) { 0 }
            for (y in -2..height + 1) {
                for (x in -2..width + 1) result += grid to Position(x, y)
            }
            // A step from such a position overflows, and the result must not land inside the grid
            for (far in listOf(Int.MIN_VALUE, Int.MAX_VALUE)) {
                result += grid to Position(far, 0)
                result += grid to Position(0, far)
                result += grid to Position(far, far)
            }
        }
        return result
    }

    @Test
    fun `neighbors are the steps in every direction that stay inside for any position`() {
        for ((grid, position) in gridsWithPositionsAround()) {
            val expected = Direction.entries.map { position + it }.filter { it in grid }
            val expected8 = Direction8.entries.map { position + it }.filter { it in grid }

            assertEquals(expected, grid.neighbors(position))
            assertEquals(expected8, grid.neighbors8(position))
        }
    }

    @Test
    fun `neighbors size and indexing agree with iteration`() {
        for ((grid, position) in gridsWithPositionsAround()) {
            for (neighbors in listOf(grid.neighbors(position), grid.neighbors8(position))) {
                val iterated = iterated(neighbors)

                assertEquals(iterated.size, neighbors.size)
                assertEquals(iterated.isEmpty(), neighbors.isEmpty())
                for (index in iterated.indices) assertEquals(iterated[index], neighbors[index])
            }
        }
    }

    @Test
    fun `ray equals the generateSequence form for every direction and start`() {
        for ((grid, from) in gridsWithPositionsAround()) {
            for (d in Direction.entries) {
                val expected = generateSequence(from + d) { it + d }.takeWhile { it in grid }.toList()
                assertEquals(expected, indexed(grid.ray(from, d)))
                assertEquals(expected, iterated(grid.ray(from, d)))
            }
            for (d in Direction8.entries) {
                val expected = generateSequence(from + d) { it + d }.takeWhile { it in grid }.toList()
                assertEquals(expected, indexed(grid.ray(from, d)))
                assertEquals(expected, iterated(grid.ray(from, d)))
            }
        }
    }

    @Test
    fun `neighbors and ray throw IndexOutOfBoundsException for an index outside`() {
        val grid = Grid(3, 3) { 0 }
        val lists = listOf(
            grid.neighbors(Position(0, 0)),
            grid.neighbors8(Position(1, 1)),
            Grid(1, 1) { 0 }.neighbors8(Position(0, 0)),
            grid.ray(Position(-1, 1), Direction.RIGHT),
            grid.ray(Position(0, 0), Direction8.DOWN_RIGHT),
            grid.ray(Position(2, 1), Direction.RIGHT),
        )

        assertEquals(listOf(2, 8, 0, 3, 2, 0), lists.map { it.size })
        for (list in lists) {
            assertFailsWith<IndexOutOfBoundsException> { list[list.size] }
            assertFailsWith<IndexOutOfBoundsException> { list[-1] }
        }
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
    fun `mapIndexed passes each position with its value`() {
        val mapped = abcDef().mapIndexed { position, value -> "$value${position.x}${position.y}" }
        assertEquals(3, mapped.width)
        assertEquals(2, mapped.height)
        assertEquals(listOf("a00", "b10", "c20", "d01", "e11", "f21"), mapped.values)
        assertEquals("f21", mapped[Position(2, 1)])
    }

    private fun firstNegative(grid: Grid<Int>): Position? {
        grid.forEachIndexed { position, value -> if (value < 0) return position }
        return null
    }

    private fun countUnlessNegative(grid: Grid<Int>): Int? = grid.count { if (it < 0) return null else it > 0 }

    private fun valueOrNullOutside(grid: Grid<Int>, position: Position): Int? = grid.getOrElse(position) { return null }

    private fun doubledUnlessNegative(grid: Grid<Int>): Grid<Int>? = grid.map { if (it < 0) return null else it * 2 }

    private fun coordinateSumsUnlessNegative(grid: Grid<Int>): Grid<Int>? =
        grid.mapIndexed { position, value -> if (value < 0) return null else position.x + position.y }

    @Test
    fun `inline functions support a non local return from the lambda`() {
        val grid = Grid(2, 2) { if (it == Position(1, 0)) -1 else 1 }
        val positive = Grid(2, 2) { 1 }

        assertEquals(Position(1, 0), firstNegative(grid))
        assertNull(firstNegative(positive))
        assertNull(countUnlessNegative(grid))
        assertEquals(4, countUnlessNegative(positive))
        assertEquals(-1, valueOrNullOutside(grid, Position(1, 0)))
        assertNull(valueOrNullOutside(grid, Position(2, 0)))
        assertNull(doubledUnlessNegative(grid))
        assertEquals(Grid(2, 2) { 2 }, doubledUnlessNegative(positive))
        assertNull(coordinateSumsUnlessNegative(grid))
        assertEquals(Grid(2, 2) { it.x + it.y }, coordinateSumsUnlessNegative(positive))
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
    fun `a single row turns into a single column`() {
        val row = listOf("abc").toGrid()

        assertEquals(listOf("a", "b", "c").toGrid(), row.transposed())
        assertEquals(listOf("a", "b", "c").toGrid(), row.rotatedClockwise())
        assertEquals(listOf("c", "b", "a").toGrid(), row.rotatedCounterclockwise())
    }

    @Test
    fun `turns and transpositions that undo each other give the original grid`() {
        for (grid in listOf(abcDef(), numbers4x3())) {
            assertEquals(grid, grid.rotatedClockwise().rotatedClockwise().rotatedClockwise().rotatedClockwise())
            assertEquals(
                grid,
                grid.rotatedCounterclockwise().rotatedCounterclockwise().rotatedCounterclockwise()
                    .rotatedCounterclockwise(),
            )
            assertEquals(grid, grid.rotatedClockwise().rotatedCounterclockwise())
            assertEquals(grid, grid.rotatedCounterclockwise().rotatedClockwise())
            assertEquals(grid, grid.transposed().transposed())
        }
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
    fun `mapped and turned grids do not change when the source is written`() {
        val source = listOf("abc", "def").toMutableGrid()
        val results = listOf(
            source.map { it },
            source.mapIndexed { _, value -> value },
            source.transposed(),
            source.rotatedClockwise(),
            source.rotatedCounterclockwise(),
        )

        source[Position(0, 0)] = 'z'

        assertEquals(
            listOf("abc\ndef", "abc\ndef", "ad\nbe\ncf", "da\neb\nfc", "cf\nbe\nad"),
            results.map { it.toString() },
        )
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
        assertTrue(elapsed < 1.seconds)
    }
}
