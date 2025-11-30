/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.graphs

class SequentialMapper<K, V>(val generator: (Int) -> V) {
    private val cache = mutableMapOf<K, V>()
    operator fun get(key: K) = cache.getOrPut(key) { generator(cache.size) }
}

class Graph {
}
