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
        val ids = Enumerator<String>()

        assertEquals(0, ids["ann"])
        assertEquals(1, ids["bob"])
        assertEquals(0, ids["ann"]) // seen before: the same number
        assertEquals(2, ids["eve"])
        assertEquals(3, ids.size)
    }

    @Test
    fun namesInDisjointSetUnion() {
        val friends = listOf("ann" to "bob", "eve" to "dan", "bob" to "kim")

        val ids = Enumerator<String>()
        val pairs = friends.map { (a, b) -> ids[a] to ids[b] }

        val groups = DisjointSetUnion(ids.size)
        for ((a, b) in pairs) groups.union(a, b)

        assertEquals(2, groups.count) // {ann, bob, kim}, {eve, dan}
        assertTrue(groups.connected(ids["ann"], ids["kim"]))
        assertFalse(groups.connected(ids["ann"], ids["eve"]))
    }
}
