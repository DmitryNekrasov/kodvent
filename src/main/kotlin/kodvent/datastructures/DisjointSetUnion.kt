@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

class DisjointSetUnion(size: Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }

    public fun find(x: Int): Int {
        if (x !in parent.indices) {
            throw IndexOutOfBoundsException("Element ($x) is out of disjoint set bounds: [0..${parent.size})")
        }
        if (parent[x] != x) {
            parent[x] = find(parent[x])
        }
        return parent[x]
    }

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

        return true
    }

    public fun connected(x: Int, y: Int): Boolean {
        return find(x) == find(y)
    }

    public fun count(): Int {
        return parent.indices.count { it == parent[it] }
    }

    public fun makeSet(x: Int) {
        if (x !in parent.indices) {
            throw IndexOutOfBoundsException("Element ($x) is out of disjoint set bounds: [0..${parent.size})")
        }
        parent[x] = x
        rank[x] = 0
    }
}
