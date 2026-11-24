/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.search.lowerBound
import kodvent.search.lowerBoundBy
import kodvent.search.upperBound
import kodvent.search.upperBoundBy
import kotlin.test.Test
import kotlin.test.assertEquals

class LowerAndUpperBoundSamples {

    @Test
    fun lowerBoundWithDuplicates() {
        val list = listOf(10, 20, 20, 20, 30, 40)

        assertEquals(1, list.lowerBound(20)) // the first of the equal elements
        assertEquals(4, list.lowerBound(25)) // absent: the index to insert it at
        assertEquals(6, list.lowerBound(50)) // greater than all: the size
        assertEquals(3, list.upperBound(20) - list.lowerBound(20)) // 20 occurs three times
    }

    @Test
    fun upperBoundWithDuplicates() {
        val list = listOf(10, 20, 20, 20, 30, 40)

        assertEquals(4, list.upperBound(20)) // just past the last of the equal elements
        assertEquals(4, list.upperBound(25)) // absent: the same index as lowerBound
        assertEquals(6, list.upperBound(50)) // greater than all: the size
        assertEquals(3, list.upperBound(20) - list.lowerBound(20)) // 20 occurs three times
    }

    @Test
    fun lowerBoundWithComparator() {
        val list = listOf(11, 9, 7, 5, 3, 1)
        val index = list.lowerBound(7, reverseOrder())

        assertEquals(2, index)
        assertEquals(7, list[index])
    }

    @Test
    fun upperBoundWithComparator() {
        val list = listOf(11, 9, 7, 5, 3, 1)
        val index = list.upperBound(7, reverseOrder())

        assertEquals(3, index)
        assertEquals(5, list[index])
    }

    @Test
    fun lowerBoundByWithSelector() {
        data class Person(val name: String, val age: Int)
        val people = listOf(Person("Alice", 25), Person("Bob", 30), Person("Charlie", 35))

        // The first person aged 30 or more
        val index = people.lowerBoundBy(30) { it.age }
        assertEquals("Bob", people[index].name)
    }

    @Test
    fun upperBoundByWithSelector() {
        data class Person(val name: String, val age: Int)
        val people = listOf(Person("Alice", 25), Person("Bob", 30), Person("Charlie", 35))

        // The first person older than 30
        val index = people.upperBoundBy(30) { it.age }
        assertEquals("Charlie", people[index].name)
    }

    @Test
    fun lowerBoundWithComparisonFunction() {
        val list = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
        val index = list.lowerBound { it.compareTo(49) }

        assertEquals(6, index)
        assertEquals(49, list[index])
    }

    @Test
    fun upperBoundWithComparisonFunction() {
        val list = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
        val index = list.upperBound { it.compareTo(49) }

        assertEquals(7, index)
        assertEquals(64, list[index])
    }
}
