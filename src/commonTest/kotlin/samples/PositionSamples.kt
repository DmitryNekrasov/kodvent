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
            "..#.",
            "....",
            ".@..",
        )
        val grid = lines.toGrid()

        // x is the column counted from the left, y is the line counted from the top
        val rock = Position(x = 2, y = 0)
        assertEquals('#', grid[rock])

        // A puzzle's "x,y" pair goes into Position(x, y) unchanged
        val robot = "1,2".split(",").let { (x, y) -> Position(x.toInt(), y.toInt()) }
        assertEquals('@', grid[robot])

        // Destructuring gives x first and then y, and the same cell is lines[y][x]
        val (x, y) = robot
        assertEquals(1, x)
        assertEquals(2, y)
        assertEquals('@', lines[y][x])

        // Code that reads lines[row][column] names the arguments, so the two numbers cannot be swapped
        val row = 2
        val column = 1
        assertEquals(lines[row][column], grid[Position(x = column, y = row)])
    }

    @Test
    fun arithmetic() {
        val a = Position(4, 3)
        val b = Position(6, 2)

        // a - b is the offset that leads from b to a
        assertEquals(Position(-2, 1), a - b)

        // Adding it to a gives the point that mirrors b across a
        assertEquals(Position(2, 4), a + (a - b))

        // The next point on the same line: the factor goes on the right
        assertEquals(Position(0, 5), a + (a - b) * 2)

        val start = Position(2, 7)
        val velocity = Position(3, -1)
        assertEquals(Position(17, 2), start + velocity * 5)
    }

    @Test
    fun stepInDirection() {
        val robot = Position(3, 4)

        // y grows downward, so UP is y - 1
        assertEquals(Position(3, 3), robot + Direction.UP)
        assertEquals(Position(4, 4), robot + Direction.RIGHT)

        assertEquals(Position(2, 4), robot - Direction.RIGHT)
        assertEquals(robot, robot + Direction.UP - Direction.UP)

        assertEquals(Position(3, 7), robot + Direction.DOWN * 3)

        assertEquals(Position(4, 3), robot + Direction8.UP_RIGHT)
        assertEquals(Position(2, 5), robot - Direction8.UP_RIGHT)
    }

    @Test
    fun manhattanDistance() {
        val start = Position(2, 7)
        val goal = Position(5, 3)

        // |2 - 5| + |7 - 3| = 3 + 4, the number of orthogonal steps from one cell to the other
        assertEquals(7, start.manhattanDistanceTo(goal))
        assertEquals(7, goal.manhattanDistanceTo(start))

        // The nearest of several positions: they are 9, 3 and 5 steps away
        val keys = listOf(Position(9, 9), Position(0, 6), Position(4, 4))
        val nearest = keys.minBy { start.manhattanDistanceTo(it) }
        assertEquals(Position(0, 6), nearest)
    }

    @Test
    fun wrapOnTorus() {
        val width = 8
        val height = 5
        val start = Position(1, 3)
        val velocity = Position(-3, 2)

        val moved = start + velocity
        assertEquals(Position(-2, 5), moved)

        // wrap is a floor modulo, so the drone comes back in from the right and from the top:
        // x = -2 wraps to 6, not to -2
        assertEquals(Position(6, 0), moved.wrap(width, height))

        // Wrapping once at the end gives the same cell as wrapping after every move
        var drone = start
        repeat(4) { drone = (drone + velocity).wrap(width, height) }
        assertEquals(Position(5, 1), drone)
        assertEquals(drone, (start + velocity * 4).wrap(width, height))
    }
}
