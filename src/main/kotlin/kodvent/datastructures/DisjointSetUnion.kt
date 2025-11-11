@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * A [Disjoint Set Union](https://en.wikipedia.org/wiki/Disjoint-set_data_structure) (DSU) data structure, also known as Union-Find.
 *
 * This data structure efficiently maintains a partition of a set of elements into disjoint subsets
 * and supports two primary operations: finding which subset an element belongs to (find) and
 * merging two subsets into one (union).
 *
 * The implementation uses two key optimizations:
 * - **Path compression** during find operations: makes elements point directly to the root
 * - **Union by rank**: attaches the smaller tree under the root of the larger tree
 *
 * These optimizations provide near-constant [amortized](https://en.wikipedia.org/wiki/Amortized_analysis) time complexity for both operations.
 *
 * @param size The number of elements in the disjoint set. Elements are represented by integers
 *             in the range [0, size). Initially, each element is in its own set.
 *
 * @constructor Creates a DisjointSetUnion with [size] elements, where each element is initially
 *              in its own singleton set.
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
    private var count = size

    /**
     * Finds the representative (root) of the set containing element [x].
     *
     * This operation uses path compression: during the search, it makes every node on the path
     * point directly to the root, which speeds up future queries.
     *
     * Time complexity: `O(α(n))` amortized, where `α` is the inverse [Ackermann function](https://en.wikipedia.org/wiki/Ackermann_function) (nearly constant).
     *
     * @param x The element whose set representative to find. Must be in range [0, size).
     *
     * @return The representative element of the set containing [x].
     *
     * @throws IndexOutOfBoundsException if [x] is not in the valid range [0, size).
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
     * Merges the sets containing elements [x] and [y].
     *
     * This operation uses union by rank: the tree with a smaller rank is attached under the root
     * of the tree with a larger rank. If ranks are equal, one tree is attached to the other and
     * the rank of the new root is increased.
     *
     * Time complexity: `O(α(n))` amortized, where `α` is the inverse [Ackermann function](https://en.wikipedia.org/wiki/Ackermann_function) (nearly constant).
     *
     * @param x The first element. Must be in range [0, size).
     * @param y The second element. Must be in range [0, size).
     *
     * @return `true` if the sets were merged (they were previously disjoint), `false` if [x] and [y]
     *         were already in the same set.
     *
     * @throws IndexOutOfBoundsException if [x] or [y] is not in the valid range [0, size).
     *
     * @sample samples.DisjointSetUnionSamples.basicUsage
     * @sample samples.DisjointSetUnionSamples.detectingCycles
     */
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

        count--
        return true
    }

    /**
     * Checks if elements [x] and [y] are in the same set.
     *
     * Time complexity: `O(α(n))` amortized, where `α` is the inverse [Ackermann function](https://en.wikipedia.org/wiki/Ackermann_function) (nearly constant).
     *
     * @param x The first element. Must be in range [0, size).
     * @param y The second element. Must be in range [0, size).
     *
     * @return `true` if [x] and [y] are in the same set, `false` otherwise.
     *
     * @throws IndexOutOfBoundsException if [x] or [y] is not in the valid range [0, size).
     *
     * @sample samples.DisjointSetUnionSamples.networkConnectivity
     */
    public fun connected(x: Int, y: Int): Boolean {
        return find(x) == find(y)
    }

    /**
     * Counts the number of disjoint sets.
     *
     * Time complexity: `O(1)`.
     *
     * @return The number of distinct sets currently in the data structure.
     *
     * @sample samples.DisjointSetUnionSamples.dynamicConnectivityUpdates
     */
    public fun count(): Int {
        return count
    }

    /**
     * Resets element [x] to be in its own singleton set.
     *
     * This operation makes [x] the representative of a new set containing only itself,
     * effectively removing it from any set it was previously part of. Other elements
     * that were in the same set remain connected.
     *
     * Time complexity: `O(n)`, where `n` is the size of the disjoint set, as it may need
     *                  to find all elements in the set and reconnect them.
     *
     * @param x The element to reset. Must be in range [0, size).
     *
     * @throws IndexOutOfBoundsException if [x] is not in the valid range [0, size).
     *
     * @sample samples.DisjointSetUnionSamples.makeSetUsage
     */
    public fun makeSet(x: Int) {
        if (x !in parent.indices) {
            throw IndexOutOfBoundsException("Element ($x) is out of disjoint set bounds: [0, ${parent.size})")
        }

        if (parent[x] != x) {
            parent[x] = x
            rank[x] = 0
            count++
            return
        }

        for (i in parent.indices) {
            find(i)
        }
        val elementsInSet = parent.indices.filter { i -> i != x && parent[i] == x }
        if (elementsInSet.isEmpty()) {
            rank[x] = 0
            return
        }

        val root = elementsInSet[0]
        for (i in elementsInSet) {
            parent[i] = root
            rank[i] = 0
        }

        rank[x] = 0
        count++
    }
}
