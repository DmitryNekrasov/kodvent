/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LowerAndUpperBoundTest {

    // compareTo returns a difference, not -1, 0 or 1: a bound must look only at the sign.
    private class Rank(val value: Int) : Comparable<Rank> {
        override fun compareTo(other: Rank): Int = value - other.value
    }

    private val ranks = listOf(1, 3, 5, 5, 5, 7, 9).map(::Rank)
    private val five = Rank(5)

    private val lowerBounds: List<(Int, Int) -> Int> = listOf(
        { from, to -> ranks.lowerBound(five, from, to) },
        { from, to -> ranks.lowerBound(five, naturalOrder(), from, to) },
        { from, to -> ranks.lowerBound(from, to) { it.compareTo(five) } },
        { from, to -> ranks.lowerBoundBy(five, from, to) { it } },
    )

    private val upperBounds: List<(Int, Int) -> Int> = listOf(
        { from, to -> ranks.upperBound(five, from, to) },
        { from, to -> ranks.upperBound(five, naturalOrder(), from, to) },
        { from, to -> ranks.upperBound(from, to) { it.compareTo(five) } },
        { from, to -> ranks.upperBoundBy(five, from, to) { it } },
    )

    @Test
    fun `lowerBound and upperBound agree with a linear scan`() {
        val lists = listOf(
            emptyList(),
            listOf(5),
            listOf(5, 5, 5, 5, 5),
            listOf(1, 3, 3, 3, 5, 7, 9),
            List(100) { it / 10 },
        )
        for (list in lists) {
            for (element in -1..11) {
                assertEquals(list.count { it < element }, list.lowerBound(element))
                assertEquals(list.count { it <= element }, list.upperBound(element))
            }
        }
    }

    @Test
    fun `lowerBound finds the place of a String`() {
        val list = listOf("apple", "banana", "cherry", "date")
        assertEquals(1, list.lowerBound("banana"))
        assertEquals(2, list.lowerBound("cat"))
        assertEquals(0, list.lowerBound("aardvark"))
        assertEquals(4, list.lowerBound("zebra"))
    }

    @Test
    fun `upperBound finds the place after a String`() {
        val list = listOf("apple", "banana", "cherry", "date")
        assertEquals(2, list.upperBound("banana"))
        assertEquals(2, list.upperBound("cat"))
        assertEquals(0, list.upperBound("aardvark"))
        assertEquals(4, list.upperBound("zebra"))
    }

    @Test
    fun `lowerBound and upperBound sort null before any value`() {
        val list: List<Int?> = listOf(null, null, 1, 3)
        assertEquals(0, list.lowerBound(null))
        assertEquals(2, list.upperBound(null))
        assertEquals(2, list.lowerBound(1))
        assertEquals(3, list.upperBound(1))
        val allNulls: List<Int?> = listOf(null, null, null)
        assertEquals(3, allNulls.lowerBound(1))
        assertEquals(3, allNulls.upperBound(null))
    }

    @Test
    fun `every overload of lowerBound and upperBound searches only the given range`() {
        // One row per overload, in the order of lowerBounds and upperBounds: a failure shows which overload it is
        assertEquals(List(4) { listOf(2, 3, 1, 6) }, lowerBounds.map { listOf(it(0, 7), it(3, 7), it(0, 1), it(6, 6)) })
        assertEquals(List(4) { listOf(5, 4, 6, 1) }, upperBounds.map { listOf(it(0, 7), it(0, 4), it(6, 7), it(1, 1)) })
        val sortedFrom3Until8 = listOf(31, 23, 15, 7, 9, 11, 13, 15, 3, 1)
        assertEquals(5, sortedFrom3Until8.lowerBound(11, fromIndex = 3, toIndex = 8))
        assertEquals(6, sortedFrom3Until8.upperBound(11, fromIndex = 3, toIndex = 8))
        assertEquals(5, sortedFrom3Until8.lowerBound(11, naturalOrder(), fromIndex = 3, toIndex = 8))
        assertEquals(6, sortedFrom3Until8.upperBound(11, naturalOrder(), fromIndex = 3, toIndex = 8))
        assertEquals(5, sortedFrom3Until8.lowerBound(fromIndex = 3, toIndex = 8) { it.compareTo(11) })
        assertEquals(6, sortedFrom3Until8.upperBound(fromIndex = 3, toIndex = 8) { it.compareTo(11) })
        assertEquals(5, sortedFrom3Until8.lowerBoundBy(11, fromIndex = 3, toIndex = 8) { it })
        assertEquals(6, sortedFrom3Until8.upperBoundBy(11, fromIndex = 3, toIndex = 8) { it })
    }

    @Test
    fun `every overload of lowerBound and upperBound rejects a range outside the list`() {
        val searches = lowerBounds + upperBounds
        fun thrown(from: Int, to: Int) = searches.map { runCatching { it(from, to) }.exceptionOrNull() }

        assertEquals(List(8) { true }, thrown(-1, 7).map { it is IndexOutOfBoundsException })
        assertEquals(List(8) { true }, thrown(0, 8).map { it is IndexOutOfBoundsException })
        assertEquals(List(8) { true }, thrown(4, 2).map { it is IllegalArgumentException })
    }

    @Test
    fun `every overload of lowerBound and upperBound reads a logarithmic number of elements`() {
        var reads = 0
        val list = object : AbstractList<Int>() {
            override val size: Int get() = 1 shl 16

            override fun get(index: Int): Int {
                reads++
                return index
            }
        }

        val found = listOf(
            list.lowerBound(40_000),
            list.lowerBound(40_000, naturalOrder()),
            list.lowerBound { it.compareTo(40_000) },
            list.lowerBoundBy(40_000) { it },
            list.upperBound(40_000),
            list.upperBound(40_000, naturalOrder()),
            list.upperBound { it.compareTo(40_000) },
            list.upperBoundBy(40_000) { it },
        )

        assertEquals(List(4) { 40_000 } + List(4) { 40_001 }, found)
        // A binary search over 2^16 elements reads at most 17 of them
        assertTrue(reads <= 8 * 17)
    }

    @Test
    fun `lowerBound with a comparator stops at the first element the comparator calls equal`() {
        data class Person(val name: String, val age: Int)
        val list = listOf(
            Person("Alice", 25),
            Person("Bob", 30),
            Person("Charlie", 35),
            Person("David", 40)
        )
        val ageComparator = compareBy<Person> { it.age }
        assertEquals(1, list.lowerBound(Person("X", 30), ageComparator))
        assertEquals(2, list.lowerBound(Person("X", 32), ageComparator))
        assertEquals(0, list.lowerBound(Person("X", 20), ageComparator))
        assertEquals(4, list.lowerBound(Person("X", 50), ageComparator))
    }

    @Test
    fun `upperBound with a comparator passes every element the comparator calls equal`() {
        data class Person(val name: String, val age: Int)
        val list = listOf(
            Person("Alice", 25),
            Person("Bob", 30),
            Person("Charlie", 35),
            Person("David", 40)
        )
        val ageComparator = compareBy<Person> { it.age }
        assertEquals(2, list.upperBound(Person("X", 30), ageComparator))
        assertEquals(2, list.upperBound(Person("X", 32), ageComparator))
        assertEquals(0, list.upperBound(Person("X", 20), ageComparator))
        assertEquals(4, list.upperBound(Person("X", 50), ageComparator))
    }

    @Test
    fun `a comparator decides where lowerBound and upperBound sort null`() {
        val list = listOf(5, 1, null)
        val descending = compareByDescending<Int?> { it }
        assertEquals(2, list.lowerBound(null, descending))
        assertEquals(3, list.upperBound(null, descending))
        assertEquals(1, list.lowerBound(3, descending))
        assertEquals(2, list.upperBound(1, descending))
    }

    @Test
    fun `lowerBound and upperBound with a comparison function follow its sign`() {
        val list = listOf(-5, -3, -1, 0, 1, 3, 5)
        assertEquals(3, list.lowerBound { it })
        assertEquals(4, list.upperBound { it })
        assertEquals(7, list.lowerBound { -1 })
        assertEquals(7, list.upperBound { -1 })
        assertEquals(0, list.lowerBound { 1 })
        assertEquals(0, list.upperBound { 1 })
    }

    @Test
    fun `lowerBoundBy finds the place of a key in a list of pairs`() {
        val list = listOf(1 to "a", 3 to "b", 5 to "c", 7 to "d")
        assertEquals(1, list.lowerBoundBy(3) { it.first })
        assertEquals(2, list.lowerBoundBy(4) { it.first })
        assertEquals(0, list.lowerBoundBy(0) { it.first })
        assertEquals(4, list.lowerBoundBy(10) { it.first })
    }

    @Test
    fun `lowerBoundBy sorts a null key first`() {
        data class Item(val id: Int?)
        val list = listOf(Item(null), Item(null), Item(1), Item(3), Item(5))
        assertEquals(0, list.lowerBoundBy(null) { it.id })
        assertEquals(2, list.lowerBoundBy(1) { it.id })
        assertEquals(3, list.lowerBoundBy(2) { it.id })
    }

    @Test
    fun `upperBoundBy finds the place after a key in a list of pairs`() {
        val list = listOf(1 to "a", 3 to "b", 5 to "c", 7 to "d")
        assertEquals(2, list.upperBoundBy(3) { it.first })
        assertEquals(2, list.upperBoundBy(4) { it.first })
        assertEquals(0, list.upperBoundBy(0) { it.first })
        assertEquals(4, list.upperBoundBy(10) { it.first })
    }

    @Test
    fun `upperBoundBy sorts a null key first`() {
        data class Item(val id: Int?)
        val list = listOf(Item(null), Item(null), Item(1), Item(3), Item(5))
        assertEquals(2, list.upperBoundBy(null) { it.id })
        assertEquals(3, list.upperBoundBy(1) { it.id })
        assertEquals(3, list.upperBoundBy(2) { it.id })
    }
}
