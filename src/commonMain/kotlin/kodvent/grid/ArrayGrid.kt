/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

/**
 * Everything the library returns as a plain [Grid] is this class and not [MutableArrayGrid], so it cannot be
 * downcast and written.
 */
internal open class ArrayGrid<T>(
    final override val width: Int,
    final override val height: Int,
    protected val cells: MutableList<T>,
) : Grid<T> {
    // Not made on first use: that puts a test and an allocation path into every loop that asks for a view,
    // and scanning a 3 x 3 grid got a quarter slower.
    final override val positions: List<Position> = PositionList(width, height)
    final override val values: List<T> = Line(cells, start = 0, step = 1, size = cells.size)
    final override val rows: List<List<T>> =
        LineList(cells, size = height, lineStride = width, step = 1, lineSize = width)
    final override val columns: List<List<T>> =
        LineList(cells, size = width, lineStride = 1, step = width, lineSize = height)

    final override fun contains(position: Position): Boolean =
        position.x in 0..<width && position.y in 0..<height

    final override fun get(position: Position): T = cells[indexOf(position)]

    final override fun get(x: Int, y: Int): T = cells[indexOf(x, y)]

    final override fun getOrNull(position: Position): T? =
        if (position in this) cells[position.y * width + position.x] else null

    final override fun getOrDefault(position: Position, defaultValue: T): T =
        if (position in this) cells[position.y * width + position.x] else defaultValue

    final override fun positionOf(value: T): Position =
        positionOfOrNull(value) ?: throw NoSuchElementException("No cell holds $value")

    final override fun positionOfOrNull(value: T): Position? {
        // Not cells.indexOf(value): on the JVM and JS that asks the value whether it equals the cell
        for (index in cells.indices) {
            if (cells[index] == value) return positionAt(index)
        }
        return null
    }

    final override fun positionsOf(value: T): List<Position> {
        val result = mutableListOf<Position>()
        if (cells.isEmpty()) return result // a side may still be Int.MAX_VALUE long
        var index = 0
        for (y in 0..<height) {
            for (x in 0..<width) {
                if (cells[index++] == value) result.add(Position(x, y))
            }
        }
        return result
    }

    final override fun equals(other: Any?): Boolean =
        other === this ||
            other is ArrayGrid<*> && width == other.width && height == other.height && cells == other.cells

    final override fun hashCode(): Int = 31 * (31 * width + height) + cells.hashCode()

    final override fun toString(): String {
        val builder = StringBuilder(minOf((width.toLong() + 1) * height, Int.MAX_VALUE.toLong()).toInt())
        for (y in 0..<height) {
            if (y > 0) builder.append('\n')
            val start = y * width
            for (index in start..<start + width) {
                // Not append(Any?) for all: it would make a String of every Char
                when (val value = cells[index]) {
                    is Char -> builder.append(value)
                    else -> builder.append(value.toString())
                }
            }
        }
        return builder.toString()
    }

    internal fun copyCells(): MutableList<T> = ArrayList(cells)

    /** Cell `(x, y)` of the [height] x [width] result is the cell at index `start + x * stepX + y * stepY`. */
    internal fun turnedCells(start: Int, stepX: Int, stepY: Int): MutableList<T> {
        val result = ArrayList<T>(cells.size)
        if (cells.isEmpty()) return result // a side may still be Int.MAX_VALUE long
        for (y in 0..<width) {
            var index = start + y * stepY
            repeat(height) {
                result.add(cells[index])
                index += stepX
            }
        }
        return result
    }

    protected fun indexOf(position: Position): Int {
        if (position !in this) throw IndexOutOfBoundsException("$position is outside the $width x $height grid")
        return position.y * width + position.x
    }

    // Not merged with indexOf(position): reads by position sent through here made a 3 x 3 scan a fifth slower
    protected fun indexOf(x: Int, y: Int): Int =
        if (x in 0..<width && y in 0..<height) y * width + x else indexOf(Position(x, y))

    private fun positionAt(index: Int): Position = Position(index % width, index / width)
}

