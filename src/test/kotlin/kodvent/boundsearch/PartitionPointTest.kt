package kodvent.boundsearch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PartitionPointTest {

    @Test
    fun `partitionPoint Int - empty range returns fromIndex`() {
        val result = partitionPoint(5, 5) { _: Int -> true }
        assertEquals(5, result)
    }

    @Test
    fun `partitionPoint Int - all true returns toIndex`() {
        val result = partitionPoint(0, 10) { _: Int -> true }
        assertEquals(10, result)
    }

    @Test
    fun `partitionPoint Int - all false returns fromIndex`() {
        val result = partitionPoint(0, 10) { _: Int -> false }
        assertEquals(0, result)
    }

    @Test
    fun `partitionPoint Int - partition in middle`() {
        val result = partitionPoint(0, 10) { i: Int -> i < 5 }
        assertEquals(5, result)
    }

    @Test
    fun `partitionPoint Int - search in sorted list`() {
        val list = listOf(1, 3, 5, 7, 9, 11, 13, 15)
        val result = partitionPoint(0, list.size) { list[it] < 10 }
        assertEquals(5, result)
    }

    @Test
    fun `partitionPoint Int - negative indices`() {
        val result = partitionPoint(-10, 10) { i: Int -> i < 0 }
        assertEquals(0, result)
    }

    @Test
    fun `partitionPoint Int - large range`() {
        val target = 1_000_000
        val result = partitionPoint(0, 2_000_000) { i: Int -> i < target }
        assertEquals(target, result)
    }

    @Test
    fun `partitionPoint Int - throws when fromIndex greater than toIndex`() {
        assertFailsWith<IllegalArgumentException> { partitionPoint(10, 5) { _: Int -> true } }
    }

    @Test
    fun `partitionPoint Int - boundary with Int MAX_VALUE`() {
        val fromIndex = Int.MAX_VALUE - 100
        val toIndex = Int.MAX_VALUE
        val result = partitionPoint(fromIndex, toIndex) { it < Int.MAX_VALUE - 50 }
        assertEquals(Int.MAX_VALUE - 50, result)
    }

    @Test
    fun `partitionPoint Int - boundary with Int MIN_VALUE`() {
        val fromIndex = Int.MIN_VALUE
        val toIndex = Int.MIN_VALUE + 100
        val result = partitionPoint(fromIndex, toIndex) { it < Int.MIN_VALUE + 50 }
        assertEquals(Int.MIN_VALUE + 50, result)
    }

    @Test
    fun `partitionPoint Int - three element range - various patterns`() {
        assertEquals(0, partitionPoint(0, 3) { _: Int -> false })
        assertEquals(1, partitionPoint(0, 3) { i: Int -> i < 1 })
        assertEquals(2, partitionPoint(0, 3) { i: Int -> i < 2 })
        assertEquals(3, partitionPoint(0, 3) { _: Int -> true })
    }

    @Test
    fun `partitionPoint Long - empty range returns fromIndex`() {
        val result = partitionPoint(5L, 5L) { true }
        assertEquals(5L, result)
    }

    @Test
    fun `partitionPoint Long - all true returns toIndex`() {
        val result = partitionPoint(0L, 10L) { true }
        assertEquals(10L, result)
    }

    @Test
    fun `partitionPoint Long - all false returns fromIndex`() {
        val result = partitionPoint(0L, 10L) { false }
        assertEquals(0L, result)
    }

    @Test
    fun `partitionPoint Long - partition in middle`() {
        val result = partitionPoint(0L, 10L) { it < 5L }
        assertEquals(5L, result)
    }

    @Test
    fun `partitionPoint Long - search in sorted list`() {
        val list = listOf(1L, 3L, 5L, 7L, 9L, 11L, 13L, 15L)
        val result = partitionPoint(0L, list.size.toLong()) { list[it.toInt()] < 10L }
        assertEquals(5L, result)
    }

    @Test
    fun `partitionPoint Long - negative indices`() {
        val result = partitionPoint(-10L, 10L) { it < 0L }
        assertEquals(0L, result)
    }

    @Test
    fun `partitionPoint Long - large range beyond Int MAX_VALUE`() {
        val target = 5_000_000_000L
        val result = partitionPoint(0L, 10_000_000_000L) { it < target }
        assertEquals(target, result)
    }

    @Test
    fun `partitionPoint Long - very large values`() {
        val fromIndex = Long.MAX_VALUE - 1000L
        val toIndex = Long.MAX_VALUE
        val result = partitionPoint(fromIndex, toIndex) { it < Long.MAX_VALUE - 500L }
        assertEquals(Long.MAX_VALUE - 500L, result)
    }

    @Test
    fun `partitionPoint Long - boundary with Long MIN_VALUE`() {
        val fromIndex = Long.MIN_VALUE
        val toIndex = Long.MIN_VALUE + 100L
        val result = partitionPoint(fromIndex, toIndex) { it < Long.MIN_VALUE + 50L }
        assertEquals(Long.MIN_VALUE + 50L, result)
    }

    @Test
    fun `partitionPoint Long - throws when fromIndex greater than toIndex`() {
        assertFailsWith<IllegalArgumentException> { partitionPoint(10L, 5L) { true } }
    }

    @Test
    fun `partitionPoint Long - three element range - various patterns`() {
        assertEquals(0L, partitionPoint(0L, 3L) { false })
        assertEquals(1L, partitionPoint(0L, 3L) { it < 1L })
        assertEquals(2L, partitionPoint(0L, 3L) { it < 2L })
        assertEquals(3L, partitionPoint(0L, 3L) { true })
    }

    @Test
    fun `partitionPoint Int - complex mathematical condition`() {
        val result = partitionPoint(0, 20) { i: Int -> i * i < 100 }
        assertEquals(10, result)
    }

    @Test
    fun `partitionPoint Long - complex mathematical condition`() {
        val result = partitionPoint(0L, 200_000_000L) { it * it < 10_000_000_000_000_000L }
        assertEquals(100_000_000L, result)
    }

    @Test
    fun `partitionPoint Int - find insertion point in sorted array`() {
        val arr = intArrayOf(1, 3, 3, 3, 5, 7, 9)
        val target = 3
        val lowerBound = partitionPoint(0, arr.size) { arr[it] < target }
        assertEquals(1, lowerBound)

        val upperBound = partitionPoint(0, arr.size) { arr[it] <= target }
        assertEquals(4, upperBound)
    }

    @Test
    fun `partitionPoint Int - binary search for square root`() {
        val target = 625
        val result = partitionPoint(0, target + 1) { it * it <= target }
        assertEquals(26, result)

        val sqrt = result - 1
        assertEquals(25, sqrt)
        assertEquals(625, sqrt * sqrt)
    }

    @Test
    fun `partitionPoint Long - search in large virtual sorted array`() {
        val target = 1_000_000L
        val result = partitionPoint(0L, 1_000_000L) { (it * 2) < target }
        assertEquals(500_000L, result)
    }
}
