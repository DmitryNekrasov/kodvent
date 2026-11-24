/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.grid

/**
 * One of the four orthogonal directions, listed clockwise from [UP]. y grows downward, so [UP] is `y - 1`.
 *
 * @sample samples.DirectionSamples.turning
 * @sample samples.DirectionSamples.stepsInDirection
 */
public enum class Direction(internal val dx: Int, internal val dy: Int) {
    /** Towards the first input line (north on a map): `y - 1`. */
    UP(0, -1),

    /** Towards the end of a line (east on a map): `x + 1`. */
    RIGHT(1, 0),

    /** Towards the last input line (south on a map): `y + 1`. */
    DOWN(0, 1),

    /** Towards the start of a line (west on a map): `x - 1`. */
    LEFT(-1, 0);

    /**
     * Returns the direction after a 90-degree clockwise turn: UP, RIGHT, DOWN, LEFT and back to UP.
     *
     * @sample samples.DirectionSamples.turning
     */
    public fun turnRight(): Direction = entries[(ordinal + 1) % 4]

    /**
     * Returns the direction after a 90-degree counterclockwise turn: UP, LEFT, DOWN, RIGHT and back to UP.
     *
     * @sample samples.DirectionSamples.turning
     */
    public fun turnLeft(): Direction = entries[(ordinal + 3) % 4]

    /**
     * The direction pointing the opposite way: UP and DOWN swap, LEFT and RIGHT swap.
     *
     * @sample samples.DirectionSamples.turning
     */
    public val opposite: Direction get() = entries[(ordinal + 2) % 4]

    /**
     * Returns the offset of [steps] steps in this direction, so `position + direction * steps` moves that far.
     *
     * @sample samples.DirectionSamples.stepsInDirection
     */
    public operator fun times(steps: Int): Position = Position(dx * steps, dy * steps)
}

/**
 * One of the eight directions to a surrounding cell, diagonals included, listed clockwise from [UP]. It is
 * a separate type, so `Direction.entries` always means four.
 *
 * @sample samples.DirectionSamples.eightDirections
 */
public enum class Direction8(internal val dx: Int, internal val dy: Int) {
    /** `y - 1`. */
    UP(0, -1),

    /** `x + 1`, `y - 1`. */
    UP_RIGHT(1, -1),

    /** `x + 1`. */
    RIGHT(1, 0),

    /** `x + 1`, `y + 1`. */
    DOWN_RIGHT(1, 1),

    /** `y + 1`. */
    DOWN(0, 1),

    /** `x - 1`, `y + 1`. */
    DOWN_LEFT(-1, 1),

    /** `x - 1`. */
    LEFT(-1, 0),

    /** `x - 1`, `y - 1`. */
    UP_LEFT(-1, -1);

    /**
     * The direction pointing the opposite way, for example DOWN_LEFT for UP_RIGHT.
     *
     * @sample samples.DirectionSamples.eightDirections
     */
    public val opposite: Direction8 get() = entries[(ordinal + 4) % 8]

    /**
     * Returns the offset of [steps] steps in this direction, so `position + direction * steps` moves that far.
     *
     * @sample samples.DirectionSamples.eightDirections
     */
    public operator fun times(steps: Int): Position = Position(dx * steps, dy * steps)
}

/**
 * Returns the direction this arrow points in: `^` is UP, `>` is RIGHT, `v` is DOWN and `<` is LEFT.
 *
 * @throws IllegalArgumentException if this character is not one of the four arrows; letters such as `U`,
 * `R`, `D`, `L` or `N`, `E`, `S`, `W` are not accepted
 *
 * @sample samples.DirectionSamples.arrowsToDirections
 */
public fun Char.toDirection(): Direction =
    toDirectionOrNull() ?: throw IllegalArgumentException("'$this' is not a direction: expected one of ^ > v <")

/**
 * Returns the direction this arrow points in (`^`, `>`, `v` or `<`), or `null` for any other character.
 *
 * @sample samples.DirectionSamples.arrowsToDirections
 */
public fun Char.toDirectionOrNull(): Direction? = when (this) {
    '^' -> Direction.UP
    '>' -> Direction.RIGHT
    'v' -> Direction.DOWN
    '<' -> Direction.LEFT
    else -> null
}