internal class MutableArrayGrid<T>(width: Int, height: Int, cells: MutableList<T>) :
    ArrayGrid<T>(width, height, cells), MutableGrid<T> {
    override fun set(position: Position, value: T) {
        cells[indexOf(position)] = value
    }

    override fun set(x: Int, y: Int, value: T) {
        cells[indexOf(x, y)] = value
    }
}

internal fun <T> createCells(width: Int, height: Int, init: (Position) -> T): MutableList<T> {
    require(width >= 0 && height >= 0) { "Grid size must not be negative, but was $width x $height" }
    require(width.toLong() * height <= Int.MAX_VALUE) { "A $width x $height grid has too many cells" }
    val cells = ArrayList<T>(width * height)
    if (width == 0) return cells // the height may still be Int.MAX_VALUE
    for (y in 0..<height) {
        for (x in 0..<width) cells.add(init(Position(x, y)))
    }
    return cells
}

internal fun charCells(lines: List<CharSequence>, width: Int): MutableList<Char> {
    val cells = ArrayList<Char>(width * lines.size)
    for (line in lines) {
        for (x in 0..<width) cells.add(line[x])
    }
    return cells
}

// Safe: Grid is sealed, and every implementation extends ArrayGrid
internal fun <T> Grid<T>.asArrayGrid(): ArrayGrid<T> = this as ArrayGrid<T>

internal fun requireGridLines(lines: List<CharSequence>): Int {
    if (lines.isEmpty()) return 0
    val width = lines[0].length
    require(width.toLong() * lines.size <= Int.MAX_VALUE) { "A $width x ${lines.size} grid has too many cells" }
    for ((index, line) in lines.withIndex()) {
        val lineBreak = line.indexOfFirst { it == '\r' || it == '\n' }
        if (lineBreak >= 0) {
            val name = if (line[lineBreak] == '\r') "\\r" else "\\n"
            throw IllegalArgumentException(
                "Line $index contains a line break character ($name) at index $lineBreak: " +
                    "split the text with lines()"
            )
        }
        if (line.length != width) {
            val onlyEmptyLinesFollow = line.isEmpty() && (index..lines.lastIndex).all { lines[it].isEmpty() }
            throw IllegalArgumentException(
                if (onlyEmptyLinesFollow) {
                    "Line $index is empty, but line 0 has length $width: the text ends with a line break; " +
                        "use text.lines().dropLastWhile { it.isEmpty() }"
                } else {
                    "Line $index has length ${line.length}, but line 0 has length $width: " +
                        "all lines of a grid must have the same length"
                }
            )
        }
    }
    return width
}

internal fun rayOf(grid: Grid<*>, from: Position, dx: Int, dy: Int): List<Position> =
    Ray(from, dx, dy, minOf(stepsInside(from.x, dx, grid.width), stepsInside(from.y, dy, grid.height)))

internal fun neighborsOf(grid: Grid<*>, position: Position, dx: IntArray, dy: IntArray): List<Position> {
    val x = position.x
    val y = position.y
    val width = grid.width
    val height = grid.height
    var inside = 0
    if (x in 1..width - 2 && y in 1..height - 2) {
        inside = (1 shl dx.size) - 1
    } else {
        // A step that overflows Int lands on a negative coordinate or on Int.MAX_VALUE, never inside
        for (index in dx.indices) {
            if (x + dx[index] in 0..<width && y + dy[index] in 0..<height) inside = inside or (1 shl index)
        }
    }
    return Neighbors(x, y, dx, dy, inside)
}

internal val orthogonalDx: IntArray = IntArray(Direction.entries.size) { Direction.entries[it].dx }
internal val orthogonalDy: IntArray = IntArray(Direction.entries.size) { Direction.entries[it].dy }
internal val surroundingDx: IntArray = IntArray(Direction8.entries.size) { Direction8.entries[it].dx }
internal val surroundingDy: IntArray = IntArray(Direction8.entries.size) { Direction8.entries[it].dy }

/** [step] is -1, 0 or 1. Each branch is written so that it cannot overflow, whatever the coordinate. */
private fun stepsInside(coordinate: Int, step: Int, size: Int): Int = when (step) {
    0 -> if (coordinate in 0..<size) Int.MAX_VALUE else 0
    1 -> if (coordinate >= -1 && coordinate < size - 1) size - 1 - coordinate else 0
    else -> if (coordinate in 1..size) coordinate else 0
}

