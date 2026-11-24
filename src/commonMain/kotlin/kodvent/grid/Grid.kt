/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.grid

/**
 * A read-only rectangular grid of [width] x [height] cells addressed by [Position], where row y holds
 * input line y. It relates to [MutableGrid] as List does to MutableList, and a grid that is not a
 * MutableGrid never changes: no cell ever gets another value. Like a List, it does not freeze a value that
 * is a mutable object itself.
 *
 * A cell is read as `grid[position]` or, with the two numbers at hand, as `grid[x, y]`; every other
 * function takes a [Position].
 *
 * A grid is not a collection: `position in grid` tests the bounds, and questions about values go through
 * [values]. Every list of positions a grid hands out ([positions], [positionsOf], `neighbors`,
 * `neighbors8`, `ray`) stays the same when cells are written, while [values], [rows] and [columns] are
 * live views.
 *
 * @sample samples.GridSamples.parseAndRead
 * @sample samples.GridSamples.countCrowdedCells
 * @sample samples.GridSamples.walkUntilLeavingGrid
 */
public sealed interface Grid<out T> {
    /**
     * The number of columns; the valid x are `0..<width`.
     *
     * @sample samples.GridSamples.parseAndRead
     */
    public val width: Int

    /**
     * The number of rows (input lines); the valid y are `0..<height`.
     *
     * @sample samples.GridSamples.parseAndRead
     */
    public val height: Int

    /**
     * All positions in reading order: row by row from the top, each row from left to right. The list is
     * computed on access, so obtaining it is O(1), and so are its `contains` and `indexOf`.
     *
     * @sample samples.GridSamples.positionsInReadingOrder
     */
    public val positions: List<Position>

    /**
     * All values in reading order, as a read-only live view. Questions about values go through it:
     * `'#' in grid.values`, `grid.values.any { }`, `grid.values.max()`.
     *
     * @sample samples.GridSamples.valuesView
     */
    public val values: List<T>

    /**
     * The rows from top to bottom, each from left to right, as read-only live views, so `rows[y][x]` is the
     * value at `Position(x, y)`. Each row is a List with List equality and hashCode, and in a `Grid<Char>`
     * `rows[y].joinToString("")` is input line y.
     *
     * @sample samples.GridSamples.rowsAndColumns
     */
    public val rows: List<List<T>>

    /**
     * The columns from left to right, each from top to bottom, as read-only live views, so `columns[x][y]`
     * is the value at `Position(x, y)`: x comes first here. Each column is a List with List equality and
     * hashCode, like a row.
     *
     * @sample samples.GridSamples.rowsAndColumns
     */
    public val columns: List<List<T>>

    /**
     * Returns the value at [position].
     *
     * @throws IndexOutOfBoundsException if [position] is outside this grid
     *
     * @sample samples.GridSamples.parseAndRead
     */
    public operator fun get(position: Position): T

    /**
     * Returns the value at column [x] of row [y], the same as `grid[Position(x, y)]`. x comes first, as in
     * [Position], so code written as `a[row][column]` becomes `grid[column, row]`.
     *
     * @throws IndexOutOfBoundsException if the cell is outside this grid
     *
     * @sample samples.GridSamples.indexByCoordinates
     */
    public operator fun get(x: Int, y: Int): T

    /**
     * Returns the value at [position], or `null` if [position] is outside this grid. In a `Grid<T?>` a
     * null cell looks the same, so test `position in grid` there.
     *
     * @sample samples.GridSamples.safeReads
     */
    public fun getOrNull(position: Position): T?

    /**
     * Returns the value at [position], or [defaultValue] if [position] is outside this grid, as in
     * `grid.getOrDefault(position, '.')`. A null cell of a `Grid<T?>` stays null.
     *
     * @sample samples.GridSamples.safeReads
     */
    public fun getOrDefault(position: Position, defaultValue: @UnsafeVariance T): T

    /**
     * Returns `true` if [position] is inside this grid, so `position in grid` is the bounds test. It never
     * looks at values.
     *
     * @sample samples.GridSamples.boundsCheck
     */
    public operator fun contains(position: Position): Boolean

