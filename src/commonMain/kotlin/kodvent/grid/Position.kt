/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.grid

import kotlin.math.abs

/**
 * A cell position, also used as the offset between two cells. [x] is the column counted from the left and
 * [y] is the input line counted from the top, so y grows downward and `input[y][x]` is the cell at
 * `Position(x, y)`: code written as `a[row][column]` becomes `grid[Position(x = column, y = row)]`.
 *
 * @sample samples.PositionSamples.coordinates
 */
public data class Position(public val x: Int, public val y: Int) {
    /**
     * Returns this position moved by the offset [other]: `Position(x + other.x, y + other.y)`.
     *
     * @sample samples.PositionSamples.arithmetic
     */
    public operator fun plus(other: Position): Position = Position(x + other.x, y + other.y)

    /**
     * Returns the offset that leads from [other] to this position: `Position(x - other.x, y - other.y)`.
     *
     * @sample samples.PositionSamples.arithmetic
     */
    public operator fun minus(other: Position): Position = Position(x - other.x, y - other.y)

    /**
     * Returns this offset taken [factor] times: `Position(x * factor, y * factor)`. The factor goes on the
     * right, so a puzzle's `a + k * (a - b)` is written `a + (a - b) * k`.
     *
     * @sample samples.PositionSamples.arithmetic
     */
    public operator fun times(factor: Int): Position = Position(x * factor, y * factor)

    /**
     * Returns the position one step away in [direction].
     *
     * @sample samples.PositionSamples.orthogonalStep
     */
    public operator fun plus(direction: Direction): Position = Position(x + direction.dx, y + direction.dy)

    /**
     * Returns the position one step back, against [direction], so `p + d - d == p`.
     *
     * @sample samples.PositionSamples.orthogonalStep
     */
    public operator fun minus(direction: Direction): Position = Position(x - direction.dx, y - direction.dy)

    /**
     * Returns the position one step away in [direction], which may be diagonal.
     *
     * @sample samples.PositionSamples.diagonalStep
     */
    public operator fun plus(direction: Direction8): Position = Position(x + direction.dx, y + direction.dy)

    /**
     * Returns the position one step back, against [direction], which may be diagonal.
     *
     * @sample samples.PositionSamples.diagonalStep
     */
    public operator fun minus(direction: Direction8): Position = Position(x - direction.dx, y - direction.dy)

    /**
     * Returns the Manhattan distance to [other]: `|x - other.x| + |y - other.y|`. It is computed in Int like
     * the coordinates, so it overflows for two positions more than `Int.MAX_VALUE` apart.
     *
     * @sample samples.PositionSamples.manhattanDistance
     */
    public fun manhattanDistanceTo(other: Position): Int = abs(x - other.x) + abs(y - other.y)

    /**
     * Returns this position wrapped onto a [width] x [height] torus: `Position(x.mod(width), y.mod(height))`.
     *
     * @throws IllegalArgumentException if [width] or [height] is not positive
     *
     * @sample samples.PositionSamples.wrapOnTorus
     */
    public fun wrap(width: Int, height: Int): Position {
        require(width > 0 && height > 0) { "Cannot wrap onto a $width x $height area: both sizes must be positive" }
        return Position(x.mod(width), y.mod(height))
    }

    /**
     * Returns a hash code that spreads small coordinates of either sign well. The exact value is unspecified.
     */
    override fun hashCode(): Int {
        // The linear value alone never collides for grid-sized coordinates, but it clusters in hash tables
        // that pick a slot by multiplying the hash (the Kotlin HashMap on JS, Wasm and Native). The
        // xorshift-multiply round is a bijection on Int: it breaks the linearity and adds no collisions.
        val linear = x * 1_000_003 + y
        val mixed = (linear xor (linear ushr 15)) * 0x2C1B3C6D
        return mixed xor (mixed ushr 13)
    }
}