private fun checkIndex(index: Int, size: Int) {
    if (index !in 0..<size) throw IndexOutOfBoundsException("Index $index is outside 0..<$size")
}

// Every view below has its own iterator class instead of the one AbstractList shares among all lists: the JVM
// resolves the shared one's call to get() quickly only while it has seen at most two kinds of list there.

private class PositionList(private val width: Int, private val height: Int) : AbstractList<Position>(), RandomAccess {
    override val size: Int get() = width * height

    override fun get(index: Int): Position {
        checkIndex(index, size)
        return Position(index % width, index / width)
    }

    override fun contains(element: Position): Boolean = element.x in 0..<width && element.y in 0..<height

    override fun indexOf(element: Position): Int = if (contains(element)) element.y * width + element.x else -1

    override fun lastIndexOf(element: Position): Int = indexOf(element)

    override fun iterator(): Iterator<Position> = object : Iterator<Position> {
        private var index = 0

        override fun hasNext(): Boolean = index < size

        override fun next(): Position {
            val index = index
            if (index >= size) throw NoSuchElementException()
            this.index = index + 1
            return Position(index % width, index / width)
        }
    }
}

private class Line<T>(
    private val cells: List<T>,
    private val start: Int,
    private val step: Int,
    override val size: Int,
) : AbstractList<T>(), RandomAccess {
    override fun get(index: Int): T {
        checkIndex(index, size)
        return cells[start + index * step]
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var index = start
        private var remaining = size

        override fun hasNext(): Boolean = remaining > 0

        override fun next(): T {
            if (remaining <= 0) throw NoSuchElementException()
            val value = cells[index]
            index += step
            remaining--
            return value
        }
    }
}

private class LineList<T>(
    private val cells: List<T>,
    override val size: Int,
    private val lineStride: Int,
    private val step: Int,
    private val lineSize: Int,
) : AbstractList<List<T>>(), RandomAccess {
    override fun get(index: Int): List<T> {
        checkIndex(index, size)
        return Line(cells, index * lineStride, step, lineSize)
    }

    override fun iterator(): Iterator<List<T>> = object : Iterator<List<T>> {
        private var index = 0

        override fun hasNext(): Boolean = index < size

        override fun next(): List<T> {
            if (index >= size) throw NoSuchElementException()
            return Line(cells, index++ * lineStride, step, lineSize)
        }
    }
}

/** Bit `i` of [inside] says whether the neighbor at offset `(dx[i], dy[i])` is inside the grid. */
private class Neighbors(
    private val x: Int,
    private val y: Int,
    private val dx: IntArray,
    private val dy: IntArray,
    private val inside: Int,
) : AbstractList<Position>(), RandomAccess {
    override val size: Int get() = inside.countOneBits()

    override fun get(index: Int): Position {
        checkIndex(index, size)
        var bits = inside
        repeat(index) { bits = bits and (bits - 1) }
        return at(bits.countTrailingZeroBits())
    }

    override fun iterator(): Iterator<Position> = object : Iterator<Position> {
        private var bits = inside

        override fun hasNext(): Boolean = bits != 0

        override fun next(): Position {
            if (bits == 0) throw NoSuchElementException()
            val offset = bits.countTrailingZeroBits()
            bits = bits and (bits - 1)
            return at(offset)
        }
    }

    private fun at(offset: Int): Position = Position(x + dx[offset], y + dy[offset])
}

private class Ray(
    private val from: Position,
    private val dx: Int,
    private val dy: Int,
    override val size: Int,
) : AbstractList<Position>(), RandomAccess {
    override fun get(index: Int): Position {
        checkIndex(index, size)
        return Position(from.x + dx * (index + 1), from.y + dy * (index + 1))
    }

    override fun iterator(): Iterator<Position> = object : Iterator<Position> {
        private var x = from.x
        private var y = from.y
        private var remaining = size

        override fun hasNext(): Boolean = remaining > 0

        override fun next(): Position {
            if (remaining <= 0) throw NoSuchElementException()
            x += dx
            y += dy
            remaining--
            return Position(x, y)
        }
    }
}
