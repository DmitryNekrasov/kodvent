package kodvent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IncrementAndDecrementTest {

    @Test
    fun `increment should set value to 1 when key does not exist`() {
        val map = mutableMapOf<String, Int>()
        map.increment("key1")
        assertEquals(1, map["key1"])
    }

    @Test
    fun `increment should increase value by 1 when key exists`() {
        val map = mutableMapOf("key1" to 5)
        map.increment("key1")
        assertEquals(6, map["key1"])
    }

    @Test
    fun `increment should handle multiple increments on same key`() {
        val map = mutableMapOf<String, Int>()
        map.increment("key1")
        map.increment("key1")
        map.increment("key1")
        assertEquals(3, map["key1"])
    }

    @Test
    fun `increment should handle multiple different keys`() {
        val map = mutableMapOf<String, Int>()
        map.increment("key1")
        map.increment("key2")
        map.increment("key1")
        assertEquals(2, map["key1"])
        assertEquals(1, map["key2"])
    }

    @Test
    fun `increment should work with different key types`() {
        val intKeyMap = mutableMapOf<Int, Int>()
        intKeyMap.increment(42)
        assertEquals(1, intKeyMap[42])

        val charKeyMap = mutableMapOf<Char, Int>()
        charKeyMap.increment('a')
        assertEquals(1, charKeyMap['a'])
    }

    @Test
    fun `decrement should return false when key does not exist`() {
        val map = mutableMapOf<String, Int>()
        val result = map.decrement("key1")
        assertFalse(result)
        assertEquals(0, map.size)
    }

    @Test
    fun `decrement should decrease value by 1 when value is greater than 1`() {
        val map = mutableMapOf("key1" to 5)
        val result = map.decrement("key1")
        assertTrue(result)
        assertEquals(4, map["key1"])
    }

    @Test
    fun `decrement should remove key when value is 1`() {
        val map = mutableMapOf("key1" to 1)
        val result = map.decrement("key1")
        assertTrue(result)
        assertNull(map["key1"])
        assertFalse(map.containsKey("key1"))
    }

    @Test
    fun `decrement should handle multiple decrements`() {
        val map = mutableMapOf("key1" to 3)
        assertTrue(map.decrement("key1"))
        assertEquals(2, map["key1"])
        assertTrue(map.decrement("key1"))
        assertEquals(1, map["key1"])
        assertTrue(map.decrement("key1"))
        assertNull(map["key1"])
        assertFalse(map.decrement("key1"))
    }

    @Test
    fun `decrement should work with different key types`() {
        val intKeyMap = mutableMapOf(42 to 2)
        assertTrue(intKeyMap.decrement(42))
        assertEquals(1, intKeyMap[42])
    }

    @Test
    fun `increment and decrement should work together`() {
        val map = mutableMapOf<String, Int>()

        map.increment("key1")
        assertEquals(1, map["key1"])

        map.increment("key1")
        assertEquals(2, map["key1"])

        assertTrue(map.decrement("key1"))
        assertEquals(1, map["key1"])

        assertTrue(map.decrement("key1"))
        assertNull(map["key1"])

        assertFalse(map.decrement("key1"))
    }

    @Test
    fun `should handle edge case with value of 0`() {
        val map = mutableMapOf("key1" to 0)
        map.increment("key1")
        assertEquals(1, map["key1"])
    }

    @Test
    fun `should handle operations on different keys`() {
        val map = mutableMapOf<String, Int>()

        map.increment("key1")
        map.increment("key2")
        map.increment("key3")

        assertEquals(1, map["key1"])
        assertEquals(1, map["key2"])
        assertEquals(1, map["key3"])

        assertTrue(map.decrement("key2"))
        assertNull(map["key2"])

        assertEquals(1, map["key1"])
        assertEquals(1, map["key3"])
    }
}
