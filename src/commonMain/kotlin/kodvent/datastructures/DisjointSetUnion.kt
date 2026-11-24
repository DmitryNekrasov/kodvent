/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * A [Disjoint Set Union](https://en.wikipedia.org/wiki/Disjoint-set_data_structure) (DSU, or Union-Find):
 * maintains a partition of the integers in `0..<size` into disjoint sets.
 *
 * [find], [union] and [connected] take near-constant amortized time, O(α(n)) with the inverse Ackermann
 * function α; [isolate] is O(n).
 *
 * @constructor Creates a DSU of [size] elements, each in its own set.
 * @throws IllegalArgumentException if [size] is negative
 *
 * @sample samples.DisjointSetUnionSamples.basicUsage
 * @sample samples.DisjointSetUnionSamples.kruskalMinimumSpanningTree
 */
public class DisjointSetUnion(size: Int) {
    init {
        require(size >= 0) { "Size must not be negative, but was $size" }
    }

    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }
    private var _count = size

    /**
     * Returns the representative (root) of the set containing [x]. O(α(n)) amortized.
     *
     * @throws IndexOutOfBoundsException if [x] is not in `0..<size`
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
     * @throws IndexOutOfBoundsException if [x] or [y] is not in `0..<size`
     *
     * @sample samples.DisjointSetUnionSamples.basicUsage
     * @sample samples.DisjointSetUnionSamples.kruskalMinimumSpanningTree
     */
    @IgnorableReturnValue
    public fun union(x: Int, y: Int): Boolean {
        val rootX = find(x)
        val rootY = find(y)

        if (rootX == rootY) {
            return false
        }

        when {
            rank[rootX] < rank[rootY] -> parent[rootX] = rootY
            rank[rootX] > rank[rootY] -> parent[rootY] = rootX
            else -> {
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
     * @throws IndexOutOfBoundsException if [x] or [y] is not in `0..<size`
     *
     * @sample samples.DisjointSetUnionSamples.basicUsage
     */
    public fun connected(x: Int, y: Int): Boolean = find(x) == find(y)

    /**
     * The number of disjoint sets. O(1).
     *
     * @sample samples.DisjointSetUnionSamples.basicUsage
     */
    public val count: Int
        get() = _count

    /**
     * Moves [x] into a set of its own. The rest of its old set stays connected. O(n).
     *
     * @throws IndexOutOfBoundsException if [x] is not in `0..<size`
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
