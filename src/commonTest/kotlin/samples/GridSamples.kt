/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.grid.Direction
import kodvent.grid.Direction8
import kodvent.grid.Grid
import kodvent.grid.MutableGrid
import kodvent.grid.Position
import kodvent.grid.count
import kodvent.grid.forEachIndexed
import kodvent.grid.getOrElse
import kodvent.grid.map
import kodvent.grid.mapIndexed
import kodvent.grid.neighbors
import kodvent.grid.neighbors8
import kodvent.grid.ray
import kodvent.grid.rotatedClockwise
import kodvent.grid.rotatedCounterclockwise
import kodvent.grid.toDirection
import kodvent.grid.toGrid
import kodvent.grid.toMutableGrid
import kodvent.grid.transposed
import kodvent.grid.wrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GridSamples {

    @Test
    fun parseAndRead() {
        val input = listOf(
            "#...",
            "..#.",
            ".@..",
        )
        val grid = input.toGrid()

        // width is the line length, height is the number of lines
        assertEquals(4, grid.width)
        assertEquals(3, grid.height)

        // x is the column, y is the line, counted from the top
        assertEquals('#', grid[Position(0, 0)])
        assertEquals('#', grid[Position(2, 1)])
        assertEquals('@', grid[Position(1, 2)])

        // input[y][x] is the cell at Position(x, y)
        assertEquals(input[2][1], grid[Position(1, 2)])
    }

    @Test
    fun indexByCoordinates() {
        val input = listOf(
            "#...",
            "..#.",
            ".@..",
        )
        val grid = input.toMutableGrid()

        // grid[x, y] is grid[Position(x, y)]: the column first, then the line
        assertEquals('#', grid[2, 1])
        assertEquals(grid[Position(2, 1)], grid[2, 1])
        assertEquals(input[1][2], grid[2, 1])

        grid[3, 0] = '#'
        var rocks = 0
        for (y in 0..<grid.height) {
            for (x in 0..<grid.width) {
                if (grid[x, y] == '#') rocks++
            }
        }
        assertEquals(3, rocks)

        // A table of numbers: the ways to reach each cell from the top left corner going right or down
        val ways = MutableGrid(4, 3) { 1L }
        for (y in 1..<ways.height) {
            for (x in 1..<ways.width) ways[x, y] = ways[x - 1, y] + ways[x, y - 1]
        }
        assertEquals(10L, ways[3, 2])
    }

    @Test
    fun parseWholeText() {
        // The middle line ends with two spaces, and the text ends with a line break
        val text = "#..\n.  \n..#\n"

        // lines() leaves an empty line after the final line break: drop it
        val grid = text.lines().dropLastWhile { it.isEmpty() }.toGrid()
        assertEquals(3, grid.width)
        assertEquals(3, grid.height)

        // Nothing is trimmed: a space is a cell like any other
        assertEquals(' ', grid[Position(2, 1)])

        val crlfText = "#..\r\n.  \r\n..#\r\n"
        assertEquals(grid, crlfText.lines().dropLastWhile { it.isEmpty() }.toGrid())
    }

    @Test
    fun parseDigits() {
        val input = listOf(
            "123",
            "908",
        )

        val heights = input.toGrid { it.digitToInt() }
        assertEquals(0, heights[Position(1, 1)])
        assertEquals(23, heights.values.sum()) // 1 + 2 + 3 + 9 + 0 + 8

        val levels = input.toMutableGrid { it.digitToInt() }
        levels[Position(2, 0)] += 1
        assertEquals(4, levels[Position(2, 0)])

        // digitToInt() throws on a character that is not a digit; with digitToIntOrNull() such a cell is null
        val partial = listOf("1.3", "908").toGrid { it.digitToIntOrNull() }
        assertNull(partial[Position(1, 0)])
        assertEquals(3, partial[Position(2, 0)])
    }

    @Test
    fun createBySize() {
        // init is called with every position in reading order
        val sums = Grid(3, 2) { it.x + it.y }
        assertEquals(listOf(0, 1, 2, 1, 2, 3), sums.values)

        val grid = MutableGrid(width = 4, height = 3) { '.' }
        for (line in listOf("1,0", "3,2", "0,2")) {
            val (x, y) = line.split(",").map { it.toInt() }
            grid[Position(x, y)] = '#'
        }

        assertEquals(".#..\n....\n#..#", grid.toString())
    }

    @Test
    fun mutableGrid() {
        val grid = listOf(
            "..",
            "..",
        ).toMutableGrid()

        grid[Position(1, 0)] = '#'
        assertEquals(".#\n..", grid.toString())

        val visits = MutableGrid(3, 1) { 0 }
        val p = Position(1, 0)
        visits[p]++
        visits[p] += 1
        assertEquals(2, visits[p])
        assertEquals(listOf(0, 2, 0), visits.values)
    }

    @Test
    fun positionsInReadingOrder() {
        val grid = listOf(
            "ab",
            "cd",
        ).toGrid()

        // Row by row from the top, each row from left to right
        val expected = listOf(Position(0, 0), Position(1, 0), Position(0, 1), Position(1, 1))
        assertEquals(expected, grid.positions)

        assertEquals("abcd", grid.positions.map { grid[it] }.joinToString(""))
        assertEquals(2, grid.positions.indexOf(Position(0, 1)))
    }

    @Test
    fun valuesView() {
        val grid = listOf(
            "#..",
            "..#",
        ).toMutableGrid()

        // Questions about values go through values
        val values = grid.values
        assertTrue('#' in values)
        assertFalse('@' in values)

        // values is a live view: it shows later writes
        grid[Position(1, 0)] = '@'
        assertTrue('@' in values)

        val heights = listOf("123", "908").toGrid { it.digitToInt() }
        assertEquals(9, heights.values.max())
        assertTrue(heights.values.any { it == 0 })
    }

    @Test
    fun rowsAndColumns() {
        val grid = listOf(
            "abc",
            "def",
        ).toGrid()

        assertEquals("def", grid.rows[1].joinToString(""))
        assertEquals(listOf("ad", "be", "cf"), grid.columns.map { it.joinToString("") })

        // rows[y][x] and columns[x][y] are both the value at Position(x, y)
        assertEquals('f', grid[Position(x = 2, y = 1)])
        assertEquals('f', grid.rows[1][2])
        assertEquals('f', grid.columns[2][1])

        // Rows and columns are lists, so two edges of a tile compare directly
        val tile = listOf(
            "#.#",
            "#..",
            "..#",
        ).toGrid()
        assertEquals(listOf('#', '.', '#'), tile.rows[0])
        assertEquals(tile.rows[0], tile.columns[2]) // top edge == right edge
        assertNotEquals(tile.rows[0], tile.columns[0]) // left edge is # # .
    }

    @Test
    fun safeReads() {
        val grid = listOf(
            "12",
            "34",
        ).toGrid { it.digitToInt() }
        val inside = Position(1, 0)
        val outside = Position(2, 0)

        assertEquals(2, grid.getOrNull(inside))
        assertNull(grid.getOrNull(outside))

        assertEquals(2, grid.getOrDefault(inside, 0))
        assertEquals(0, grid.getOrDefault(outside, 0))

        // getOrElse passes the outside position to the lambda
        assertEquals(20, grid.getOrElse(outside) { it.x * 10 })

        val sum = Direction.entries.sumOf { grid.getOrDefault(Position(0, 0) + it, 0) }
        assertEquals(5, sum) // 2 to the right + 3 below
    }

    @Test
    fun boundsCheck() {
        val grid = listOf(
            "...",
            "...",
        ).toGrid()

        // in tests the bounds: 0 <= x < width and 0 <= y < height
        assertTrue(Position(0, 0) in grid)
        assertTrue(Position(2, 1) in grid)

        assertTrue(Position(3, 0) !in grid) // x == width
        assertTrue(Position(-1, 0) !in grid)
        assertTrue(Position(0, 2) !in grid) // y == height
    }

    @Test
    fun findPositions() {
        val grid = listOf(
            "S.#",
            "#.E",
        ).toGrid()

        assertEquals(Position(0, 0), grid.positionOf('S'))
        assertEquals(Position(2, 1), grid.positionOf('E'))

        assertNull(grid.positionOfOrNull('@'))

        assertEquals(listOf(Position(2, 0), Position(0, 1)), grid.positionsOf('#'))
    }

    @Test
    fun equalityAndSnapshots() {
        // A read-only grid and a mutable grid with the same cells are equal
        val parsed = listOf("#.", ".#").toGrid()
        val built = MutableGrid(2, 2) { if (it.x == it.y) '#' else '.' }
        assertEquals<Grid<Char>>(parsed, built)
        assertEquals(parsed.hashCode(), built.hashCode())

        // Find when a changing grid returns to an earlier state
        val lights = listOf("#..").toMutableGrid()
        val firstSeen = mutableMapOf<Grid<Char>, Int>()
        var step = 0
        var snapshot = lights.toGrid() // later writes do not change it
        while (snapshot !in firstSeen) {
            firstSeen[snapshot] = step
            // Move every light one cell to the right, wrapping around
            for (p in lights.positions) lights[p] = snapshot[(p - Direction.RIGHT).wrap(lights)]
            step++
            snapshot = lights.toGrid()
        }

        // #.. -> .#. -> ..# -> #.. again
        assertEquals(3, step)
        assertEquals(0, firstSeen[snapshot])
    }

    @Test
    fun printing() {
        val input = listOf(
            "#..",
            ".#.",
        )
        val grid = input.toGrid()

        assertEquals("#..\n.#.", grid.toString())
        assertEquals(input.joinToString("\n"), grid.toString())

        // Other values print with their own toString, so map them to characters first
        val walls = grid.map { it == '#' }
        assertEquals("truefalsefalse\nfalsetruefalse", walls.toString())
        assertEquals("#..\n.#.", walls.map { if (it) '#' else '.' }.toString())
    }

    @Test
    fun copies() {
        val original = listOf("ab").toMutableGrid()
        val snapshot = original.toGrid()
        val copy = original.toMutableGrid()

        original[Position(0, 0)] = 'x'
        copy[Position(1, 0)] = 'y'

        assertEquals("xb", original.toString())
        assertEquals("ab", snapshot.toString())
        assertEquals("ay", copy.toString())
    }

    @Test
    fun neighbors() {
        val grid = listOf(
            "...",
            "...",
            "...",
        ).toGrid()

        // In a corner, only the neighbors inside the grid: right and down
        val corner = Position(0, 0)
        assertEquals(listOf(Position(1, 0), Position(0, 1)), grid.neighbors(corner))
        // With diagonals: right, down-right, down
        assertEquals(listOf(Position(1, 0), Position(1, 1), Position(0, 1)), grid.neighbors8(corner))

        // In the middle: up, right, down, left
        val middle = Position(1, 1)
        assertEquals(listOf(Position(1, 0), Position(2, 1), Position(1, 2), Position(0, 1)), grid.neighbors(middle))
        assertEquals(8, grid.neighbors8(middle).size)
    }

    @Test
    fun rays() {
        val grid = listOf(
            "a....",
            ".....",
            "..#.b",
        ).toGrid()

        // The start is excluded; the ray ends at the edge of the grid
        val right = grid.ray(Position(0, 0), Direction.RIGHT)
        assertEquals(listOf(Position(1, 0), Position(2, 0), Position(3, 0), Position(4, 0)), right)
        assertEquals(listOf(Position(1, 1), Position(2, 2)), grid.ray(Position(0, 0), Direction8.DOWN_RIGHT))

        val seen = grid.ray(Position(4, 0), Direction.DOWN).firstOrNull { grid[it] != '.' }
        assertEquals(Position(4, 2), seen)
        assertNull(grid.ray(Position(0, 0), Direction.DOWN).firstOrNull { grid[it] != '.' })

        // Starting just outside the grid gives the whole row
        assertEquals("..#.b", grid.ray(Position(-1, 2), Direction.RIGHT).map { grid[it] }.joinToString(""))
    }

    @Test
    fun wrapOnGrid() {
        // This pattern repeats endlessly in every direction
        val pattern = listOf(
            "#..",
            ".#.",
        ).toGrid()

        assertEquals(Position(1, 1), Position(4, 3).wrap(pattern))
        assertEquals('#', pattern[Position(4, 3).wrap(pattern)])
        assertEquals('#', pattern[Position(-3, -2).wrap(pattern)])

        val diagonal = (0..5).map { Position(it, it) }
        assertEquals(2, diagonal.count { pattern[it.wrap(pattern)] == '#' }) // (0, 0) and (1, 1)
    }

    @Test
    fun countCells() {
        val grid = listOf(
            "#.#",
            ".##",
        ).toGrid()

        assertEquals(4, grid.count { it == '#' })
        assertEquals(2, grid.count { it == '.' })
    }

    @Test
    fun iterateWithPositions() {
        val grid = listOf(
            "a.",
            ".b",
        ).toGrid()

        val letters = mutableMapOf<Char, Position>()
        grid.forEachIndexed { position, value ->
            if (value != '.') letters[value] = position
        }

        assertEquals(mapOf('a' to Position(0, 0), 'b' to Position(1, 1)), letters)
    }

    @Test
    fun mapCells() {
        val grid = listOf(
            "#.",
            "..",
        ).toGrid()

        val walls = grid.map { it == '#' }
        assertEquals(listOf(true, false, false, false), walls.values)

        val distances = grid.mapIndexed { position, value ->
            if (value == '#') -1 else position.manhattanDistanceTo(Position(0, 0))
        }
        assertEquals(listOf(-1, 1, 1, 2), distances.values)

        // The receiver is not changed
        assertEquals("#.\n..", grid.toString())
    }

    @Test
    fun rotateAndTranspose() {
        val grid = listOf(
            "abc",
            "def",
        ).toGrid()

        // The value at (x, y) moves to (y, x)
        assertEquals("ad\nbe\ncf", grid.transposed().toString())

        // The top row becomes the right column; width and height swap
        val clockwise = grid.rotatedClockwise()
        assertEquals("da\neb\nfc", clockwise.toString())
        assertEquals(2, clockwise.width)
        assertEquals(3, clockwise.height)

        // The right column becomes the top row
        assertEquals("cf\nbe\nad", grid.rotatedCounterclockwise().toString())

        // A mirror image, left to right
        assertEquals("cba\nfed", grid.transposed().rotatedClockwise().toString())
    }

    @Test
    fun countCrowdedCells() {
        val grid = listOf(
            "@@.@",
            "@@@.",
            ".@@@",
        ).toGrid()

        val accessible = grid.positions.filter { p ->
            grid[p] == '@' && grid.neighbors8(p).count { grid[it] == '@' } < 4
        }

        // (0, 0) has 3 such neighbors, (3, 0) has 1 and (3, 2) has 2; every other '@' has at least 4
        assertEquals(listOf(Position(0, 0), Position(3, 0), Position(3, 2)), accessible)
        assertEquals(3, accessible.size)
    }

    @Test
    fun walkUntilLeavingGrid() {
        val grid = listOf(
            "..#..",
            "....#",
            "#....",
            "..^..",
            "...#.",
        ).toGrid()

        var position = grid.positionOf('^')
        var direction = grid[position].toDirection() // UP
        val visited = mutableSetOf(position)

        while (true) {
            val ahead = position + direction
            when (grid.getOrNull(ahead)) {
                null -> break
                '#' -> direction = direction.turnRight()
                else -> {
                    position = ahead
                    visited += ahead
                }
            }
        }

        // Up to (2, 1), right to (3, 1), down to (3, 3), then left across the start and out
        assertEquals(Position(0, 3), position)
        assertEquals(Direction.LEFT, direction)
        assertEquals(8, visited.size)
    }
}
