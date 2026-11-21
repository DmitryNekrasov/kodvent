/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DirectionTest {

    @Test
    fun `Direction entries are UP RIGHT DOWN LEFT`() {
        assertEquals(
            listOf(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT),
            Direction.entries
        )
    }

    @Test
    fun `Direction UP is y minus one`() {
        assertEquals(Position(0, -1), Position(0, 0) + Direction.UP)
    }

    @Test
    fun `Direction RIGHT is x plus one`() {
        assertEquals(Position(1, 0), Position(0, 0) + Direction.RIGHT)
    }

    @Test
    fun `Direction DOWN is y plus one`() {
        assertEquals(Position(0, 1), Position(0, 0) + Direction.DOWN)
    }

    @Test
    fun `Direction LEFT is x minus one`() {
        assertEquals(Position(-1, 0), Position(0, 0) + Direction.LEFT)
    }

    @Test
    fun `a Direction step adds the same offset at negative coordinates`() {
        val start = Position(-3, -7)

        assertEquals(Position(-3, -8), start + Direction.UP)
        assertEquals(Position(-2, -7), start + Direction.RIGHT)
        assertEquals(Position(-3, -6), start + Direction.DOWN)
        assertEquals(Position(-4, -7), start + Direction.LEFT)
    }

    @Test
    fun `each Direction leads to the adjacent cell of a grid`() {
        val grid = listOf("abcd", "efgh", "ijkl").toGrid()
        val cell = Position(1, 1)

        assertEquals('b', grid[cell + Direction.UP])
        assertEquals('g', grid[cell + Direction.RIGHT])
        assertEquals('j', grid[cell + Direction.DOWN])
        assertEquals('e', grid[cell + Direction.LEFT])
    }

    @Test
    fun `Direction steps over the edge of a grid land outside it`() {
        val grid = listOf("abcd", "efgh", "ijkl").toGrid()
        val topLeft = Position(0, 0)
        val bottomRight = Position(3, 2)

        assertEquals(
            listOf(Direction.RIGHT, Direction.DOWN),
            Direction.entries.filter { (topLeft + it) in grid }
        )
        assertEquals(
            listOf(Direction.UP, Direction.LEFT),
            Direction.entries.filter { (bottomRight + it) in grid }
        )
    }

    @Test
    fun `a Direction step from outside a grid can land on its edge`() {
        val grid = listOf("abcd", "efgh", "ijkl").toGrid()

        assertEquals('e', grid[Position(-1, 1) + Direction.RIGHT])
        assertEquals('h', grid[Position(4, 1) + Direction.LEFT])
        assertEquals('b', grid[Position(1, -1) + Direction.DOWN])
        assertEquals('j', grid[Position(1, 3) + Direction.UP])
    }

    @Test
    fun `no step from the origin is inside a grid without cells`() {
        val origin = Position(0, 0)
        val gridsWithoutCells = listOf(Grid(0, 0) { '.' }, Grid(0, 2) { '.' }, Grid(3, 0) { '.' })

        for (grid in gridsWithoutCells) {
            assertTrue(Direction.entries.none { (origin + it) in grid })
            assertTrue(Direction8.entries.none { (origin + it) in grid })
        }
    }

    @Test
    fun `turnRight goes UP RIGHT DOWN LEFT and back to UP`() {
        assertEquals(Direction.RIGHT, Direction.UP.turnRight())
        assertEquals(Direction.DOWN, Direction.RIGHT.turnRight())
        assertEquals(Direction.LEFT, Direction.DOWN.turnRight())
        assertEquals(Direction.UP, Direction.LEFT.turnRight())
    }

    @Test
    fun `turnLeft goes UP LEFT DOWN RIGHT and back to UP`() {
        assertEquals(Direction.LEFT, Direction.UP.turnLeft())
        assertEquals(Direction.DOWN, Direction.LEFT.turnLeft())
        assertEquals(Direction.RIGHT, Direction.DOWN.turnLeft())
        assertEquals(Direction.UP, Direction.RIGHT.turnLeft())
    }

    @Test
    fun `turnLeft undoes turnRight`() {
        for (direction in Direction.entries) {
            assertEquals(direction, direction.turnRight().turnLeft())
        }
    }

    @Test
    fun `turnRight undoes turnLeft`() {
        for (direction in Direction.entries) {
            assertEquals(direction, direction.turnLeft().turnRight())
        }
    }

    @Test
    fun `four right turns give the starting direction`() {
        for (direction in Direction.entries) {
            assertEquals(direction, direction.turnRight().turnRight().turnRight().turnRight())
        }
    }

    @Test
    fun `four left turns give the starting direction`() {
        for (direction in Direction.entries) {
            assertEquals(direction, direction.turnLeft().turnLeft().turnLeft().turnLeft())
        }
    }

    @Test
    fun `two right turns give the opposite direction`() {
        for (direction in Direction.entries) {
            assertEquals(direction.opposite, direction.turnRight().turnRight())
        }
    }

    @Test
    fun `two left turns give the opposite direction`() {
        for (direction in Direction.entries) {
            assertEquals(direction.opposite, direction.turnLeft().turnLeft())
        }
    }

    @Test
    fun `three right turns equal one left turn`() {
        for (direction in Direction.entries) {
            assertEquals(direction.turnLeft(), direction.turnRight().turnRight().turnRight())
        }
    }

    @Test
    fun `three left turns equal one right turn`() {
        for (direction in Direction.entries) {
            assertEquals(direction.turnRight(), direction.turnLeft().turnLeft().turnLeft())
        }
    }

    @Test
    fun `turnRight rotates the offset clockwise with y growing downward`() {
        for (direction in Direction.entries) {
            val offset = Position(0, 0) + direction
            // Clockwise on a screen whose y axis points down: (dx, dy) becomes (-dy, dx).
            assertEquals(Position(-offset.y, offset.x), Position(0, 0) + direction.turnRight())
        }
    }

    @Test
    fun `turnLeft rotates the offset counterclockwise with y growing downward`() {
        for (direction in Direction.entries) {
            val offset = Position(0, 0) + direction
            // Counterclockwise on a screen whose y axis points down: (dx, dy) becomes (dy, -dx).
            assertEquals(Position(offset.y, -offset.x), Position(0, 0) + direction.turnLeft())
        }
    }

    @Test
    fun `stepping and turning right walks a square clockwise`() {
        val headings = generateSequence(Direction.UP) { it.turnRight() }.take(4).toList()
        val visited = headings.runningFold(Position(0, 0)) { position, heading -> position + heading }

        assertEquals(
            listOf(Position(0, 0), Position(0, -1), Position(1, -1), Position(1, 0), Position(0, 0)),
            visited
        )
    }

    @Test
    fun `stepping and turning left walks a square counterclockwise`() {
        val headings = generateSequence(Direction.UP) { it.turnLeft() }.take(4).toList()
        val visited = headings.runningFold(Position(0, 0)) { position, heading -> position + heading }

        assertEquals(
            listOf(Position(0, 0), Position(0, -1), Position(-1, -1), Position(-1, 0), Position(0, 0)),
            visited
        )
    }

    @Test
    fun `opposite swaps UP and DOWN`() {
        assertEquals(Direction.DOWN, Direction.UP.opposite)
        assertEquals(Direction.UP, Direction.DOWN.opposite)
    }

    @Test
    fun `opposite swaps LEFT and RIGHT`() {
        assertEquals(Direction.RIGHT, Direction.LEFT.opposite)
        assertEquals(Direction.LEFT, Direction.RIGHT.opposite)
    }

    @Test
    fun `opposite of opposite is the same Direction`() {
        for (direction in Direction.entries) {
            assertEquals(direction, direction.opposite.opposite)
        }
    }

    @Test
    fun `opposite Direction has the negated offset`() {
        for (direction in Direction.entries) {
            assertEquals(Position(0, 0) - direction, Position(0, 0) + direction.opposite)
        }
    }

    @Test
    fun `a step and a step in the opposite Direction cancel out`() {
        val start = Position(-2, 6)

        for (direction in Direction.entries) {
            assertEquals(start, start + direction + direction.opposite)
        }
    }

    @Test
    fun `Direction times a step count is the offset of that many steps`() {
        assertEquals(Position(0, -3), Direction.UP * 3)
        assertEquals(Position(3, 0), Direction.RIGHT * 3)
        assertEquals(Position(0, 3), Direction.DOWN * 3)
        assertEquals(Position(-3, 0), Direction.LEFT * 3)
    }

    @Test
    fun `Direction times one is the offset of a single step`() {
        for (direction in Direction.entries) {
            assertEquals(Position(0, 0) + direction, direction * 1)
        }
    }

    @Test
    fun `Direction times zero is the zero offset`() {
        for (direction in Direction.entries) {
            assertEquals(Position(0, 0), direction * 0)
        }
    }

    @Test
    fun `Direction times a negative count points the opposite way`() {
        assertEquals(Position(0, 2), Direction.UP * -2)
        assertEquals(Position(-2, 0), Direction.RIGHT * -2)
        assertEquals(Position(0, -2), Direction.DOWN * -2)
        assertEquals(Position(2, 0), Direction.LEFT * -2)

        for (direction in Direction.entries) {
            assertEquals(direction.opposite * 2, direction * -2)
        }
    }

    @Test
    fun `Direction times the largest Int stays exact`() {
        assertEquals(Position(0, -Int.MAX_VALUE), Direction.UP * Int.MAX_VALUE)
        assertEquals(Position(Int.MAX_VALUE, 0), Direction.RIGHT * Int.MAX_VALUE)
        assertEquals(Position(0, Int.MAX_VALUE), Direction.DOWN * Int.MAX_VALUE)
        assertEquals(Position(-Int.MAX_VALUE, 0), Direction.LEFT * Int.MAX_VALUE)
    }

    @Test
    fun `position plus Direction times n moves n cells`() {
        val start = Position(2, 5)

        assertEquals(Position(2, 2), start + Direction.UP * 3)
        assertEquals(Position(5, 5), start + Direction.RIGHT * 3)
        assertEquals(Position(2, 8), start + Direction.DOWN * 3)
        assertEquals(Position(-1, 5), start + Direction.LEFT * 3)
    }

    @Test
    fun `position plus Direction times n equals n single steps`() {
        val start = Position(-4, 2)

        for (direction in Direction.entries) {
            val stepped = (1..5).fold(start) { position, _ -> position + direction }
            assertEquals(stepped, start + direction * 5)
        }
    }

    @Test
    fun `Direction8 entries are the eight directions clockwise from UP`() {
        assertEquals(
            listOf(
                Direction8.UP,
                Direction8.UP_RIGHT,
                Direction8.RIGHT,
                Direction8.DOWN_RIGHT,
                Direction8.DOWN,
                Direction8.DOWN_LEFT,
                Direction8.LEFT,
                Direction8.UP_LEFT
            ),
            Direction8.entries
        )
    }

    @Test
    fun `Direction8 UP is y minus one`() {
        assertEquals(Position(0, -1), Position(0, 0) + Direction8.UP)
    }

    @Test
    fun `Direction8 UP RIGHT is x plus one and y minus one`() {
        assertEquals(Position(1, -1), Position(0, 0) + Direction8.UP_RIGHT)
    }

    @Test
    fun `Direction8 RIGHT is x plus one`() {
        assertEquals(Position(1, 0), Position(0, 0) + Direction8.RIGHT)
    }

    @Test
    fun `Direction8 DOWN RIGHT is x plus one and y plus one`() {
        assertEquals(Position(1, 1), Position(0, 0) + Direction8.DOWN_RIGHT)
    }

    @Test
    fun `Direction8 DOWN is y plus one`() {
        assertEquals(Position(0, 1), Position(0, 0) + Direction8.DOWN)
    }

    @Test
    fun `Direction8 DOWN LEFT is x minus one and y plus one`() {
        assertEquals(Position(-1, 1), Position(0, 0) + Direction8.DOWN_LEFT)
    }

    @Test
    fun `Direction8 LEFT is x minus one`() {
        assertEquals(Position(-1, 0), Position(0, 0) + Direction8.LEFT)
    }

    @Test
    fun `Direction8 UP LEFT is x minus one and y minus one`() {
        assertEquals(Position(-1, -1), Position(0, 0) + Direction8.UP_LEFT)
    }

    @Test
    fun `Direction8 offsets are the eight cells around the origin`() {
        val around = (-1..1).flatMap { y -> (-1..1).map { x -> Position(x, y) } }
            .filter { it != Position(0, 0) }
            .toSet()

        assertEquals(8, around.size)
        assertEquals(around, Direction8.entries.map { Position(0, 0) + it }.toSet())
    }

    @Test
    fun `Direction8 straight constants have the offsets of the Direction constants with the same names`() {
        for (direction in Direction.entries) {
            val sameName = Direction8.valueOf(direction.name)
            assertEquals(Position(0, 0) + direction, Position(0, 0) + sameName)
            assertEquals(direction * 4, sameName * 4)
        }
    }

    @Test
    fun `a Direction8 step adds the same offset at negative coordinates`() {
        val start = Position(-3, -7)

        assertEquals(
            listOf(
                Position(-3, -8),
                Position(-2, -8),
                Position(-2, -7),
                Position(-2, -6),
                Position(-3, -6),
                Position(-4, -6),
                Position(-4, -7),
                Position(-4, -8)
            ),
            Direction8.entries.map { start + it }
        )
    }

    @Test
    fun `Direction8 entries go around a cell clockwise starting above it`() {
        val grid = listOf("abcd", "efgh", "ijkl").toGrid()
        val cell = Position(1, 1)

        assertEquals("bcgkjiea", Direction8.entries.map { grid[cell + it] }.joinToString(""))
    }

    @Test
    fun `Direction8 steps over the edge of a grid land outside it`() {
        val grid = listOf("abcd", "efgh", "ijkl").toGrid()
        val topLeft = Position(0, 0)
        val bottomRight = Position(3, 2)

        assertEquals(
            listOf(Direction8.RIGHT, Direction8.DOWN_RIGHT, Direction8.DOWN),
            Direction8.entries.filter { (topLeft + it) in grid }
        )
        assertEquals(
            listOf(Direction8.UP, Direction8.LEFT, Direction8.UP_LEFT),
            Direction8.entries.filter { (bottomRight + it) in grid }
        )
    }

    @Test
    fun `Direction8 opposite swaps UP and DOWN`() {
        assertEquals(Direction8.DOWN, Direction8.UP.opposite)
        assertEquals(Direction8.UP, Direction8.DOWN.opposite)
    }

    @Test
    fun `Direction8 opposite swaps LEFT and RIGHT`() {
        assertEquals(Direction8.RIGHT, Direction8.LEFT.opposite)
        assertEquals(Direction8.LEFT, Direction8.RIGHT.opposite)
    }

    @Test
    fun `Direction8 opposite swaps UP RIGHT and DOWN LEFT`() {
        assertEquals(Direction8.DOWN_LEFT, Direction8.UP_RIGHT.opposite)
        assertEquals(Direction8.UP_RIGHT, Direction8.DOWN_LEFT.opposite)
    }

    @Test
    fun `Direction8 opposite swaps DOWN RIGHT and UP LEFT`() {
        assertEquals(Direction8.UP_LEFT, Direction8.DOWN_RIGHT.opposite)
        assertEquals(Direction8.DOWN_RIGHT, Direction8.UP_LEFT.opposite)
    }

    @Test
    fun `opposite of opposite is the same Direction8`() {
        for (direction in Direction8.entries) {
            assertEquals(direction, direction.opposite.opposite)
        }
    }

    @Test
    fun `opposite Direction8 has the negated offset`() {
        for (direction in Direction8.entries) {
            assertEquals(Position(0, 0) - direction, Position(0, 0) + direction.opposite)
        }
    }

    @Test
    fun `a step and a step in the opposite Direction8 cancel out`() {
        val start = Position(-2, 6)

        for (direction in Direction8.entries) {
            assertEquals(start, start + direction + direction.opposite)
        }
    }

    @Test
    fun `Direction8 times a step count is the offset of that many steps`() {
        assertEquals(Position(0, -2), Direction8.UP * 2)
        assertEquals(Position(2, -2), Direction8.UP_RIGHT * 2)
        assertEquals(Position(2, 0), Direction8.RIGHT * 2)
        assertEquals(Position(2, 2), Direction8.DOWN_RIGHT * 2)
        assertEquals(Position(0, 2), Direction8.DOWN * 2)
        assertEquals(Position(-2, 2), Direction8.DOWN_LEFT * 2)
        assertEquals(Position(-2, 0), Direction8.LEFT * 2)
        assertEquals(Position(-2, -2), Direction8.UP_LEFT * 2)
    }

    @Test
    fun `Direction8 times one is the offset of a single step`() {
        for (direction in Direction8.entries) {
            assertEquals(Position(0, 0) + direction, direction * 1)
        }
    }

    @Test
    fun `Direction8 times zero is the zero offset`() {
        for (direction in Direction8.entries) {
            assertEquals(Position(0, 0), direction * 0)
        }
    }

    @Test
    fun `Direction8 times a negative count points the opposite way`() {
        assertEquals(Position(0, 3), Direction8.UP * -3)
        assertEquals(Position(-3, 3), Direction8.UP_RIGHT * -3)
        assertEquals(Position(-3, -3), Direction8.DOWN_RIGHT * -3)
        assertEquals(Position(3, 0), Direction8.LEFT * -3)

        for (direction in Direction8.entries) {
            assertEquals(direction.opposite * 3, direction * -3)
        }
    }

    @Test
    fun `Direction8 times the largest Int stays exact`() {
        assertEquals(Position(Int.MAX_VALUE, -Int.MAX_VALUE), Direction8.UP_RIGHT * Int.MAX_VALUE)
        assertEquals(Position(Int.MAX_VALUE, Int.MAX_VALUE), Direction8.DOWN_RIGHT * Int.MAX_VALUE)
        assertEquals(Position(-Int.MAX_VALUE, Int.MAX_VALUE), Direction8.DOWN_LEFT * Int.MAX_VALUE)
        assertEquals(Position(-Int.MAX_VALUE, -Int.MAX_VALUE), Direction8.UP_LEFT * Int.MAX_VALUE)
    }

    @Test
    fun `position plus Direction8 times n moves n cells`() {
        val start = Position(2, 5)

        assertEquals(Position(2, 2), start + Direction8.UP * 3)
        assertEquals(Position(5, 2), start + Direction8.UP_RIGHT * 3)
        assertEquals(Position(5, 5), start + Direction8.RIGHT * 3)
        assertEquals(Position(5, 8), start + Direction8.DOWN_RIGHT * 3)
        assertEquals(Position(2, 8), start + Direction8.DOWN * 3)
        assertEquals(Position(-1, 8), start + Direction8.DOWN_LEFT * 3)
        assertEquals(Position(-1, 5), start + Direction8.LEFT * 3)
        assertEquals(Position(-1, 2), start + Direction8.UP_LEFT * 3)
    }

    @Test
    fun `position plus Direction8 times n equals n single steps`() {
        val start = Position(-4, 2)

        for (direction in Direction8.entries) {
            val stepped = (1..5).fold(start) { position, _ -> position + direction }
            assertEquals(stepped, start + direction * 5)
        }
    }

    @Test
    fun `caret converts to UP`() {
        assertEquals(Direction.UP, '^'.toDirection())
    }

    @Test
    fun `greater than sign converts to RIGHT`() {
        assertEquals(Direction.RIGHT, '>'.toDirection())
    }

    @Test
    fun `lowercase v converts to DOWN`() {
        assertEquals(Direction.DOWN, 'v'.toDirection())
    }

    @Test
    fun `less than sign converts to LEFT`() {
        assertEquals(Direction.LEFT, '<'.toDirection())
    }

    @Test
    fun `toDirectionOrNull returns the direction of each arrow`() {
        assertEquals(Direction.UP, '^'.toDirectionOrNull())
        assertEquals(Direction.RIGHT, '>'.toDirectionOrNull())
        assertEquals(Direction.DOWN, 'v'.toDirectionOrNull())
        assertEquals(Direction.LEFT, '<'.toDirectionOrNull())
    }

    @Test
    fun `the arrows in clockwise order convert to the Direction entries in order`() {
        assertEquals(
            listOf(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT),
            "^>v<".map { it.toDirection() }
        )
    }

    @Test
    fun `a position follows a string of arrows`() {
        // (0, 0) > (1, 0) > (2, 0) v (2, 1) < (1, 1) ^ (1, 0)
        val end = ">>v<^".fold(Position(0, 0)) { position, arrow -> position + arrow.toDirection() }

        assertEquals(Position(1, 0), end)
    }

    @Test
    fun `toDirectionOrNull tells which cells of a grid hold an arrow`() {
        val grid = listOf(".>.", "v.^").toGrid()

        assertEquals(
            listOf(Position(1, 0), Position(0, 1), Position(2, 1)),
            grid.positions.filter { grid[it].toDirectionOrNull() != null }
        )
        assertEquals(
            listOf(Direction.RIGHT, Direction.DOWN, Direction.UP),
            grid.values.mapNotNull { it.toDirectionOrNull() }
        )
    }

    @Test
    fun `toDirection throws for the letters U R D L`() {
        assertFailsWith<IllegalArgumentException> { 'U'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'R'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'D'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'L'.toDirection() }
    }

    @Test
    fun `toDirection throws for the compass letters N E S W`() {
        assertFailsWith<IllegalArgumentException> { 'N'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'E'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'S'.toDirection() }
        assertFailsWith<IllegalArgumentException> { 'W'.toDirection() }
    }

    @Test
    fun `toDirection throws for an uppercase V`() {
        assertFailsWith<IllegalArgumentException> { 'V'.toDirection() }
    }

    @Test
    fun `toDirection throws for a dot`() {
        assertFailsWith<IllegalArgumentException> { '.'.toDirection() }
    }

    @Test
    fun `toDirection throws for lowercase direction letters`() {
        for (letter in "urdlnesw") {
            assertFailsWith<IllegalArgumentException>("letter $letter") { letter.toDirection() }
        }
    }

    @Test
    fun `toDirection throws for Unicode arrows`() {
        // Leftwards, upwards, rightwards and downwards arrows: U+2190..U+2193.
        for (arrow in '\u2190'..'\u2193') {
            assertFailsWith<IllegalArgumentException>("character code ${arrow.code}") { arrow.toDirection() }
        }
    }

    @Test
    fun `toDirection throws for every ASCII character that is not an arrow`() {
        for (character in '\u0000'..'\u007F') {
            if (character !in "^>v<") {
                assertFailsWith<IllegalArgumentException>("character code ${character.code}") {
                    character.toDirection()
                }
            }
        }
    }

    @Test
    fun `toDirectionOrNull returns null for the letters U R D L`() {
        assertNull('U'.toDirectionOrNull())
        assertNull('R'.toDirectionOrNull())
        assertNull('D'.toDirectionOrNull())
        assertNull('L'.toDirectionOrNull())
    }

    @Test
    fun `toDirectionOrNull returns null for the compass letters N E S W`() {
        assertNull('N'.toDirectionOrNull())
        assertNull('E'.toDirectionOrNull())
        assertNull('S'.toDirectionOrNull())
        assertNull('W'.toDirectionOrNull())
    }

    @Test
    fun `toDirectionOrNull returns null for an uppercase V`() {
        assertNull('V'.toDirectionOrNull())
    }

    @Test
    fun `toDirectionOrNull returns null for a dot`() {
        assertNull('.'.toDirectionOrNull())
    }

    @Test
    fun `toDirectionOrNull returns null for lowercase direction letters`() {
        for (letter in "urdlnesw") {
            assertNull(letter.toDirectionOrNull())
        }
    }

    @Test
    fun `toDirectionOrNull returns null for Unicode arrows`() {
        // Leftwards, upwards, rightwards and downwards arrows: U+2190..U+2193.
        for (arrow in '\u2190'..'\u2193') {
            assertNull(arrow.toDirectionOrNull())
        }
    }

    @Test
    fun `the four arrows are the only characters that convert to a direction`() {
        val accepted = (Char.MIN_VALUE..Char.MAX_VALUE).filter { it.toDirectionOrNull() != null }

        // In code order: '<' is 60, '>' is 62, '^' is 94 and 'v' is 118.
        assertEquals(listOf('<', '>', '^', 'v'), accepted)
    }
}
