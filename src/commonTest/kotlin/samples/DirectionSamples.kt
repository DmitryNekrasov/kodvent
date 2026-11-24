/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.grid.Direction
import kodvent.grid.Direction8
import kodvent.grid.Position
import kodvent.grid.toDirection
import kodvent.grid.toDirectionOrNull
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DirectionSamples {

    @Test
    fun turning() {
        // The directions are listed clockwise, so turnRight() gives the next one and wraps around
        assertEquals(Direction.RIGHT, Direction.UP.turnRight())
        assertEquals(Direction.UP, Direction.LEFT.turnRight())

        assertEquals(Direction.LEFT, Direction.UP.turnLeft())
        assertEquals(Direction.DOWN, Direction.UP.opposite)
    }

    @Test
    fun stepsInDirection() {
        assertEquals(Position(0, -3), Direction.UP * 3)

        val start = Position(5, 5)
        assertEquals(Position(5, 2), start + Direction.UP * 3)
    }

    @Test
    fun eightDirections() {
        assertEquals(8, Direction8.entries.size)
        assertEquals(listOf(Direction8.UP, Direction8.UP_RIGHT, Direction8.RIGHT), Direction8.entries.take(3))
        assertEquals(Direction8.DOWN_LEFT, Direction8.UP_RIGHT.opposite)

        assertEquals(Position(3, -3), Direction8.UP_RIGHT * 3)
    }

    @Test
    fun arrowsToDirections() {
        assertEquals(Direction.entries, "^>v<".map { it.toDirection() })

        // Only the four arrows are directions: letters are not accepted
        assertNull('L'.toDirectionOrNull())
        assertFailsWith<IllegalArgumentException> { 'L'.toDirection() }

        val grid = listOf(
            "..#.",
            "#.<.",
        ).toGrid()
        val walker = grid.positions.first { grid[it].toDirectionOrNull() != null }
        assertEquals(Position(2, 1), walker)
        assertEquals(Direction.LEFT, grid[walker].toDirection())
    }
}
