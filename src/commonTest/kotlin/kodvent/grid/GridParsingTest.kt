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
import kotlin.test.assertTrue

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
        val plain = assertFailsWith<IllegalArgumentException> { lines.toGrid() }
        assertEquals(expectedMessage, plain.message, "toGrid()")

        val transformed = assertFailsWith<IllegalArgumentException> { lines.toGrid { it.code } }
        assertEquals(expectedMessage, transformed.message, "toGrid { }")

        val mutable = assertFailsWith<IllegalArgumentException> { lines.toMutableGrid() }
        assertEquals(expectedMessage, mutable.message, "toMutableGrid()")

        val mutableTransformed = assertFailsWith<IllegalArgumentException> { lines.toMutableGrid { it.code } }
        assertEquals(expectedMessage, mutableTransformed.message, "toMutableGrid { }")
    }

    @Test
    fun `toGrid puts character x of line y at position x y`() {
        val grid = listOf("abc", "def").toGrid()

        assertEquals('a', grid[Position(0, 0)])
        assertEquals('b', grid[Position(1, 0)])
        assertEquals('c', grid[Position(2, 0)])
        assertEquals('d', grid[Position(0, 1)])
        assertEquals('e', grid[Position(1, 1)])
        assertEquals('f', grid[Position(2, 1)])
    }

    @Test
    fun `toGrid makes the width the line length and the height the number of lines`() {
        val grid = listOf("abc", "def").toGrid()

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
    }

    @Test
    fun `toGrid of a tall grid keeps x as the column and y as the line`() {
        val grid = listOf("ab", "cd", "ef", "gh").toGrid()

        assertEquals(2, grid.width)
        assertEquals(4, grid.height)
        assertEquals('b', grid[Position(1, 0)])
        assertEquals('e', grid[Position(0, 2)])
        assertEquals('h', grid[Position(1, 3)])
    }

    @Test
    fun `toGrid treats positions past the lines and negative positions as outside`() {
        val grid = listOf("abc", "def").toGrid()

        assertFalse(Position(3, 0) in grid)
        assertFalse(Position(0, 2) in grid)
        assertFalse(Position(-1, 0) in grid)
        assertFalse(Position(0, -1) in grid)
        assertTrue(Position(2, 1) in grid)
        assertNull(grid.getOrNull(Position(3, 1)))
        assertFailsWith<IndexOutOfBoundsException> { grid[Position(3, 0)] }
        assertFailsWith<IndexOutOfBoundsException> { grid[Position(0, -1)] }
    }

    @Test
    fun `toGrid keeps spaces as cells`() {
        val grid = listOf(" a ", "b  ").toGrid()

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
        assertEquals(' ', grid[Position(0, 0)])
        assertEquals('a', grid[Position(1, 0)])
        assertEquals(' ', grid[Position(2, 0)])
        assertEquals('b', grid[Position(0, 1)])
        assertEquals(' ', grid[Position(1, 1)])
        assertEquals(' ', grid[Position(2, 1)])
    }

    @Test
    fun `toGrid of a line made only of spaces keeps every space`() {
        val grid = listOf("   ").toGrid()

        assertEquals(3, grid.width)
        assertEquals(1, grid.height)
        assertEquals(listOf(' ', ' ', ' '), grid.values)
    }

    @Test
    fun `toGrid does not trim so a trailing space makes a line longer`() {
        assertAllParsersReject(listOf("ab ", "ab"), wrongLengthMessage(line = 1, length = 2, firstLength = 3))
    }

    @Test
    fun `toGrid of an empty list is a 0 by 0 grid`() {
        val grid = emptyList<String>().toGrid()

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
        assertFalse(Position(0, 0) in grid)
        assertEquals("", grid.toString())
    }

    @Test
    fun `toGrid of a single empty line is a 0 by 1 grid`() {
        val grid = listOf("").toGrid()

        assertEquals(0, grid.width)
        assertEquals(1, grid.height)
        assertFalse(Position(0, 0) in grid)
        assertEquals(emptyList(), grid.values)
    }

    @Test
    fun `toGrid of several empty lines is a zero width grid`() {
        val grid = listOf("", "", "").toGrid()

        assertEquals(0, grid.width)
        assertEquals(3, grid.height)
        assertFalse(Position(0, 2) in grid)
    }

    @Test
    fun `toGrid of a single character is a 1 by 1 grid`() {
        val grid = listOf("#").toGrid()

        assertEquals(1, grid.width)
        assertEquals(1, grid.height)
        assertEquals('#', grid[Position(0, 0)])
        assertFalse(Position(1, 0) in grid)
        assertFalse(Position(0, 1) in grid)
    }

    @Test
    fun `toGrid accepts lines of any CharSequence type`() {
        val grid = listOf(StringBuilder("ab"), StringBuilder("cd")).toGrid()

        assertEquals(listOf("ab", "cd").toGrid(), grid)
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
    fun `toGrid result is not a MutableGrid`() {
        val grid = listOf("ab", "cd").toGrid()

        assertFalse(grid is MutableGrid<*>)
    }

    @Test
    fun `toGrid with a transform holds the transform of every character`() {
        val grid = listOf("123", "456").toGrid { it.digitToInt() }

        assertEquals(3, grid.width)
        assertEquals(2, grid.height)
        assertEquals(1, grid[Position(0, 0)])
        assertEquals(3, grid[Position(2, 0)])
        assertEquals(4, grid[Position(0, 1)])
        assertEquals(5, grid[Position(1, 1)])
        assertEquals(6, grid[Position(2, 1)])
    }

    @Test
    fun `toGrid with a transform calls it once per cell in reading order`() {
        val seen = mutableListOf<Char>()
        val grid = listOf("ab", "cd", "ef").toGrid { seen += it; it.uppercaseChar() }

        assertEquals(listOf('a', 'b', 'c', 'd', 'e', 'f'), seen)
        assertEquals('A', grid[Position(0, 0)])
        assertEquals('D', grid[Position(1, 1)])
        assertEquals('E', grid[Position(0, 2)])
    }

    @Test
    fun `toGrid with a transform keeps spaces as cells`() {
        val grid = listOf("a b").toGrid { it == ' ' }

        assertEquals(listOf(false, true, false), grid.values)
    }

    @Test
    fun `toGrid with a transform result is not a MutableGrid`() {
        val grid = listOf("12", "34").toGrid { it.digitToInt() }

        assertFalse(grid is MutableGrid<*>)
    }

    @Test
    fun `toGrid with a transform of an empty list is a 0 by 0 grid without calling the transform`() {
        var calls = 0
        val grid = emptyList<String>().toGrid { calls++; it }

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
        assertEquals(0, calls)
    }

    @Test
    fun `toGrid with a transform of a single empty line is a 0 by 1 grid without calling the transform`() {
        var calls = 0
        val grid = listOf("").toGrid { calls++; it }

        assertEquals(0, grid.width)
        assertEquals(1, grid.height)
        assertEquals(0, calls)
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
    fun `toMutableGrid keeps spaces as cells`() {
        val grid = listOf("  ", "a ").toMutableGrid()

        assertEquals(listOf(' ', ' ', 'a', ' '), grid.values)
    }

    @Test
    fun `toMutableGrid result can be written`() {
        val grid = listOf("ab", "cd").toMutableGrid()

        grid[Position(1, 0)] = 'z'

        assertEquals('z', grid[Position(1, 0)])
        assertEquals("az\ncd", grid.toString())
    }

    @Test
    fun `toMutableGrid of an empty list is a 0 by 0 grid`() {
        val grid = emptyList<String>().toMutableGrid()

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
        assertFailsWith<IndexOutOfBoundsException> { grid[Position(0, 0)] = 'x' }
    }

    @Test
    fun `toMutableGrid of a single empty line is a 0 by 1 grid`() {
        val grid = listOf("").toMutableGrid()

        assertEquals(0, grid.width)
        assertEquals(1, grid.height)
        assertFailsWith<IndexOutOfBoundsException> { grid[Position(0, 0)] = 'x' }
    }

    @Test
    fun `toMutableGrid with a transform holds the transform of every character and can be written`() {
        val grid = listOf("12", "34", "56").toMutableGrid { it.digitToInt() }

        assertEquals(2, grid.width)
        assertEquals(3, grid.height)
        assertEquals(2, grid[Position(1, 0)])
        assertEquals(5, grid[Position(0, 2)])

        grid[Position(0, 2)] += 10

        assertEquals(15, grid[Position(0, 2)])
        assertEquals(listOf(1, 2, 3, 4, 15, 6), grid.values)
    }

    @Test
    fun `toMutableGrid with a transform calls it once per cell in reading order`() {
        val seen = mutableListOf<Char>()
        val grid = listOf("abc", "def").toMutableGrid { seen += it; it.code }

        assertEquals(listOf('a', 'b', 'c', 'd', 'e', 'f'), seen)
        assertEquals('e'.code, grid[Position(1, 1)])
    }

    @Test
    fun `toMutableGrid with a transform of an empty list is a 0 by 0 grid without calling the transform`() {
        var calls = 0
        val grid = emptyList<String>().toMutableGrid { calls++; it }

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
        assertEquals(0, calls)
    }

    @Test
    fun `parsed read only and mutable grids of the same lines are equal`() {
        val lines = listOf("a.", ".b", "c ")
        val readOnly = lines.toGrid()
        val mutable = lines.toMutableGrid()

        assertEquals(readOnly, mutable)
        assertEquals(mutable, readOnly)
        assertEquals(readOnly.hashCode(), mutable.hashCode())
        assertEquals(lines.toGrid { it.code }, lines.toMutableGrid { it.code })
    }

    @Test
    fun `all parsers reject a carriage return naming its line and index`() {
        assertAllParsersReject(listOf("ab\rc", "abcd"), lineBreakMessage(line = 0, name = "\\r", index = 2))
    }

    @Test
    fun `all parsers reject a line feed on a later line naming its line and index`() {
        assertAllParsersReject(listOf("abc", "d\nf"), lineBreakMessage(line = 1, name = "\\n", index = 1))
    }

    @Test
    fun `all parsers reject a single line that holds a line feed`() {
        assertAllParsersReject(listOf("\nab"), lineBreakMessage(line = 0, name = "\\n", index = 0))
    }

    @Test
    fun `a line break on line 0 is reported before a length problem on a later line`() {
        assertAllParsersReject(listOf("ab\r", "x", ""), lineBreakMessage(line = 0, name = "\\r", index = 2))
    }

    @Test
    fun `a line break is reported before the length problem of the same line`() {
        assertAllParsersReject(listOf("abc", "a\n"), lineBreakMessage(line = 1, name = "\\n", index = 1))
    }

    @Test
    fun `a CRLF text split on line feeds only is rejected for the carriage return on line 0`() {
        val lines = "ab\r\ncd\r\n".split("\n")

        assertEquals(listOf("ab\r", "cd\r", ""), lines)
        assertAllParsersReject(lines, lineBreakMessage(line = 0, name = "\\r", index = 2))
    }

    @Test
    fun `all parsers reject a trailing empty line with the hint to drop it`() {
        assertAllParsersReject(listOf("ab", "cd", ""), trailingEmptyLineMessage(line = 2, firstLength = 2))
    }

    @Test
    fun `all parsers reject an empty second and last line with the hint to drop it`() {
        assertAllParsersReject(listOf("abc", ""), trailingEmptyLineMessage(line = 1, firstLength = 3))
    }

    @Test
    fun `several trailing empty lines are reported at the first of them with the hint`() {
        assertAllParsersReject(listOf("ab", "cd", "", ""), trailingEmptyLineMessage(line = 2, firstLength = 2))
    }

    @Test
    fun `the lines of a text ending with a line feed are rejected with the hint`() {
        val lines = "ab\ncd\n".lines()

        assertEquals(listOf("ab", "cd", ""), lines)
        assertAllParsersReject(lines, trailingEmptyLineMessage(line = 2, firstLength = 2))
    }

    @Test
    fun `the lines of a CRLF text ending with a line break are rejected with the hint`() {
        val lines = "ab\r\ncd\r\n".lines()

        assertEquals(listOf("ab", "cd", ""), lines)
        assertAllParsersReject(lines, trailingEmptyLineMessage(line = 2, firstLength = 2))
    }

    @Test
    fun `all parsers reject a shorter line with the plain message`() {
        assertAllParsersReject(listOf("abc", "ab", "abc"), wrongLengthMessage(line = 1, length = 2, firstLength = 3))
    }

    @Test
    fun `all parsers reject a longer last line with the plain message`() {
        assertAllParsersReject(listOf("ab", "ab", "abc"), wrongLengthMessage(line = 2, length = 3, firstLength = 2))
    }

    @Test
    fun `an empty line in the middle gets the plain message`() {
        assertAllParsersReject(listOf("ab", "", "cd"), wrongLengthMessage(line = 1, length = 0, firstLength = 2))
    }

    @Test
    fun `an empty line followed by empty lines and then a non empty line gets the plain message`() {
        assertAllParsersReject(
            listOf("ab", "", "", "cd", ""),
            wrongLengthMessage(line = 1, length = 0, firstLength = 2),
        )
    }

    @Test
    fun `a non empty line after an empty first line gets the plain message`() {
        assertAllParsersReject(listOf("", "a"), wrongLengthMessage(line = 1, length = 1, firstLength = 0))
    }

    @Test
    fun `the transform of toGrid is never called on rejected input`() {
        val rejected = listOf(
            listOf("ab", "c\r"),
            listOf("ab", "cd", ""),
            listOf("ab", "cd", "e"),
            listOf("ab", "", "cd"),
        )
        for (lines in rejected) {
            var calls = 0
            assertFailsWith<IllegalArgumentException> { lines.toGrid { calls++; it } }
            assertEquals(0, calls, "transform called for $lines")
        }
    }

    @Test
    fun `the transform of toMutableGrid is never called on rejected input`() {
        val rejected = listOf(
            listOf("ab", "c\n"),
            listOf("ab", "cd", ""),
            listOf("ab", "cd", "e"),
            listOf("ab", "", "cd"),
        )
        for (lines in rejected) {
            var calls = 0
            assertFailsWith<IllegalArgumentException> { lines.toMutableGrid { calls++; it } }
            assertEquals(0, calls, "transform called for $lines")
        }
    }

    @Test
    fun `the whole text idiom parses a text ending with a line feed`() {
        val grid = "ab\ncd\nef\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("ab", "cd", "ef").toGrid(), grid)
        assertEquals(2, grid.width)
        assertEquals(3, grid.height)
    }

    @Test
    fun `the whole text idiom parses a CRLF text ending with a line break`() {
        val grid = "ab\r\ncd\r\nef\r\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("ab", "cd", "ef").toGrid(), grid)
        assertFalse('\r' in grid.values)
    }

    @Test
    fun `the whole text idiom parses a text ending with two line breaks`() {
        val grid = "ab\ncd\n\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("ab", "cd").toGrid(), grid)
    }

    @Test
    fun `the whole text idiom parses a CRLF text ending with two line breaks`() {
        val grid = "ab\r\ncd\r\n\r\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("ab", "cd").toGrid(), grid)
    }

    @Test
    fun `the whole text idiom parses a text without a final line break`() {
        val grid = "abc\ndef".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(listOf("abc", "def").toGrid(), grid)
    }

    @Test
    fun `the whole text idiom works with the mutable and transforming parsers`() {
        val lines = "12\r\n34\r\n".lines().dropLastWhile { it.isEmpty() }

        assertEquals(listOf("12", "34").toMutableGrid(), lines.toMutableGrid())
        assertEquals(listOf(1, 2, 3, 4), lines.toGrid { it.digitToInt() }.values)
        assertEquals(listOf(1, 2, 3, 4), lines.toMutableGrid { it.digitToInt() }.values)
    }

    @Test
    fun `the whole text idiom turns an empty text into a 0 by 0 grid`() {
        val grid = "".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(0, grid.width)
        assertEquals(0, grid.height)
    }

    @Test
    fun `a last line of spaces stays a row`() {
        val grid = "ab\n  \n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(2, grid.width)
        assertEquals(2, grid.height)
        assertEquals(' ', grid[Position(0, 1)])
        assertEquals(' ', grid[Position(1, 1)])
        assertEquals(listOf("ab", "  ").toGrid(), grid)
    }

    @Test
    fun `a last line of spaces stays a row in a CRLF text`() {
        val grid = "ab\r\n  \r\n".lines().dropLastWhile { it.isEmpty() }.toGrid()

        assertEquals(2, grid.height)
        assertEquals(listOf("ab", "  ").toGrid(), grid)
    }

    @Test
    fun `toString of a parsed grid gives back its lines joined by line feeds`() {
        assertEquals("ab\ncd\nef", listOf("ab", "cd", "ef").toGrid().toString())
        assertEquals(" a \n   ", listOf(" a ", "   ").toGrid().toString())
    }

    @Test
    fun `toString lines toGrid gives back a parsed grid of any positive height`() {
        val grids = listOf(
            listOf("#").toGrid(),
            listOf("ab", "cd").toGrid(),
            listOf("abc", "def").toGrid(),
            listOf("ab", "cd", "ef", "gh").toGrid(),
            listOf(" a ", "   ").toGrid(),
            listOf("#..#", ".##.", "....").toGrid(),
            listOf("").toGrid(),
            listOf("", "", "").toGrid(),
        )
        for (grid in grids) {
            assertEquals(grid, grid.toString().lines().toGrid(), "round trip of\n$grid")
        }
    }

    @Test
    fun `toString lines toMutableGrid gives back a parsed mutable grid`() {
        val grid = listOf("a b", "c d").toMutableGrid()
        grid[Position(1, 1)] = 'x'

        val reparsed = grid.toString().lines().toMutableGrid()

        assertEquals(grid, reparsed)
        assertEquals('x', reparsed[Position(1, 1)])
    }

    // Int.MAX_VALUE lines of two characters each, without holding them in memory.
    private val tooManyLines: List<CharSequence> = object : AbstractList<CharSequence>() {
        override val size: Int get() = Int.MAX_VALUE

        override fun get(index: Int): CharSequence = "ab"
    }

    @Test
    fun `parsers reject lines that would make more than Int MAX_VALUE cells without reading them all`() {
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
