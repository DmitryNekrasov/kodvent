/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.grid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class GridParsingTest {

    private fun lineBreakMessage(line: Int, name: String, index: Int): String =
        "Line $line contains a line break character ($name) at index $index: split the text with lines()"

    private fun trailingEmptyLineMessage(line: Int, firstLength: Int): String =
        "Line $line is empty, but line 0 has length $firstLength: the text ends with a line break; " +
            "use text.lines().dropLastWhile { it.isEmpty() }"

    private fun wrongLengthMessage(line: Int, length: Int, firstLength: Int): String =
        "Line $line has length $length, but line 0 has length $firstLength: " +
            "all lines of a grid must have the same length"

    private fun assertAllParsersReject(lines: List<CharSequence>, expectedMessage: String) {
        var calls = 0

        val plain = assertFailsWith<IllegalArgumentException> { lines.toGrid() }
        assertEquals(expectedMessage, plain.message)

        val transformed = assertFailsWith<IllegalArgumentException> { lines.toGrid { calls++; it } }
        assertEquals(expectedMessage, transformed.message)

        val mutable = assertFailsWith<IllegalArgumentException> { lines.toMutableGrid() }
        assertEquals(expectedMessage, mutable.message)

        val mutableTransformed = assertFailsWith<IllegalArgumentException> { lines.toMutableGrid { calls++; it } }
        assertEquals(expectedMessage, mutableTransformed.message)

        assertEquals(0, calls)
    }

    @Test
    fun `toGrid puts character x of line y at position x y`() {
        val grid = listOf("abc", "def").toGrid()

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
        assertEquals('a', grid[Position(0, 0)])
        assertEquals('b', grid[Position(1, 0)])
        assertEquals('c', grid[Position(2, 0)])
        assertEquals('d', grid[Position(0, 1)])
        assertEquals('e', grid[Position(1, 1)])
        assertEquals('f', grid[Position(2, 1)])
    }

    @Test
    fun `parsers keep every space as a cell`() {
        val lines = listOf(" a ", "   ")
        val cells = listOf(' ', 'a', ' ', ' ', ' ', ' ')

        assertEquals(cells, lines.toGrid().values)
        assertEquals(cells, lines.toMutableGrid().values)
        assertEquals(cells, lines.toGrid { it }.values)
        assertEquals(cells, lines.toMutableGrid { it }.values)
    }

    @Test
    fun `parsers give a grid without cells for an empty list and for empty lines`() {
        for (lines in listOf(emptyList(), listOf(""), listOf("", "", ""))) {
            var calls = 0
            val grids = listOf(
                lines.toGrid(),
                lines.toMutableGrid(),
                lines.toGrid { calls++; it },
                lines.toMutableGrid { calls++; it },
            )

            assertEquals(List(4) { 0 to lines.size }, grids.map { it.width to it.height })
            assertEquals(0, calls)
        }
    }

    @Test
    fun `toGrid copies the characters so later changes to the lines do not show`() {
        val line = StringBuilder("ab")
        val grid = listOf(line).toGrid()

        assertEquals("abx", line.append('x').toString())
        assertEquals(2, grid.width)
        assertEquals('b', grid[Position(1, 0)])
        assertFalse(Position(2, 0) in grid)
    }

    @Test
    fun `toGrid with a transform holds the transform of every character in reading order`() {
        val seen = mutableListOf<Char>()
        val grid = listOf("123", "456").toGrid { seen += it; it.digitToInt() }

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), grid.values)
        assertEquals(listOf('1', '2', '3', '4', '5', '6'), seen)
    }

    @Test
    fun `toGrid with digitToIntOrNull holds null for a character that is not a digit`() {
        val grid = listOf("1.3", "908").toGrid { it.digitToIntOrNull() }

        assertEquals(listOf(1, null, 3, 9, 0, 8), grid.values)
    }

    @Test
    fun `toMutableGrid puts character x of line y at position x y`() {
        val grid = listOf("abc", "def").toMutableGrid()

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
        assertEquals('b', grid[Position(1, 0)])
        assertEquals('d', grid[Position(0, 1)])
        assertEquals('f', grid[Position(2, 1)])
        assertFalse(Position(3, 0) in grid)
        assertFalse(Position(-1, 1) in grid)
    }

    @Test
    fun `toMutableGrid with a transform holds the transform of every character in reading order`() {
        val seen = mutableListOf<Char>()
        val grid = listOf("12", "34", "56").toMutableGrid { seen += it; it.digitToInt() }

        assertEquals(2, grid.width)
        assertEquals(3, grid.height)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), grid.values)
        assertEquals(listOf('1', '2', '3', '4', '5', '6'), seen)
    }

    @Test
    fun `all parsers reject a line break character naming its line and index`() {
        assertAllParsersReject(listOf("ab\rc", "abcd"), lineBreakMessage(line = 0, name = "\\r", index = 2))
        assertAllParsersReject(listOf("abc", "d\nf"), lineBreakMessage(line = 1, name = "\\n", index = 1))
        assertAllParsersReject(listOf("\nab"), lineBreakMessage(line = 0, name = "\\n", index = 0))
        assertAllParsersReject(listOf("a\r\n"), lineBreakMessage(line = 0, name = "\\r", index = 1))
    }

    @Test
    fun `a line break is reported before a length problem`() {
        // What split on line feeds leaves of a CRLF text: a later line is empty
        assertAllParsersReject("ab\r\ncd\r\n".split("\n"), lineBreakMessage(line = 0, name = "\\r", index = 2))
        // The same line is too short
        assertAllParsersReject(listOf("abc", "a\n"), lineBreakMessage(line = 1, name = "\\n", index = 1))
    }

    @Test
    fun `all parsers reject trailing empty lines with the hint to drop them`() {
        assertAllParsersReject(listOf("ab", "cd", ""), trailingEmptyLineMessage(line = 2, firstLength = 2))
        assertAllParsersReject(listOf("abc", ""), trailingEmptyLineMessage(line = 1, firstLength = 3))
        assertAllParsersReject(listOf("ab", "cd", "", ""), trailingEmptyLineMessage(line = 2, firstLength = 2))
    }

    @Test
    fun `all parsers reject a line of another length with the plain message`() {
        assertAllParsersReject(listOf("abc", "ab", "abc"), wrongLengthMessage(line = 1, length = 2, firstLength = 3))
        assertAllParsersReject(listOf("ab", "ab", "abc"), wrongLengthMessage(line = 2, length = 3, firstLength = 2))
        assertAllParsersReject(listOf("", "a"), wrongLengthMessage(line = 1, length = 1, firstLength = 0))
        assertAllParsersReject(listOf("ab", " "), wrongLengthMessage(line = 1, length = 1, firstLength = 2))
        // An empty line gets the hint only when nothing but empty lines follows it
        assertAllParsersReject(listOf("ab", "", "cd"), wrongLengthMessage(line = 1, length = 0, firstLength = 2))
        assertAllParsersReject(
            listOf("ab", "", "", "cd", ""),
            wrongLengthMessage(line = 1, length = 0, firstLength = 2),
        )
    }

    @Test
    fun `the whole text idiom parses a CRLF text ending with a line break`() {
        val grid = "ab\r\ncd\r\nef\r\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("ab", "cd", "ef").toGrid(), grid)
        assertFalse('\r' in grid.values)
    }

    // Only line 0 can be read: a parser that scanned Int.MAX_VALUE lines of "ab" before it counted the cells
    // would throw the same exception, only much later.
    private val tooManyLines: List<CharSequence> = object : AbstractList<CharSequence>() {
        override val size: Int get() = Int.MAX_VALUE

        override fun get(index: Int): CharSequence = if (index == 0) "ab" else error("line $index was read")
    }

    @Test
    fun `parsers reject lines that would make more cells than an Int can count without reading them all`() {
        val expected = "A 2 x ${Int.MAX_VALUE} grid has too many cells"

        assertEquals(expected, assertFailsWith<IllegalArgumentException> { tooManyLines.toGrid() }.message)
        assertEquals(expected, assertFailsWith<IllegalArgumentException> { tooManyLines.toMutableGrid() }.message)
        assertEquals(
            expected,
            assertFailsWith<IllegalArgumentException> { tooManyLines.toGrid { it.code } }.message,
        )
        assertEquals(
            expected,
            assertFailsWith<IllegalArgumentException> { tooManyLines.toMutableGrid { it.code } }.message,
        )
    }

    @Test
    fun `parsers read lines that are not Strings`() {
        val lines = listOf<CharSequence>(StringBuilder("ab"), "cd")

        assertEquals("ab\ncd", lines.toGrid().toString())
        assertEquals("ab\ncd", lines.toMutableGrid().toString())
        assertEquals(listOf(97, 98, 99, 100), lines.toGrid { it.code }.values)
        assertEquals(listOf(97, 98, 99, 100), lines.toMutableGrid { it.code }.values)
    }

    private fun digitsOrNull(lines: List<String>): Grid<Int>? =
        lines.toGrid { if (it.isDigit()) it.digitToInt() else return null }

    private fun mutableDigitsOrNull(lines: List<String>): MutableGrid<Int>? =
        lines.toMutableGrid { if (it.isDigit()) it.digitToInt() else return null }

    @Test
    fun `parsers with a transform support a non local return from it`() {
        assertEquals(listOf(1, 2, 3, 4), digitsOrNull(listOf("12", "34"))?.values)
        assertNull(digitsOrNull(listOf("12", "3.")))
        assertEquals(listOf(1, 2, 3, 4), mutableDigitsOrNull(listOf("12", "34"))?.values)
        assertNull(mutableDigitsOrNull(listOf("12", "3.")))
    }
}
