/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GcdLcmTest {

    @Test
    fun `gcd should return correct result for basic positive integers`() {
        assertEquals(6, gcd(48, 18))
        assertEquals(5, gcd(15, 25))
        assertEquals(12, gcd(60, 48))
        assertEquals(1, gcd(7, 13))
    }

    @Test
    fun `gcd should handle zero as second parameter`() {
        assertEquals(5, gcd(5, 0))
        assertEquals(42, gcd(42, 0))
        assertEquals(1, gcd(1, 0))
    }

    @Test
    fun `gcd should handle zero as first parameter`() {
        assertEquals(5, gcd(0, 5))
        assertEquals(42, gcd(0, 42))
        assertEquals(1, gcd(0, 1))
    }

    @Test
    fun `gcd should handle both parameters as zero`() {
        assertEquals(0, gcd(0, 0))
    }

    @Test
    fun `gcd should return 1 for coprime numbers`() {
        assertEquals(1, gcd(17, 19))
        assertEquals(1, gcd(25, 49))
        assertEquals(1, gcd(9, 28))
        assertEquals(1, gcd(13, 27))
    }

    @Test
    fun `gcd should handle when one number divides another`() {
        assertEquals(12, gcd(12, 36))
        assertEquals(7, gcd(7, 49))
        assertEquals(5, gcd(5, 100))
        assertEquals(8, gcd(8, 64))
    }

    @Test
    fun `gcd should return the number when both are the same`() {
        assertEquals(7, gcd(7, 7))
        assertEquals(100, gcd(100, 100))
        assertEquals(1, gcd(1, 1))
    }

    @Test
    fun `gcd should be commutative`() {
        assertEquals(gcd(48, 18), gcd(18, 48))
        assertEquals(gcd(100, 35), gcd(35, 100))
        assertEquals(gcd(7, 0), gcd(0, 7))
    }

    @Test
    fun `gcd should handle consecutive numbers`() {
        assertEquals(1, gcd(100, 101))
        assertEquals(1, gcd(53, 54))
        assertEquals(1, gcd(999, 1000))
    }

    @Test
    fun `gcd should handle powers of 2`() {
        assertEquals(16, gcd(16, 64))
        assertEquals(8, gcd(8, 32))
        assertEquals(4, gcd(12, 20))
    }

    @Test
    fun `gcd should handle large numbers`() {
        assertEquals(17, gcd(323, 391))
        assertEquals(125, gcd(1000, 625))
        assertEquals(333, gcd(999, 333))
        assertEquals(111, gcd(888, 333))
    }

    @Test
    fun `gcd should return correct result for basic positive longs`() {
        assertEquals(6L, gcd(48L, 18L))
        assertEquals(5L, gcd(15L, 25L))
        assertEquals(12L, gcd(60L, 48L))
    }

    @Test
    fun `gcd should handle zero for longs`() {
        assertEquals(5L, gcd(5L, 0L))
        assertEquals(42L, gcd(0L, 42L))
        assertEquals(0L, gcd(0L, 0L))
    }

    @Test
    fun `gcd should handle very large long numbers`() {
        assertEquals(1L, gcd(1000000007L, 1000000009L))
        assertEquals(100000L, gcd(10000000000L, 300000L))
        assertEquals(123450L, gcd(123450L, 493800L))
    }

    @Test
    fun `gcd should be commutative for longs`() {
        assertEquals(gcd(123456789L, 987654321L), gcd(987654321L, 123456789L))
        assertEquals(gcd(1000L, 0L), gcd(0L, 1000L))
    }

    @Test
    fun `lcm should return correct result for basic positive integers`() {
        assertEquals(24, lcm(8, 12))
        assertEquals(60, lcm(15, 20))
        assertEquals(36, lcm(12, 18))
        assertEquals(42, lcm(6, 21))
    }

    @Test
    fun `lcm should return 0 when first parameter is zero`() {
        assertEquals(0, lcm(0, 5))
        assertEquals(0, lcm(0, 42))
    }

    @Test
    fun `lcm should return 0 when second parameter is zero`() {
        assertEquals(0, lcm(5, 0))
        assertEquals(0, lcm(42, 0))
    }

    @Test
    fun `lcm should throw ArithmeticException when both parameters are zero`() {
        assertFailsWith<ArithmeticException> { lcm(0, 0) }
    }

    @Test
    fun `lcm should return product for coprime numbers`() {
        assertEquals(91, lcm(7, 13))
        assertEquals(323, lcm(17, 19))
        assertEquals(143, lcm(11, 13))
    }

    @Test
    fun `lcm should handle when one number divides another`() {
        assertEquals(36, lcm(12, 36))
        assertEquals(49, lcm(7, 49))
        assertEquals(100, lcm(5, 100))
        assertEquals(64, lcm(8, 64))
    }

    @Test
    fun `lcm should return the number when both are the same`() {
        assertEquals(7, lcm(7, 7))
        assertEquals(100, lcm(100, 100))
        assertEquals(1, lcm(1, 1))
    }

    @Test
    fun `lcm should be commutative`() {
        assertEquals(lcm(12, 18), lcm(18, 12))
        assertEquals(lcm(25, 35), lcm(35, 25))
        assertEquals(lcm(7, 0), lcm(0, 7))
    }

    @Test
    fun `lcm should handle consecutive numbers`() {
        assertEquals(100 * 101, lcm(100, 101))
        assertEquals(53 * 54, lcm(53, 54))
    }

    @Test
    fun `lcm should handle powers of 2`() {
        assertEquals(64, lcm(16, 64))
        assertEquals(32, lcm(8, 32))
    }

    @Test
    fun `lcm should handle large numbers`() {
        assertEquals(7429, lcm(323, 391))
        assertEquals(5000, lcm(1000, 625))
    }

    @Test
    fun `lcm should return correct result for basic positive longs`() {
        assertEquals(24L, lcm(8L, 12L))
        assertEquals(60L, lcm(15L, 20L))
        assertEquals(36L, lcm(12L, 18L))
    }

    @Test
    fun `lcm should return 0 for longs with zero`() {
        assertEquals(0L, lcm(0L, 5L))
        assertEquals(0L, lcm(42L, 0L))
    }

    @Test
    fun `lcm should throw ArithmeticException when both parameters are zero for longs`() {
        assertFailsWith<ArithmeticException> { lcm(0L, 0L) }
    }

    @Test
    fun `lcm should handle very large long numbers`() {
        assertEquals(1000000007L * 1000000009L, lcm(1000000007L, 1000000009L))
        assertEquals(30000000000L, lcm(10000000000L, 300000L))
    }

    @Test
    fun `lcm should be commutative for longs`() {
        assertEquals(lcm(123L, 456L), lcm(456L, 123L))
        assertEquals(lcm(1000L, 0L), lcm(0L, 1000L))
    }

    @Test
    fun `gcd and lcm should satisfy fundamental relationship for ints`() {
        val pairs = listOf(
            12 to 18,
            15 to 25,
            48 to 18,
            7 to 13,
            100 to 35
        )

        for ((a, b) in pairs) {
            assertEquals(a * b, gcd(a, b) * lcm(a, b), "Failed for pair ($a, $b)")
        }
    }

    @Test
    fun `gcd and lcm should satisfy fundamental relationship for longs`() {
        val pairs = listOf(
            12L to 18L,
            123456L to 789012L,
            1000L to 625L,
            17L to 19L
        )

        for ((a, b) in pairs) {
            assertEquals(a * b, gcd(a, b) * lcm(a, b), "Failed for pair ($a, $b)")
        }
    }

    @Test
    fun `lcm should always be greater than or equal to both numbers`() {
        val pairs = listOf(
            12 to 18,
            7 to 13,
            100 to 35,
            5 to 5
        )

        for ((a, b) in pairs) {
            val result = lcm(a, b)
            assertTrue(result >= a && result >= b, "LCM $result should be >= both $a and $b")
        }
    }

    @Test
    fun `gcd should always be less than or equal to both numbers`() {
        val pairs = listOf(
            12 to 18,
            7 to 13,
            100 to 35,
            5 to 5
        )

        for ((a, b) in pairs) {
            val result = gcd(a, b)
            assertTrue(result <= a && result <= b, "GCD $result should be <= both $a and $b")
        }
    }

    @Test
    fun `gcd should divide both numbers`() {
        val pairs = listOf(
            48 to 18,
            100 to 35,
            323 to 391,
            12 to 18
        )

        for ((a, b) in pairs) {
            val result = gcd(a, b)
            assertEquals(0, a % result, "GCD $result should divide $a")
            assertEquals(0, b % result, "GCD $result should divide $b")
        }
    }

    @Test
    fun `both numbers should divide lcm`() {
        val pairs = listOf(
            12 to 18,
            7 to 13,
            100 to 35,
            8 to 12
        )

        for ((a, b) in pairs) {
            val result = lcm(a, b)
            assertEquals(0, result % a, "LCM $result should be divisible by $a")
            assertEquals(0, result % b, "LCM $result should be divisible by $b")
        }
    }
}
