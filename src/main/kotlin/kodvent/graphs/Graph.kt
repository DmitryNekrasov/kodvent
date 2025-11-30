/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.graphs

class SequentialMapper<K, V>(val generator: (Int) -> V) {
    private val cache = mutableMapOf<K, V>()
    operator fun get(key: K) = cache.getOrPut(key) { generator(cache.size) }
    operator fun contains(key: K) = key in cache
}

@JvmInline
public value class Vertex(val id: Int)

public class Graph<T> {
    private val vertexMap = SequentialMapper<T, Vertex>(::Vertex)

    private val graph = mutableListOf<MutableList<Vertex>>()

    public val size get() = graph.size

    public fun addEdge(from: T, to: T) {
        val vertexFrom = addVertex(from)
        val vertexTo = addVertex(to)
        graph[vertexFrom.id].add(vertexTo)
    }

    public fun getVertex(v: T): Vertex {
        require(v in vertexMap) { "Vertex $v not found" }
        return vertexMap[v]
    }

    private fun addVertex(v: T): Vertex {
        val vertex = vertexMap[v]
        if (graph.size <= vertex.id) {
            graph.add(mutableListOf())
        }
        return vertex
    }
}
