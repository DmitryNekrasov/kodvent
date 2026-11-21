/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.grid

/**
 * Returns a grid of the characters of these lines, so `lines[y][x]` is at `Position(x, y)`. Nothing is
 * trimmed and a space is a cell like any other. An empty list gives a 0 x 0 grid.
 *
 * The empty line that `lines()` leaves after a final line break counts as a line, so a whole text is
 * `text.lines().dropLastWhile { it.isEmpty() }.toGrid()`.
 *
 * @throws IllegalArgumentException if the lines differ in length, if a line contains a line break
 * character (`\r` or `\n`), or if the lines would make more than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.parseAndRead
 * @sample samples.GridSamples.parseWholeText
 */
public fun List<CharSequence>.toGrid(): Grid<Char> {
    val width = requireGridLines(this)
    return ArrayGrid(width, size, charCells(this, width))
}

/**
 * Returns a grid holding [transform] of every character of these lines, as in
 * `input.toGrid { it.digitToInt() }`. `digitToInt()` throws on any other character, such as `'.'`: for a
 * map that also holds other characters, `toGrid { it.digitToIntOrNull() }` gives a `Grid<Int?>` with `null`
 * in those cells.
 *
 * @throws IllegalArgumentException if the lines differ in length, if a line contains a line break
 * character (`\r` or `\n`), or if the lines would make more than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.parseDigits
 */
public inline fun <T> List<CharSequence>.toGrid(transform: (Char) -> T): Grid<T> = toGrid().map(transform)

/**
 * Returns a mutable grid of the characters of these lines, so `lines[y][x]` is at `Position(x, y)`.
 *
 * @throws IllegalArgumentException if the lines differ in length, if a line contains a line break
 * character (`\r` or `\n`), or if the lines would make more than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.mutableGrid
 */
public fun List<CharSequence>.toMutableGrid(): MutableGrid<Char> {
    val width = requireGridLines(this)
    return MutableArrayGrid(width, size, charCells(this, width))
}

/**
 * Returns a mutable grid holding [transform] of every character of these lines.
 *
 * @throws IllegalArgumentException if the lines differ in length, if a line contains a line break
 * character (`\r` or `\n`), or if the lines would make more than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.parseDigits
 */
public inline fun <T> List<CharSequence>.toMutableGrid(transform: (Char) -> T): MutableGrid<T> {
    val characters = toGrid()
    val width = characters.width
    val height = characters.height
    return mutableGridOfCells(width, height, characters.values.mapTo(ArrayList(width * height), transform))
}

/**
 * Returns a read-only snapshot of this grid that later writes to the original do not affect, safe to use
 * as a map key or a set element as long as the values themselves do not change: the snapshot holds the
 * same value objects. A grid that is not a MutableGrid never changes, so it is returned as it is.
 *
 * @sample samples.GridSamples.equalityAndSnapshots
 */
public fun <T> Grid<T>.toGrid(): Grid<T> =
    if (this is MutableGrid<*>) ArrayGrid(width, height, asArrayGrid().copyCells()) else this

/**
 * Returns a new mutable grid with the same values. Later writes to either grid do not affect the other.
 *
 * @sample samples.GridSamples.copies
 */
public fun <T> Grid<T>.toMutableGrid(): MutableGrid<T> =
    MutableArrayGrid(width, height, asArrayGrid().copyCells())

/**
 * Returns the value at [position], or the result of calling [defaultValue] with it if [position] is
 * outside this grid, like `List.getOrElse`.
 *
 * @sample samples.GridSamples.safeReads
 */
public inline fun <T> Grid<T>.getOrElse(position: Position, defaultValue: (Position) -> T): T =
    if (position in this) this[position] else defaultValue(position)

/**
 * Returns the orthogonal neighbors of [position] that are inside this grid (at most 4), in
 * `Direction.entries` order, as a list computed on access from the size of the grid alone. [position]
 * itself is never included and may be outside. For the neighbors regardless of bounds, write
 * `Direction.entries.map { position + it }`.
 *
 * @sample samples.GridSamples.neighbors
 */
public fun Grid<*>.neighbors(position: Position): List<Position> =
    neighborsOf(this, position, orthogonalDx, orthogonalDy)

/**
 * Returns the neighbors of [position], diagonals included, that are inside this grid (at most 8), in
 * `Direction8.entries` order, as a list computed on access from the size of the grid alone. [position]
 * itself is never included and may be outside.
 *
 * @sample samples.GridSamples.neighbors
 */
