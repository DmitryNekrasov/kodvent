/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.graphs

class SequentialMapper<K, V>(val generator: (Int) -> V) {
    private val cache = mutableMapOf<K, V>()
    operator fun get(key: K) = cache.getOrPut(key) { generator(cache.size) }
}

@JvmInline
public value class Vertex(val id: Int)

class Graph {
}
