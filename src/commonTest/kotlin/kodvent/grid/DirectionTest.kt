/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTest {

    @Test
    fun `Direction entries are UP RIGHT DOWN LEFT`() {
        assertEquals(
            listOf(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT),
            Direction.entries
        )
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
    fun `opposite swaps UP with DOWN and LEFT with RIGHT`() {
        assertEquals(Direction.DOWN, Direction.UP.opposite)
        assertEquals(Direction.UP, Direction.DOWN.opposite)
        assertEquals(Direction.RIGHT, Direction.LEFT.opposite)
        assertEquals(Direction.LEFT, Direction.RIGHT.opposite)
    }

    @Test
    fun `Direction times a step count is the offset of that many steps`() {
        assertEquals(Position(0, -3), Direction.UP * 3)
        assertEquals(Position(3, 0), Direction.RIGHT * 3)
        assertEquals(Position(0, 3), Direction.DOWN * 3)
        assertEquals(Position(-3, 0), Direction.LEFT * 3)
        assertEquals(Position(0, 0), Direction.UP * 0)
        assertEquals(Position(0, 0), Direction.LEFT * 0)
        assertEquals(Position(0, 2), Direction.UP * -2)
        assertEquals(Position(-2, 0), Direction.RIGHT * -2)
        assertEquals(Position(0, -Int.MAX_VALUE), Direction.UP * Int.MAX_VALUE)
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
    fun `Direction8 opposite swaps each direction with the one across`() {
        assertEquals(Direction8.DOWN, Direction8.UP.opposite)
        assertEquals(Direction8.DOWN_LEFT, Direction8.UP_RIGHT.opposite)
        assertEquals(Direction8.LEFT, Direction8.RIGHT.opposite)
        assertEquals(Direction8.UP_LEFT, Direction8.DOWN_RIGHT.opposite)
        assertEquals(Direction8.UP, Direction8.DOWN.opposite)
        assertEquals(Direction8.UP_RIGHT, Direction8.DOWN_LEFT.opposite)
        assertEquals(Direction8.RIGHT, Direction8.LEFT.opposite)
        assertEquals(Direction8.DOWN_RIGHT, Direction8.UP_LEFT.opposite)
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
        assertEquals(Position(0, 0), Direction8.UP_LEFT * 0)
        assertEquals(Position(-3, 3), Direction8.UP_RIGHT * -3)
        assertEquals(Position(Int.MAX_VALUE, -Int.MAX_VALUE), Direction8.UP_RIGHT * Int.MAX_VALUE)
    }

    @Test
    fun `each arrow converts to its direction`() {
        val clockwise = listOf(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)

        assertEquals(clockwise, "^>v<".map { it.toDirection() })
        assertEquals(clockwise, "^>v<".map { it.toDirectionOrNull() })
    }

    @Test
    fun `toDirection throws for every ASCII character that is not an arrow and for the Unicode arrows`() {
        val notRejected = (('\u0000'..'\u007F') + ('\u2190'..'\u2193')).filter {
            it !in "^>v<" && runCatching { it.toDirection() }.exceptionOrNull() !is IllegalArgumentException
        }

        assertEquals(emptyList(), notRejected)
    }

    @Test
    fun `the four arrows are the only characters that convert to a direction`() {
        val accepted = (Char.MIN_VALUE..Char.MAX_VALUE).filter { it.toDirectionOrNull() != null }

        // In code order: '<' is 60, '>' is 62, '^' is 94 and 'v' is 118.
        assertEquals(listOf('<', '>', '^', 'v'), accepted)
    }
}
