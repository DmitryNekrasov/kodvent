package kodvent.datastructures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class DisjointSetUnionTest {

    @Test
    fun testInitialState() {
        val dsu = DisjointSetUnion(5)

        // Initially, each element should be in its own set
        assertEquals(5, dsu.count())

        // Each element should be its own root
        for (i in 0..<5) {
            assertEquals(i, dsu.find(i))
        }

        // No elements should be connected initially
        assertFalse(dsu.connected(0, 1))
        assertFalse(dsu.connected(1, 2))
        assertFalse(dsu.connected(3, 4))
    }

    @Test
    fun testSingleElementDSU() {
        val dsu = DisjointSetUnion(1)

        assertEquals(1, dsu.count())
        assertEquals(0, dsu.find(0))
        assertTrue(dsu.connected(0, 0))
    }

    @Test
    fun testBasicUnion() {
        val dsu = DisjointSetUnion(5)

        // Union 0 and 1
        assertTrue(dsu.union(0, 1))
        assertEquals(4, dsu.count())
        assertTrue(dsu.connected(0, 1))

        // Union 2 and 3
        assertTrue(dsu.union(2, 3))
        assertEquals(3, dsu.count())
        assertTrue(dsu.connected(2, 3))

        // Verify other elements are still separate
        assertFalse(dsu.connected(0, 2))
        assertFalse(dsu.connected(1, 3))
        assertFalse(dsu.connected(0, 4))
    }

    @Test
    fun testUnionReturnsFalseForSameSet() {
        val dsu = DisjointSetUnion(5)

        // Union 0 and 1
        assertTrue(dsu.union(0, 1))
        assertEquals(4, dsu.count())

        // Try to union them again - should return false
        assertFalse(dsu.union(0, 1))
        assertFalse(dsu.union(1, 0))
        assertEquals(4, dsu.count())
    }

    @Test
    fun testChainedUnions() {
        val dsu = DisjointSetUnion(6)

        // Chain: 0 - 1 - 2
        assertTrue(dsu.union(0, 1))
        assertTrue(dsu.union(1, 2))

        assertEquals(4, dsu.count())
        assertTrue(dsu.connected(0, 1))
        assertTrue(dsu.connected(1, 2))
        assertTrue(dsu.connected(0, 2))

        // Chain: 3 - 4 - 5
        assertTrue(dsu.union(3, 4))
        assertTrue(dsu.union(4, 5))

        assertEquals(2, dsu.count())
        assertTrue(dsu.connected(3, 4))
        assertTrue(dsu.connected(4, 5))
        assertTrue(dsu.connected(3, 5))

        // Two chains should be separate
        assertFalse(dsu.connected(0, 3))
        assertFalse(dsu.connected(2, 5))
    }

    @Test
    fun testMergingLargeSets() {
        val dsu = DisjointSetUnion(10)

        // Create a set {0, 1, 2, 3}
        dsu.union(0, 1)
        dsu.union(1, 2)
        dsu.union(2, 3)

        // Create a set {4, 5, 6, 7}
        dsu.union(4, 5)
        dsu.union(5, 6)
        dsu.union(6, 7)

        assertEquals(4, dsu.count()) // {0,1,2,3}, {4,5,6,7}, {8}, {9}

        // Merge the two large sets
        assertTrue(dsu.union(3, 4))
        assertEquals(3, dsu.count()) // {0,1,2,3,4,5,6,7}, {8}, {9}

        // All elements from both sets should be connected
        assertTrue(dsu.connected(0, 7))
        assertTrue(dsu.connected(3, 4))
        assertTrue(dsu.connected(1, 6))

        // But not connected to 8 or 9
        assertFalse(dsu.connected(0, 8))
        assertFalse(dsu.connected(7, 9))
    }

    @Test
    fun testUnionToSingleSet() {
        val dsu = DisjointSetUnion(5)

        // Union all elements into one set
        dsu.union(0, 1)
        dsu.union(1, 2)
        dsu.union(2, 3)
        dsu.union(3, 4)

        assertEquals(1, dsu.count())

        // All elements should be connected
        for (i in 0..<5) {
            for (j in 0..<5) {
                assertTrue(dsu.connected(i, j))
            }
        }
    }

    @Test
    fun testConnected() {
        val dsu = DisjointSetUnion(5)

        // Element is always connected to itself
        for (i in 0..<5) {
            assertTrue(dsu.connected(i, i))
        }

        dsu.union(0, 1)
        dsu.union(2, 3)

        // Test connected elements
        assertTrue(dsu.connected(0, 1))
        assertTrue(dsu.connected(1, 0))
        assertTrue(dsu.connected(2, 3))
        assertTrue(dsu.connected(3, 2))

        // Test disconnected elements
        assertFalse(dsu.connected(0, 2))
        assertFalse(dsu.connected(1, 3))
        assertFalse(dsu.connected(0, 4))
        assertFalse(dsu.connected(2, 4))
    }

    @Test
    fun testMakeSet() {
        val dsu = DisjointSetUnion(5)

        // Create a set {0, 1, 2}
        dsu.union(0, 1)
        dsu.union(1, 2)

        assertEquals(3, dsu.count())
        assertTrue(dsu.connected(0, 2))

        // Reset element 1
        dsu.makeSet(1)

        assertEquals(4, dsu.count())
        assertFalse(dsu.connected(0, 1))
        assertFalse(dsu.connected(1, 2))

        // 1 should be in its own set
        assertEquals(1, dsu.find(1))
    }

    @Test
    fun testMakeSetOnRoot() {
        val dsu = DisjointSetUnion(5)

        // Create a set {0, 1, 2}
        dsu.union(0, 1)
        dsu.union(1, 2)

        val root = dsu.find(0)
        assertEquals(3, dsu.count())

        // Reset the root element
        dsu.makeSet(root)

        // The count should increase
        assertEquals(4, dsu.count())

        // Original root should be in its own set now
        assertEquals(root, dsu.find(root))
    }

    @Test
    fun testCount() {
        val dsu = DisjointSetUnion(10)

        assertEquals(10, dsu.count())

        dsu.union(0, 1)
        assertEquals(9, dsu.count())

        dsu.union(2, 3)
        assertEquals(8, dsu.count())

        dsu.union(0, 2) // Merges {0, 1} with {2, 3}
        assertEquals(7, dsu.count())

        dsu.union(1, 3) // Already in the same set
        assertEquals(7, dsu.count())
    }

    @Test
    fun testFindWithPathCompression() {
        val dsu = DisjointSetUnion(5)

        // Create a chain: 0 -> 1 -> 2 -> 3 -> 4
        dsu.union(0, 1)
        dsu.union(1, 2)
        dsu.union(2, 3)
        dsu.union(3, 4)

        val root = dsu.find(0)

        // After path compression, all elements should point to the same root
        assertEquals(root, dsu.find(0))
        assertEquals(root, dsu.find(1))
        assertEquals(root, dsu.find(2))
        assertEquals(root, dsu.find(3))
        assertEquals(root, dsu.find(4))
    }

    @Test
    fun testFindOutOfBoundsNegative() {
        val dsu = DisjointSetUnion(5)

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.find(-1)
        }
    }

    @Test
    fun testFindOutOfBoundsTooLarge() {
        val dsu = DisjointSetUnion(5)

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.find(5)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.find(10)
        }
    }

    @Test
    fun testUnionOutOfBounds() {
        val dsu = DisjointSetUnion(5)

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.union(-1, 0)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.union(0, 5)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.union(10, 20)
        }
    }

    @Test
    fun testConnectedOutOfBounds() {
        val dsu = DisjointSetUnion(5)

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.connected(-1, 0)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.connected(0, 5)
        }
    }

    @Test
    fun testMakeSetOutOfBounds() {
        val dsu = DisjointSetUnion(5)

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.makeSet(-1)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            dsu.makeSet(5)
        }
    }

    @Test
    fun testLargeScale() {
        val size = 1000
        val dsu = DisjointSetUnion(size)

        assertEquals(size, dsu.count())

        // Union every even number with 0
        for (i in 0..<size step 2) {
            dsu.union(0, i)
        }

        // Union every odd number with 1
        for (i in 1..<size step 2) {
            dsu.union(1, i)
        }

        assertEquals(2, dsu.count())

        // All even numbers should be connected to 0
        for (i in 0..<size step 2) {
            assertTrue(dsu.connected(0, i))
        }

        // All odd numbers should be connected to 1
        for (i in 1..<size step 2) {
            assertTrue(dsu.connected(1, i))
        }

        // Even and odd should not be connected
        assertFalse(dsu.connected(0, 1))

        // Merge all into one
        dsu.union(0, 1)
        assertEquals(1, dsu.count())

        // Now all should be connected
        assertTrue(dsu.connected(0, 999))
        assertTrue(dsu.connected(500, 501))
    }

    @Test
    fun testComplexScenario() {
        val dsu = DisjointSetUnion(10)

        // Create several components
        dsu.union(0, 1)
        dsu.union(1, 2)  // Component: {0, 1, 2}

        dsu.union(3, 4)
        dsu.union(4, 5)  // Component: {3, 4, 5}

        dsu.union(6, 7)  // Component: {6, 7}

        // Elements 8 and 9 are standalone

        assertEquals(5, dsu.count())

        // Test connections within components
        assertTrue(dsu.connected(0, 2))
        assertTrue(dsu.connected(3, 5))
        assertTrue(dsu.connected(6, 7))

        // Test connections across components
        assertFalse(dsu.connected(0, 3))
        assertFalse(dsu.connected(2, 6))
        assertFalse(dsu.connected(5, 8))

        // Merge some components
        dsu.union(2, 3)  // Merges {0,1,2} and {3,4,5}
        assertEquals(4, dsu.count())
        assertTrue(dsu.connected(0, 5))

        dsu.union(7, 8)  // Merges {6,7} and {8}
        assertEquals(3, dsu.count())
        assertTrue(dsu.connected(6, 8))

        // Reset element 5
        dsu.makeSet(5)
        assertEquals(4, dsu.count())
        assertFalse(dsu.connected(3, 5))
        assertFalse(dsu.connected(0, 5))

        // Re-union 5 back
        dsu.union(5, 4)
        assertEquals(3, dsu.count())
        assertTrue(dsu.connected(0, 5))
    }

    @Test
    fun testEmptyDSU() {
        val dsu = DisjointSetUnion(0)
        assertEquals(0, dsu.count())
    }

    @Test
    fun testStressPathCompression() {
        val dsu = DisjointSetUnion(100)

        // Create a long chain
        for (i in 0..<99) {
            dsu.union(i, i + 1)
        }

        assertEquals(1, dsu.count())

        // Find on the last element should compress the entire path
        val root = dsu.find(99)

        // All elements should now point to the same root
        for (i in 0..<100) {
            assertEquals(root, dsu.find(i))
        }

        // Verify all are still connected
        for (i in 0..<100) {
            for (j in 0..<100) {
                assertTrue(dsu.connected(i, j))
            }
        }
    }
}
