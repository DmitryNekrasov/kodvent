/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * A [Disjoint Set Union](https://en.wikipedia.org/wiki/Disjoint-set_data_structure) (DSU, or Union-Find):
 * maintains a partition of the integers in `[0, size)` into disjoint sets.
 *
 * Path compression and union by rank give near-constant
 * [amortized](https://en.wikipedia.org/wiki/Amortized_analysis) time per operation — O(α(n)), where α is
 * the inverse [Ackermann function](https://en.wikipedia.org/wiki/Ackermann_function). Initially each
 * element is in its own singleton set.
 *
 * @constructor Creates a DSU of [size] singleton elements.
 *
 * @sample samples.DisjointSetUnionSamples.basicUsage
 * @sample samples.DisjointSetUnionSamples.networkConnectivity
 * @sample samples.DisjointSetUnionSamples.detectingCycles
 * @sample samples.DisjointSetUnionSamples.socialNetworkClusters
 * @sample samples.DisjointSetUnionSamples.kruskalMinimumSpanningTree
 */
public class DisjointSetUnion(size: Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }
    private var _count = size

    /**
     * Returns the representative (root) of the set containing [x]. O(α(n)) amortized.
     *
     * @throws IndexOutOfBoundsException if [x] is not in `[0, size)`.
     *
     * @sample samples.DisjointSetUnionSamples.findingRepresentatives
     */
    public fun find(x: Int): Int {
        if (x !in parent.indices) {
            throw IndexOutOfBoundsException("Element ($x) is out of disjoint set bounds: [0, ${parent.size})")
        }
        if (parent[x] != x) {
            parent[x] = find(parent[x])
        }
        return parent[x]
    }

    /**
     * Merges the sets containing [x] and [y]. Returns `true` if they were merged, or `false` if they were
     * already in the same set. O(α(n)) amortized.
     *
     * @throws IndexOutOfBoundsException if [x] or [y] is not in `[0, size)`.
     *
     * @sample samples.DisjointSetUnionSamples.basicUsage
     * @sample samples.DisjointSetUnionSamples.detectingCycles
     */
    @IgnorableReturnValue
    public fun union(x: Int, y: Int): Boolean {
        val rootX = find(x)
        val rootY = find(y)

        // If x and y are already in the same set, no need to merge
        if (rootX == rootY) {
            return false
        }

        // Union by rank: attach the smaller rank tree under the root of the higher rank tree
        when {
            rank[rootX] < rank[rootY] -> parent[rootX] = rootY
            rank[rootX] > rank[rootY] -> parent[rootY] = rootX
            else -> {
                // If ranks are the same, make one the parent and increase its rank
                parent[rootY] = rootX
                rank[rootX]++
            }
        }

        _count--
        return true
    }

    /**
     * Returns `true` if [x] and [y] are in the same set. O(α(n)) amortized.
     *
     * @throws IndexOutOfBoundsException if [x] or [y] is not in `[0, size)`.
     *
     * @sample samples.DisjointSetUnionSamples.networkConnectivity
     */
    public fun connected(x: Int, y: Int): Boolean = find(x) == find(y)

    /**
     * The number of disjoint sets. O(1).
     *
     * @sample samples.DisjointSetUnionSamples.dynamicConnectivityUpdates
     */
    public val count: Int
        get() = _count

    /**
     * Removes [x] from its current set, putting it in its own singleton; the other members of that set
     * stay connected. O(n), since it may rebuild the set.
     *
     * @throws IndexOutOfBoundsException if [x] is not in `[0, size)`.
     *
     * @sample samples.DisjointSetUnionSamples.isolateUsage
     */
    public fun isolate(x: Int) {
        if (x !in parent.indices) {
            throw IndexOutOfBoundsException("Element ($x) is out of disjoint set bounds: [0, ${parent.size})")
        }

        for (i in parent.indices) {
            parent[i] = find(i)
        }

        val oldRoot = parent[x]
        val elementsInSet = parent.indices.filter { parent[it] == oldRoot }
        if (elementsInSet.size == 1) return

        val newRoot = elementsInSet.first { it != x }
        for (i in elementsInSet) {
            if (i != x) {
                parent[i] = newRoot
            }
        }
        rank[newRoot] = 0

        parent[x] = x
        rank[x] = 0
        _count++
    }
}
