/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BoundSearchTest {

    @Test
    fun `lowerBound - empty list returns 0`() {
        val list = emptyList<Int>()
        assertEquals(0, list.lowerBound(5))
    }

    @Test
    fun `lowerBound - single element - element found`() {
        val list = listOf(5)
        assertEquals(0, list.lowerBound(5))
    }

    @Test
    fun `lowerBound - single element - element smaller`() {
        val list = listOf(5)
        assertEquals(0, list.lowerBound(3))
    }

    @Test
    fun `lowerBound - single element - element larger`() {
        val list = listOf(5)
        assertEquals(1, list.lowerBound(7))
    }

    @Test
    fun `lowerBound - finds first occurrence in list with duplicates`() {
        val list = listOf(1, 3, 3, 3, 5, 7, 9)
        assertEquals(1, list.lowerBound(3))
    }

    @Test
    fun `lowerBound - element not in list - smaller than all`() {
        val list = listOf(5, 10, 15, 20)
        assertEquals(0, list.lowerBound(2))
    }

    @Test
    fun `lowerBound - element not in list - larger than all`() {
        val list = listOf(5, 10, 15, 20)
        assertEquals(4, list.lowerBound(25))
    }

    @Test
    fun `lowerBound - element not in list - in between`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(2, list.lowerBound(4))
    }

    @Test
    fun `lowerBound - finds correct position at start`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(0, list.lowerBound(1))
    }

    @Test
    fun `lowerBound - finds correct position at end`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(4, list.lowerBound(5))
    }

    @Test
    fun `lowerBound - all elements equal`() {
        val list = listOf(5, 5, 5, 5, 5)
        assertEquals(0, list.lowerBound(5))
    }

    @Test
    fun `lowerBound - String list`() {
        val list = listOf("apple", "banana", "cherry", "date")
        assertEquals(1, list.lowerBound("banana"))
        assertEquals(2, list.lowerBound("cat"))
        assertEquals(0, list.lowerBound("aardvark"))
        assertEquals(4, list.lowerBound("zebra"))
    }

    @Test
    fun `lowerBound - large list with duplicates`() {
        val list = List(1000) { it / 10 }
        assertEquals(0, list.lowerBound(0))
        assertEquals(10, list.lowerBound(1))
        assertEquals(50, list.lowerBound(5))
        assertEquals(990, list.lowerBound(99))
        assertEquals(1000, list.lowerBound(100))
    }

    @Test
    fun `lowerBound - nullable list with null element`() {
        val list: List<Int?> = listOf(null, null, 1, 3, 5, 7)
        assertEquals(0, list.lowerBound(null))
        assertEquals(2, list.lowerBound(1))
        assertEquals(2, list.lowerBound(0))
    }

    @Test
    fun `lowerBound - nullable list searching for null`() {
        val list: List<Int?> = listOf(1, 3, 5, 7)
        assertEquals(0, list.lowerBound(null))
    }

    @Test
    fun `lowerBound - nullable list all nulls`() {
        val list: List<Int?> = listOf(null, null, null)
        assertEquals(0, list.lowerBound(null))
        assertEquals(3, list.lowerBound(1))
    }

    @Test
    fun `lowerBound - custom range within list`() {
        val list = listOf(1, 3, 5, 7, 9, 11, 13)
        assertEquals(2, list.lowerBound(5, fromIndex = 2, toIndex = 5))
        assertEquals(4, list.lowerBound(8, fromIndex = 2, toIndex = 5))
    }

    @Test
    fun `lowerBound - custom range at start`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(0, list.lowerBound(1, fromIndex = 0, toIndex = 3))
        assertEquals(1, list.lowerBound(3, fromIndex = 0, toIndex = 3))
    }

    @Test
    fun `lowerBound - custom range at end`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(3, list.lowerBound(7, fromIndex = 3, toIndex = 5))
        assertEquals(4, list.lowerBound(9, fromIndex = 3, toIndex = 5))
    }

    @Test
    fun `lowerBound - empty range`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(3, list.lowerBound(5, fromIndex = 3, toIndex = 3))
    }

    @Test
    fun `lowerBound - with reverse comparator`() {
        val list = listOf(9, 7, 5, 3, 1)
        val reverseComparator = compareByDescending<Int> { it }
        assertEquals(2, list.lowerBound(5, reverseComparator))
        assertEquals(1, list.lowerBound(8, reverseComparator))
        assertEquals(5, list.lowerBound(0, reverseComparator))
    }

    @Test
    fun `lowerBound - with custom comparator for strings case-insensitive`() {
        val list = listOf("Apple", "Banana", "Cherry", "Date")
        val caseInsensitiveComparator = String.CASE_INSENSITIVE_ORDER
        assertEquals(1, list.lowerBound("banana", caseInsensitiveComparator))
        assertEquals(0, list.lowerBound("apple", caseInsensitiveComparator))
    }

    @Test
    fun `lowerBound - with comparator for custom objects`() {
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
    fun `lowerBound - with comparison function`() {
        val list = listOf(1, 3, 5, 7, 9)
        val target = 6
        assertEquals(3, list.lowerBound { it.compareTo(target) })
    }

    @Test
    fun `lowerBound - with comparison function all negative`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.lowerBound { -1 })
    }

    @Test
    fun `lowerBound - with comparison function all positive`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(0, list.lowerBound { 1 })
    }

    @Test
    fun `lowerBound - with comparison function mixed signs`() {
        val list = listOf(-5, -3, -1, 0, 1, 3, 5)
        assertEquals(3, list.lowerBound { it.compareTo(0) })
    }

    @Test
    fun `lowerBoundBy - find by key in list of pairs`() {
        val list = listOf(1 to "a", 3 to "b", 5 to "c", 7 to "d")
        assertEquals(1, list.lowerBoundBy(3) { it.first })
        assertEquals(2, list.lowerBoundBy(4) { it.first })
        assertEquals(0, list.lowerBoundBy(0) { it.first })
        assertEquals(4, list.lowerBoundBy(10) { it.first })
    }

    @Test
    fun `lowerBoundBy - find by property in custom objects`() {
        data class Book(val title: String, val year: Int)
        val books = listOf(
            Book("A", 2000),
            Book("B", 2005),
            Book("C", 2010),
            Book("D", 2015)
        )
        assertEquals(1, books.lowerBoundBy(2005) { it.year })
        assertEquals(2, books.lowerBoundBy(2007) { it.year })
        assertEquals(0, books.lowerBoundBy(1990) { it.year })
        assertEquals(4, books.lowerBoundBy(2020) { it.year })
    }

    @Test
    fun `lowerBoundBy - with nullable keys`() {
        data class Item(val id: Int?)
        val list = listOf(Item(null), Item(null), Item(1), Item(3), Item(5))
        assertEquals(0, list.lowerBoundBy(null) { it.id })
        assertEquals(2, list.lowerBoundBy(1) { it.id })
        assertEquals(3, list.lowerBoundBy(2) { it.id })
    }

    @Test
    fun `lowerBoundBy - with string keys`() {
        data class User(val id: Int, val name: String)
        val users = listOf(
            User(1, "Alice"),
            User(2, "Bob"),
            User(3, "Charlie"),
            User(4, "David")
        )
        assertEquals(0, users.lowerBoundBy("Alice") { it.name })
        assertEquals(1, users.lowerBoundBy("Bob") { it.name })
        assertEquals(2, users.lowerBoundBy("Carl") { it.name })
    }

    @Test
    fun `lowerBound - throws IndexOutOfBoundsException when fromIndex less than zero`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IndexOutOfBoundsException> {
            list.lowerBound(3, fromIndex = -1)
        }
    }

    @Test
    fun `lowerBound - throws IndexOutOfBoundsException when toIndex greater than size`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IndexOutOfBoundsException> {
            list.lowerBound(3, toIndex = 6)
        }
    }

    @Test
    fun `lowerBound - throws IllegalArgumentException when fromIndex greater than toIndex`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IllegalArgumentException> {
            list.lowerBound(3, fromIndex = 4, toIndex = 2)
        }
    }

    @Test
    fun `upperBound - empty list returns 0`() {
        val list = emptyList<Int>()
        assertEquals(0, list.upperBound(5))
    }

    @Test
    fun `upperBound - single element - element found`() {
        val list = listOf(5)
        assertEquals(1, list.upperBound(5))
    }

    @Test
    fun `upperBound - single element - element smaller`() {
        val list = listOf(5)
        assertEquals(0, list.upperBound(3))
    }

    @Test
    fun `upperBound - single element - element larger`() {
        val list = listOf(5)
        assertEquals(1, list.upperBound(7))
    }

    @Test
    fun `upperBound - finds position after last occurrence in list with duplicates`() {
        val list = listOf(1, 3, 3, 3, 5, 7, 9)
        assertEquals(4, list.upperBound(3))
    }

    @Test
    fun `upperBound - element not in list - smaller than all`() {
        val list = listOf(5, 10, 15, 20)
        assertEquals(0, list.upperBound(2))
    }

    @Test
    fun `upperBound - element not in list - larger than all`() {
        val list = listOf(5, 10, 15, 20)
        assertEquals(4, list.upperBound(25))
    }

    @Test
    fun `upperBound - element not in list - in between`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(2, list.upperBound(4))
    }

    @Test
    fun `upperBound - finds correct position at start`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(1, list.upperBound(1))
    }

    @Test
    fun `upperBound - finds correct position at end`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.upperBound(5))
    }

    @Test
    fun `upperBound - all elements equal`() {
        val list = listOf(5, 5, 5, 5, 5)
        assertEquals(5, list.upperBound(5))
    }

    @Test
    fun `upperBound - String list`() {
        val list = listOf("apple", "banana", "cherry", "date")
        assertEquals(2, list.upperBound("banana"))
        assertEquals(2, list.upperBound("cat"))
        assertEquals(0, list.upperBound("aardvark"))
        assertEquals(4, list.upperBound("zebra"))
    }

    @Test
    fun `upperBound - large list with duplicates`() {
        val list = List(1000) { it / 10 }
        assertEquals(10, list.upperBound(0))
        assertEquals(20, list.upperBound(1))
        assertEquals(60, list.upperBound(5))
        assertEquals(1000, list.upperBound(99))
        assertEquals(1000, list.upperBound(100))
    }

    @Test
    fun `upperBound - nullable list with null element`() {
        val list: List<Int?> = listOf(null, null, 1, 3, 5, 7)
        assertEquals(2, list.upperBound(null))
        assertEquals(3, list.upperBound(1))
        assertEquals(2, list.upperBound(0))
    }

    @Test
    fun `upperBound - nullable list searching for null`() {
        val list: List<Int?> = listOf(1, 3, 5, 7)
        assertEquals(0, list.upperBound(null))
    }

    @Test
    fun `upperBound - nullable list all nulls`() {
        val list: List<Int?> = listOf(null, null, null)
        assertEquals(3, list.upperBound(null))
        assertEquals(3, list.upperBound(1))
    }

    @Test
    fun `upperBound - custom range within list`() {
        val list = listOf(1, 3, 5, 7, 9, 11, 13)
        assertEquals(3, list.upperBound(5, fromIndex = 2, toIndex = 5))
        assertEquals(4, list.upperBound(8, fromIndex = 2, toIndex = 5))
    }

    @Test
    fun `upperBound - custom range at start`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(1, list.upperBound(1, fromIndex = 0, toIndex = 3))
        assertEquals(2, list.upperBound(3, fromIndex = 0, toIndex = 3))
    }

    @Test
    fun `upperBound - custom range at end`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(4, list.upperBound(7, fromIndex = 3, toIndex = 5))
        assertEquals(5, list.upperBound(9, fromIndex = 3, toIndex = 5))
    }

    @Test
    fun `upperBound - empty range`() {
        val list = listOf(1, 3, 5, 7, 9)
        assertEquals(3, list.upperBound(5, fromIndex = 3, toIndex = 3))
    }

    @Test
    fun `upperBound - with reverse comparator`() {
        val list = listOf(9, 7, 5, 3, 1)
        val reverseComparator = compareByDescending<Int> { it }
        assertEquals(3, list.upperBound(5, reverseComparator))
        assertEquals(1, list.upperBound(8, reverseComparator))
        assertEquals(5, list.upperBound(0, reverseComparator))
    }

    @Test
    fun `upperBound - with custom comparator for strings case-insensitive`() {
        val list = listOf("Apple", "Banana", "Cherry", "Date")
        val caseInsensitiveComparator = String.CASE_INSENSITIVE_ORDER
        assertEquals(2, list.upperBound("banana", caseInsensitiveComparator))
        assertEquals(1, list.upperBound("apple", caseInsensitiveComparator))
    }

    @Test
    fun `upperBound - with comparator for custom objects`() {
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
    fun `upperBound - with comparison function`() {
        val list = listOf(1, 3, 5, 7, 9)
        val target = 6
        assertEquals(3, list.upperBound { it.compareTo(target) })
    }

    @Test
    fun `upperBound - with comparison function all negative`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.upperBound { -1 })
    }

    @Test
    fun `upperBound - with comparison function all positive`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(0, list.upperBound { 1 })
    }

    @Test
    fun `upperBound - with comparison function mixed signs`() {
        val list = listOf(-5, -3, -1, 0, 1, 3, 5)
        assertEquals(4, list.upperBound { it.compareTo(0) })
    }

    @Test
    fun `upperBoundBy - find by key in list of pairs`() {
        val list = listOf(1 to "a", 3 to "b", 5 to "c", 7 to "d")
        assertEquals(2, list.upperBoundBy(3) { it.first })
        assertEquals(2, list.upperBoundBy(4) { it.first })
        assertEquals(0, list.upperBoundBy(0) { it.first })
        assertEquals(4, list.upperBoundBy(10) { it.first })
    }

    @Test
    fun `upperBoundBy - find by property in custom objects`() {
        data class Book(val title: String, val year: Int)
        val books = listOf(
            Book("A", 2000),
            Book("B", 2005),
            Book("C", 2010),
            Book("D", 2015)
        )
        assertEquals(2, books.upperBoundBy(2005) { it.year })
        assertEquals(2, books.upperBoundBy(2007) { it.year })
        assertEquals(0, books.upperBoundBy(1990) { it.year })
        assertEquals(4, books.upperBoundBy(2020) { it.year })
    }

    @Test
    fun `upperBoundBy - with nullable keys`() {
        data class Item(val id: Int?)
        val list = listOf(Item(null), Item(null), Item(1), Item(3), Item(5))
        assertEquals(2, list.upperBoundBy(null) { it.id })
        assertEquals(3, list.upperBoundBy(1) { it.id })
        assertEquals(3, list.upperBoundBy(2) { it.id })
    }

    @Test
    fun `upperBoundBy - with string keys`() {
        data class User(val id: Int, val name: String)
        val users = listOf(
            User(1, "Alice"),
            User(2, "Bob"),
            User(3, "Charlie"),
            User(4, "David")
        )
        assertEquals(1, users.upperBoundBy("Alice") { it.name })
        assertEquals(2, users.upperBoundBy("Bob") { it.name })
        assertEquals(2, users.upperBoundBy("Carl") { it.name })
    }

    @Test
    fun `upperBound - throws IndexOutOfBoundsException when fromIndex less than zero`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IndexOutOfBoundsException> {
            list.upperBound(3, fromIndex = -1)
        }
    }

    @Test
    fun `upperBound - throws IndexOutOfBoundsException when toIndex greater than size`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IndexOutOfBoundsException> {
            list.upperBound(3, toIndex = 6)
        }
    }

    @Test
    fun `upperBound - throws IllegalArgumentException when fromIndex greater than toIndex`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFailsWith<IllegalArgumentException> {
            list.upperBound(3, fromIndex = 4, toIndex = 2)
        }
    }

    @Test
    fun `lowerBound and upperBound - find range of duplicates`() {
        val list = listOf(1, 2, 3, 3, 3, 3, 4, 5, 6)
        val lower = list.lowerBound(3)
        val upper = list.upperBound(3)
        assertEquals(2, lower)
        assertEquals(6, upper)
        assertEquals(4, upper - lower) // count of 3s
    }

    @Test
    fun `lowerBound and upperBound - element not present`() {
        val list = listOf(1, 2, 4, 5, 6)
        val lower = list.lowerBound(3)
        val upper = list.upperBound(3)
        assertEquals(2, lower)
        assertEquals(2, upper)
        assertEquals(0, upper - lower) // element not found
    }

    @Test
    fun `lowerBound and upperBound - all elements are duplicates`() {
        val list = listOf(7, 7, 7, 7, 7, 7, 7)
        val lower = list.lowerBound(7)
        val upper = list.upperBound(7)
        assertEquals(0, lower)
        assertEquals(7, upper)
        assertEquals(7, upper - lower)
    }

    @Test
    fun `lowerBound and upperBound - insertion point for smallest element`() {
        val list = listOf(2, 4, 6, 8, 10)
        val lower = list.lowerBound(1)
        val upper = list.upperBound(1)
        assertEquals(0, lower)
        assertEquals(0, upper)
    }

    @Test
    fun `lowerBound and upperBound - insertion point for largest element`() {
        val list = listOf(2, 4, 6, 8, 10)
        val lower = list.lowerBound(12)
        val upper = list.upperBound(12)
        assertEquals(5, lower)
        assertEquals(5, upper)
    }

    @Test
    fun `lowerBound and upperBound - with negative numbers`() {
        val list = listOf(-10, -5, -5, -3, 0, 3, 5, 5, 10)
        assertEquals(1, list.lowerBound(-5))
        assertEquals(3, list.upperBound(-5))
        assertEquals(4, list.lowerBound(0))
        assertEquals(5, list.upperBound(0))
        assertEquals(6, list.lowerBound(5))
        assertEquals(8, list.upperBound(5))
    }

    @Test
    fun `lowerBound and upperBound - with Double values`() {
        val list = listOf(1.0, 2.5, 2.5, 3.7, 5.0)
        assertEquals(1, list.lowerBound(2.5))
        assertEquals(3, list.upperBound(2.5))
        assertEquals(4, list.lowerBound(4.0))
        assertEquals(4, list.upperBound(4.0))
    }
}
