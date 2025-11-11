package samples

import kodvent.datastructures.DisjointSetUnion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisjointSetUnionSamples {

    @Test
    fun kruskalMinimumSpanningTree() {
        // Use DSU to implement Kruskal's algorithm for finding Minimum Spanning Tree (MST)
        // Graph with 6 vertices (0-5) and weighted edges
        // edge = listOf(from, to, weight)
        val n = 6

        val edges = listOf(
            listOf(0, 1, 4),
            listOf(0, 2, 2),
            listOf(1, 2, 1),
            listOf(1, 3, 5),
            listOf(2, 3, 8),
            listOf(2, 4, 10),
            listOf(3, 4, 2),
            listOf(3, 5, 6),
            listOf(4, 5, 3)
        )

        // Kruskal's algorithm: sort edges by weight (ascending)
        val sortedEdges = edges.sortedBy { it[2] }

        val dsu = DisjointSetUnion(n)
        val mstEdges = mutableListOf<List<Int>>()
        var totalWeight = 0

        // Process edges in order of increasing weight
        for (edge in sortedEdges) {
            val from = edge[0]
            val to = edge[1]
            val weight = edge[2]

            // Try to add edge to MST
            // If the vertices are not connected, add the edge (no cycle is created)
            if (dsu.union(from, to)) {
                mstEdges.add(edge)
                totalWeight += weight
            }
            // If union returns false, the edge would create a cycle, so skip it
        }

        // MST should have exactly n-1 edges for n vertices
        assertEquals(n - 1, mstEdges.size)

        // Verify the minimum total weight
        // MST edges: (1,2,1), (0,2,2), (3,4,2), (4,5,3), (1,3,5) = total 13
        assertEquals(13, totalWeight)

        // All vertices should be in one connected part
        assertEquals(1, dsu.count())

        // Verify the MST contains the correct edges (sorted by weight)
        val mstWeights = mstEdges.map { it[2] }.sorted()
        assertEquals(listOf(1, 2, 2, 3, 5), mstWeights)
    }

    @Test
    fun basicUsage() {
        // Create a DSU with 5 elements (0, 1, 2, 3, 4)
        val dsu = DisjointSetUnion(5)

        // Initially, each element is in its own set
        assertEquals(5, dsu.count())

        // Union elements 0 and 1
        assertTrue(dsu.union(0, 1))
        assertEquals(4, dsu.count())

        // Union elements 2 and 3
        assertTrue(dsu.union(2, 3))
        assertEquals(3, dsu.count())

        // Elements 0 and 1 are now connected
        assertTrue(dsu.connected(0, 1))

        // Elements 0 and 2 are not connected
        assertFalse(dsu.connected(0, 2))
    }

    @Test
    fun findingRepresentatives() {
        val dsu = DisjointSetUnion(6)

        // Union several elements
        dsu.union(0, 1)
        dsu.union(1, 2)
        dsu.union(3, 4)

        // Find representatives of sets
        val rep012 = dsu.find(0)
        assertEquals(rep012, dsu.find(1))
        assertEquals(rep012, dsu.find(2))

        val rep34 = dsu.find(3)
        assertEquals(rep34, dsu.find(4))

        // Element 5 is in its own set
        assertEquals(5, dsu.find(5))
    }

    @Test
    fun networkConnectivity() {
        // Model a network with 6 computers (0-5)
        val dsu = DisjointSetUnion(6)

        // Connect computers with network cables
        dsu.union(0, 1) // Computer 0 <-> Computer 1
        dsu.union(1, 2) // Computer 1 <-> Computer 2
        dsu.union(3, 4) // Computer 3 <-> Computer 4

        // Check connectivity
        assertTrue(dsu.connected(0, 2)) // 0 and 2 are connected through 1
        assertFalse(dsu.connected(0, 3)) // 0 and 3 are in different networks

        // We have 3 separate networks: {0,1,2}, {3,4}, {5}
        assertEquals(3, dsu.count())

        // Connect the networks
        dsu.union(2, 3) // Bridge the first two networks
        assertTrue(dsu.connected(0, 4)) // Now 0 and 4 are connected
        assertEquals(2, dsu.count())
    }

    @Test
    fun detectingCycles() {
        // Use DSU to detect cycles in a graph
        val dsu = DisjointSetUnion(4)

        // Add edges to the graph
        // Edge 0-1
        val edge1Added = dsu.union(0, 1)
        assertTrue(edge1Added) // Successfully added

        // Edge 1-2
        val edge2Added = dsu.union(1, 2)
        assertTrue(edge2Added) // Successfully added

        // Edge 2-3
        val edge3Added = dsu.union(2, 3)
        assertTrue(edge3Added) // Successfully added

        // Try to add edge 0-3
        // This would create a cycle because 0 and 3 are already connected
        val edge4Added = dsu.union(0, 3)
        assertFalse(edge4Added) // Returns false, indicating they are already in the same set
    }

    @Test
    fun socialNetworkClusters() {
        // Model a social network with 8 people (0-7)
        val dsu = DisjointSetUnion(8)

        // Form friendships
        dsu.union(0, 1) // Person 0 and 1 are friends
        dsu.union(1, 2) // Person 1 and 2 are friends
        dsu.union(3, 4) // Person 3 and 4 are friends
        dsu.union(4, 5) // Person 4 and 5 are friends
        dsu.union(6, 7) // Person 6 and 7 are friends

        // We have 3 friend groups: {0,1,2}, {3,4,5}, {6,7}
        assertEquals(3, dsu.count())

        // Check if people are in the same friend group
        assertTrue(dsu.connected(0, 2)) // In the same group
        assertTrue(dsu.connected(3, 5)) // In the same group
        assertFalse(dsu.connected(0, 3)) // In different groups

        // Two groups merge when members become friends
        dsu.union(2, 3) // Person 2 and 3 become friends
        assertTrue(dsu.connected(0, 5)) // Now in the same group
        assertEquals(2, dsu.count()) // Only 2 groups remain
    }

    @Test
    fun makeSetUsage() {
        val dsu = DisjointSetUnion(5)

        // Create a set {0, 1, 2}
        dsu.union(0, 1)
        dsu.union(1, 2)
        assertEquals(3, dsu.count()) // {0,1,2}, {3}, {4}

        // Verify all are connected
        assertTrue(dsu.connected(0, 1))
        assertTrue(dsu.connected(1, 2))
        assertTrue(dsu.connected(0, 2))

        // Remove element 1 from its set
        dsu.makeSet(1)
        assertEquals(4, dsu.count()) // {0,2}, {1}, {3}, {4}

        // Element 1 is now isolated
        assertFalse(dsu.connected(0, 1))
        assertFalse(dsu.connected(1, 2))

        // But 0 and 2 remain connected
        assertTrue(dsu.connected(0, 2))
    }

    @Test
    fun dynamicConnectivityUpdates() {
        val dsu = DisjointSetUnion(6)

        // Build a network step by step
        assertEquals(6, dsu.count()) // Initially 6 separate components

        dsu.union(0, 1)
        assertEquals(5, dsu.count())

        dsu.union(2, 3)
        assertEquals(4, dsu.count())

        dsu.union(4, 5)
        assertEquals(3, dsu.count())

        // Now merge the components
        dsu.union(1, 2)
        assertEquals(2, dsu.count()) // {0,1,2,3}, {4,5}

        dsu.union(3, 4)
        assertEquals(1, dsu.count()) // All connected: {0,1,2,3,4,5}

        // Verify everything is connected
        assertTrue(dsu.connected(0, 5))
        assertTrue(dsu.connected(1, 4))
        assertTrue(dsu.connected(2, 5))
    }
}
