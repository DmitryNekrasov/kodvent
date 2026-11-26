/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.Enumerator
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumeratorSamples {

    @Test
    fun basicUsage() {
        val ids = Enumerator<String>()

        assertEquals(0, ids["you"])
        assertEquals(1, ids["out"])
        assertEquals(0, ids["you"]) // the same number as before
        assertEquals(2, ids["svr"])

        assertEquals(3, ids.size)
    }

    @Test
    fun namedVertices() {
        val wires = listOf("you" to "bbb", "you" to "ccc", "bbb" to "out", "ccc" to "out")

        val ids = Enumerator<String>()
        val edges = wires.map { (from, to) -> ids[from] to ids[to] }
        val graph = List(ids.size) { mutableListOf<Int>() }
        for ((from, to) in edges) graph[from] += to

        // you is 0, bbb is 1, ccc is 2 and out is 3
        assertEquals(listOf(listOf(1, 2), listOf(3), listOf(3), emptyList()), graph)
    }
}
