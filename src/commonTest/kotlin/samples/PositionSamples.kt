/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.grid.Direction
import kodvent.grid.Direction8
import kodvent.grid.Position
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals

class PositionSamples {

    @Test
    fun coordinates() {
        val lines = listOf(
            "....",
            "....",
            ".@..",
        )
        val grid = lines.toGrid()

        val robot = Position(x = 1, y = 2)
        assertEquals('@', grid[robot])
        assertEquals('@', lines[robot.y][robot.x])
    }

    @Test
    fun arithmetic() {
        val start = Position(2, 7)
        val velocity = Position(3, -1)

        val next = start + velocity
        assertEquals(Position(5, 6), next)
        assertEquals(velocity, next - start)

        // Five moves at once: the factor goes on the right
        assertEquals(Position(17, 2), start + velocity * 5)
    }

    @Test
    fun orthogonalStep() {
        val robot = Position(3, 4)

        // y grows downward, so UP is y - 1
        assertEquals(Position(3, 3), robot + Direction.UP)
        assertEquals(Position(4, 4), robot + Direction.RIGHT)

        assertEquals(Position(2, 4), robot - Direction.RIGHT)
        assertEquals(robot, robot + Direction.UP - Direction.UP)
    }

    @Test
    fun diagonalStep() {
        val robot = Position(3, 4)

        assertEquals(Position(4, 3), robot + Direction8.UP_RIGHT)
        assertEquals(Position(2, 5), robot - Direction8.UP_RIGHT)
    }

    @Test
    fun manhattanDistance() {
        val start = Position(2, 7)
        val goal = Position(5, 3)

        // |2 - 5| + |7 - 3| = 3 + 4, the number of orthogonal steps from one cell to the other
        assertEquals(7, start.manhattanDistanceTo(goal))
    }

    @Test
    fun wrapOnTorus() {
        val drone = Position(1, 3)
        val velocity = Position(-3, 2)

        val moved = drone + velocity
        assertEquals(Position(-2, 5), moved)

        // wrap is a floor modulo, so the drone comes back in from the right and from the top:
        // x = -2 wraps to 6, not to -2
        assertEquals(Position(6, 0), moved.wrap(width = 8, height = 5))

        // Four moves at once: wrapping at the end gives the same cell as wrapping after every move
        assertEquals(Position(5, 1), (drone + velocity * 4).wrap(width = 8, height = 5))
    }
}