    /**
     * Returns the first position in reading order that holds [value].
     *
     * @throws NoSuchElementException if no cell holds [value]
     *
     * @sample samples.GridSamples.findPositions
     */
    public fun positionOf(value: @UnsafeVariance T): Position

    /**
     * Returns the first position in reading order that holds [value], or `null` if there is none.
     *
     * @sample samples.GridSamples.findPositions
     */
    public fun positionOfOrNull(value: @UnsafeVariance T): Position?

    /**
     * Returns every position that holds [value], in reading order, as a new list that later writes to the
     * grid do not change. It scans every cell on each call: for a test that can stop at the first match,
     * write `positions.any { grid[it] == value && ... }`, which builds no list.
     *
     * @sample samples.GridSamples.findPositions
     */
    public fun positionsOf(value: @UnsafeVariance T): List<Position>

    /**
     * Returns `true` if [other] is a grid, read-only or mutable, with the same width, the same height and
     * equal values at every position.
     *
     * @sample samples.GridSamples.equalityAndSnapshots
     */
    override fun equals(other: Any?): Boolean

    /**
     * Returns a hash code consistent with [equals], computed from the size and every value. Take a
     * `toGrid()` snapshot before using a MutableGrid as a map key.
     *
     * @sample samples.GridSamples.equalityAndSnapshots
     */
    override fun hashCode(): Int

    /**
     * Returns the rows joined by `\n`, each row being its values written one after another: a Char as it is
     * and any other value, null included, as its `toString()`. So a `Grid<Char>` prints as the puzzle
     * input. There is no line break after the last row.
     *
     * @sample samples.GridSamples.printing
     */
    override fun toString(): String
}

/**
 * A [Grid] whose cells can be overwritten. Its size never changes, and its views show later writes.
 *
 * @sample samples.GridSamples.mutableGrid
 */
public sealed interface MutableGrid<T> : Grid<T> {
    /**
     * Sets the cell at [position] to [value], which also makes `grid[position] += 1` and
     * `grid[position]++` work.
     *
     * @throws IndexOutOfBoundsException if [position] is outside this grid
     *
     * @sample samples.GridSamples.mutableGrid
     */
    public operator fun set(position: Position, value: T)

    /**
     * Sets the cell at column [x] of row [y] to [value], the same as `grid[Position(x, y)] = value`, so
     * `grid[x, y] += 1` works too.
     *
     * @throws IndexOutOfBoundsException if the cell is outside this grid
     *
     * @sample samples.GridSamples.indexByCoordinates
     */
    public operator fun set(x: Int, y: Int, value: T)
}

/**
 * Creates a read-only grid of [width] x [height] cells, calling [init] once for every position in reading
 * order, like `List(size) { }`.
 *
 * @throws IllegalArgumentException if [width] or [height] is negative, or if the grid would have more
 * than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.createBySize
 */
@Suppress("FunctionName")
public fun <T> Grid(width: Int, height: Int, init: (position: Position) -> T): Grid<T> =
    ArrayGrid(width, height, createCells(width, height, init))

/**
 * Creates a mutable grid of [width] x [height] cells, calling [init] once for every position in reading
 * order, as in `MutableGrid(71, 71) { '.' }`.
 *
 * @throws IllegalArgumentException if [width] or [height] is negative, or if the grid would have more
 * than `Int.MAX_VALUE` cells
 *
 * @sample samples.GridSamples.createBySize
 */
@Suppress("FunctionName")
public fun <T> MutableGrid(width: Int, height: Int, init: (position: Position) -> T): MutableGrid<T> =
    MutableArrayGrid(width, height, createCells(width, height, init))

// The two functions below are called from the public inline functions, so the calls are compiled into user
// code: their names, their signatures and the JVM class of this file, GridKt, are binary interface.

@PublishedApi
internal fun <T> gridOfCells(width: Int, height: Int, cells: MutableList<T>): Grid<T> = ArrayGrid(width, height, cells)

@PublishedApi
internal fun <T> mutableGridOfCells(width: Int, height: Int, cells: MutableList<T>): MutableGrid<T> =
    MutableArrayGrid(width, height, cells)
