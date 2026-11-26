/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.datastructures.DisjointSetUnion
import kodvent.datastructures.Enumerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnumeratorSamples {

    @Test
    fun basicUsage() {
        val id = Enumerator<String>()

        assertEquals(0, id["you"])
        assertEquals(1, id["out"])
        assertEquals(2, id["svr"])
        assertEquals(0, id["you"]) // seen before: the same number
    }

    @Test
    fun namesInDisjointSetUnion() {
        val cables = listOf("kh" to "tc", "qp" to "kh", "de" to "cg")

        // A DisjointSetUnion takes the elements 0..<size, and a cable brings at most 2 new names
        val id = Enumerator<String>()
        val dsu = DisjointSetUnion(2 * cables.size)
        for ((a, b) in cables) dsu.union(id[a], id[b])

        assertTrue(dsu.connected(id["tc"], id["qp"]))
        assertFalse(dsu.connected(id["tc"], id["de"]))
    }
}
