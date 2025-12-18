/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.collections.lowerBound
import kodvent.collections.lowerBoundBy
import kodvent.collections.upperBound
import kodvent.collections.upperBoundBy
import kodvent.math.sqr
import kotlin.test.Test
import kotlin.test.assertEquals

class BoundSearchSamples {

    @Test
    fun lowerBoundWithDuplicates() {
        val list = listOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 12)
        val index = list.lowerBound(9)

        assertEquals(7, index)
        assertEquals(9, list[index])
        assertEquals(7, list[index - 1])
    }

    @Test
    fun upperBoundWithDuplicates() {
        val list = listOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 12)
        val index = list.upperBound(9)

        assertEquals(11, index)
        assertEquals(12, list[index])
        assertEquals(9, list[index - 1])
    }

    @Test
    fun countOccurrencesUsingBounds() {
        // Use lowerBound and upperBound together to count occurrences
        val list = listOf(1, 3, 3, 3, 5, 5, 7, 9, 9, 9, 9, 11)

        val target = 9
        val lower = list.lowerBound(target)
        val upper = list.upperBound(target)
        val count = upper - lower
        assertEquals(4, count)

        val nonExistent = 8
        val lowerNE = list.lowerBound(nonExistent)
        val upperNE = list.upperBound(nonExistent)
        assertEquals(0, upperNE - lowerNE)
    }

    @Test
    fun lowerBoundWithComparator() {
        // Use custom comparator for a reverse-sorted list
        val list = listOf(11, 9, 7, 5, 3, 1)
        val index = list.lowerBound(7, reverseOrder())

        assertEquals(2, index)
        assertEquals(7, list[index])
    }

    @Test
    fun upperBoundWithComparator() {
        // Use custom comparator for a reverse-sorted list
        val list = listOf(11, 9, 7, 5, 3, 1)
        val index = list.upperBound(7, reverseOrder())

        assertEquals(3, index)
        assertEquals(5, list[index])
    }

    @Test
    fun lowerBoundByWithSelector() {
        data class Person(val name: String, val age: Int)

        val people = listOf(
            Person("Alice", 25),
            Person("Bob", 30),
            Person("Charlie", 35),
            Person("Diana", 40),
            Person("Eve", 45)
        )

        // Find the first person with age >= 33
        val index = people.lowerBoundBy(33) { it.age }
        assertEquals(2, index)
        assertEquals("Charlie", people[index].name)
        assertEquals(35, people[index].age)
    }

    @Test
    fun upperBoundByWithSelector() {
        data class Person(val name: String, val age: Int)

        val people = listOf(
            Person("Alice", 25),
            Person("Bob", 30),
            Person("Charlie", 35),
            Person("Diana", 40),
            Person("Eve", 45)
        )

        // Find first person with age > 35
        val index = people.upperBoundBy(35) { it.age }
        assertEquals(3, index)
        assertEquals("Diana", people[index].name)
        assertEquals(40, people[index].age)
    }

    @Test
    fun upperBoundWithNulls() {
        val list = listOf(null, null, 1, 3, 5, 7, 9)

        // Find the first non-null element
        val indexNull = list.upperBound(null)
        assertEquals(2, indexNull)
        assertEquals(1, list[indexNull])
    }

    @Test
    fun findRangeInSublist() {
        // Search only in a subrange of the list
        val list = listOf(31, 23, 15, 7, 9, 11, 13, 15, 17, 19)
        val target = 11

        val lower = list.lowerBound(target, fromIndex = 3, toIndex = 8)
        val upper = list.upperBound(target, fromIndex = 3, toIndex = 8)

        assertEquals(5, lower)
        assertEquals(6, upper)
        assertEquals(11, list[lower])
        assertEquals(13, list[upper])
    }

    @Test
    fun insertionPointMaintainsSortOrder() {
        // Demonstrate inserting elements at the correct position
        val list = mutableListOf(1, 3, 5, 7, 9)

        val insertionIndex = list.lowerBound(6)
        list.add(insertionIndex, 6)

        assertEquals(listOf(1, 3, 5, 6, 7, 9), list)
    }

    @Test
    fun lowerBoundWithComparisonFunction() {
        val list = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)

        // Find the first element whose square root is >= 7
        val index = list.lowerBound { it - 7.sqr() }

        assertEquals(6, index)
        assertEquals(49, list[index])
    }

    @Test
    fun upperBoundWithComparisonFunction() {
        val list = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)

        // Find the first element whose square root is > 7
        val index = list.upperBound { it - 49 }

        assertEquals(7, index)
        assertEquals(64, list[index])
    }
}
