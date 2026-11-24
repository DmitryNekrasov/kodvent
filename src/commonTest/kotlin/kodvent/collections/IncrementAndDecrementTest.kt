/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IncrementAndDecrementTest {

    @Test
    fun `increment counts every key on its own`() {
        val map = mutableMapOf<String, Int>()
        map.increment("key1")
        map.increment("key2")
        map.increment("key1")
        assertEquals(2, map["key1"])
        assertEquals(1, map["key2"])
    }

    @Test
    fun `decrement lowers the count and removes the key when it reaches zero`() {
        val map = mutableMapOf("key1" to 3)
        assertTrue(map.decrement("key1"))
        assertEquals(2, map["key1"])
        assertTrue(map.decrement("key1"))
        assertEquals(1, map["key1"])
        assertTrue(map.decrement("key1"))
        assertNull(map["key1"])
        assertFalse(map.decrement("key1"))
        assertTrue(map.isEmpty())
    }

    @Test
    fun `decrement removes a key whose count is zero or negative`() {
        val map = mutableMapOf("zero" to 0, "negative" to -5)
        assertTrue(map.decrement("zero"))
        assertTrue(map.decrement("negative"))
        assertTrue(map.isEmpty())
    }

    @Test
    fun `decrement can be called without using its result`() {
        val map = mutableMapOf("key1" to 2)
        map.decrement("key1")
        assertEquals(1, map["key1"])
    }
}