public fun Grid<*>.neighbors8(position: Position): List<Position> =
    neighborsOf(this, position, surroundingDx, surroundingDy)

/**
 * Returns the positions `from + direction`, `from + direction * 2`, and so on up to the edge of this
 * grid, nearest first, as a list computed on access from the size of the grid alone. [from] itself is
 * never included and need not be inside: `ray(Position(-1, y), Direction.RIGHT)` is the whole row y, and
 * the list is empty if `from + direction` is outside.
 *
 * @sample samples.GridSamples.rays
 */
public fun Grid<*>.ray(from: Position, direction: Direction): List<Position> =
    rayOf(this, from, direction.dx, direction.dy)

/**
 * Returns the positions `from + direction`, `from + direction * 2`, and so on up to the edge of this
 * grid, where [direction] may be diagonal, with the same rules as the other overload. For any other step,
 * write `generateSequence(from + step) { it + step }.takeWhile { it in grid }`.
 *
 * @sample samples.GridSamples.rays
 */
public fun Grid<*>.ray(from: Position, direction: Direction8): List<Position> =
    rayOf(this, from, direction.dx, direction.dy)

/**
 * Returns the position inside [grid] that this position falls on when the grid repeats endlessly in every
 * direction, so `grid[position.wrap(grid)]` reads the repeated pattern. The same as
 * `wrap(grid.width, grid.height)`.
 *
 * @throws IllegalArgumentException if [grid] is empty
 *
 * @sample samples.GridSamples.wrapOnGrid
 */
public fun Position.wrap(grid: Grid<*>): Position = wrap(grid.width, grid.height)

/**
 * Returns the number of cells whose value matches [predicate], as in `grid.count { it == '#' }`. It is the
 * one shortcut: every other question about values goes through `values`.
 *
 * @sample samples.GridSamples.countCells
 */
public inline fun <T> Grid<T>.count(predicate: (T) -> Boolean): Int = values.count(predicate)

/**
 * Calls [action] with the position and the value of every cell, in reading order.
 *
 * @sample samples.GridSamples.iterateWithPositions
 */
public inline fun <T> Grid<T>.forEachIndexed(action: (position: Position, value: T) -> Unit) {
    for (position in positions) action(position, this[position])
}

/**
 * Returns a new read-only grid of the same size holding [transform] of every value. This grid is not
 * changed.
 *
 * @sample samples.GridSamples.mapCells
 */
public inline fun <T, R> Grid<T>.map(transform: (T) -> R): Grid<R> =
    gridOfCells(width, height, values.mapTo(ArrayList(width * height), transform))

/**
 * Returns a new read-only grid of the same size holding [transform] of every position and its value. This
 * grid is not changed.
 *
 * @sample samples.GridSamples.mapCells
 */
public inline fun <T, R> Grid<T>.mapIndexed(transform: (position: Position, value: T) -> R): Grid<R> =
    gridOfCells(width, height, positions.mapTo(ArrayList(width * height)) { transform(it, this[it]) })

/**
 * Returns a new read-only grid with rows and columns exchanged: the value at `(x, y)` moves to `(y, x)`.
 * This grid is not changed. `transposed().rotatedClockwise()` mirrors left to right and
 * `transposed().rotatedCounterclockwise()` mirrors top to bottom, so with the rotations it reaches all
 * eight orientations.
 *
 * @sample samples.GridSamples.rotateAndTranspose
 */
public fun <T> Grid<T>.transposed(): Grid<T> =
    ArrayGrid(height, width, asArrayGrid().turnedCells(start = 0, stepX = width, stepY = 1))

/**
 * Returns a new read-only grid turned 90 degrees clockwise: the top row becomes the right column, and
 * width and height swap. This grid is not changed.
 *
 * @sample samples.GridSamples.rotateAndTranspose
 */
public fun <T> Grid<T>.rotatedClockwise(): Grid<T> =
    ArrayGrid(height, width, asArrayGrid().turnedCells(start = (height - 1) * width, stepX = -width, stepY = 1))

/**
 * Returns a new read-only grid turned 90 degrees counterclockwise: the right column becomes the top row,
 * and width and height swap. This grid is not changed.
 *
 * @sample samples.GridSamples.rotateAndTranspose
 */
public fun <T> Grid<T>.rotatedCounterclockwise(): Grid<T> =
    ArrayGrid(height, width, asArrayGrid().turnedCells(start = width - 1, stepX = width, stepY = -1))
