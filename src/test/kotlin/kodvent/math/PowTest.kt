package kodvent.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PowTest {

    @Test
    fun `pow should return correct result for basic cases`() {
        assertEquals(8L, 2L.pow(3L))
        assertEquals(125L, 5L.pow(3L))
        assertEquals(64L, 4L.pow(3L))
        assertEquals(1024L, 2L.pow(10L))
    }

    @Test
    fun `pow should return 1 when power is 0`() {
        assertEquals(1L, 2L.pow(0L))
        assertEquals(1L, 5L.pow(0L))
        assertEquals(1L, 100L.pow(0L))
        assertEquals(1L, 999L.pow(0L))
    }

    @Test
    fun `pow should return base when power is 1`() {
        assertEquals(2L, 2L.pow(1L))
        assertEquals(5L, 5L.pow(1L))
        assertEquals(100L, 100L.pow(1L))
        assertEquals(999L, 999L.pow(1L))
    }

    @Test
    fun `pow should return 0 when base is 0 and power is positive`() {
        assertEquals(0L, 0L.pow(1L))
        assertEquals(0L, 0L.pow(5L))
        assertEquals(0L, 0L.pow(100L))
    }

    @Test
    fun `pow should return 1 when base is 1`() {
        assertEquals(1L, 1L.pow(0L))
        assertEquals(1L, 1L.pow(1L))
        assertEquals(1L, 1L.pow(5L))
        assertEquals(1L, 1L.pow(100L))
        assertEquals(1L, 1L.pow(1000L))
        assertEquals(1L, 1L.pow(1000000000000000000L))
    }

    @Test
    fun `pow should handle negative base correctly`() {
        assertEquals(-8L, (-2L).pow(3L))
        assertEquals(16L, (-2L).pow(4L))
        assertEquals(-32L, (-2L).pow(5L))
        assertEquals(64L, (-2L).pow(6L))
    }

    @Test
    fun `pow should handle base of -1`() {
        assertEquals(1L, (-1L).pow(0L))
        assertEquals(-1L, (-1L).pow(1L))
        assertEquals(1L, (-1L).pow(2L))
        assertEquals(-1L, (-1L).pow(3L))
        assertEquals(1L, (-1L).pow(100L))
        assertEquals(-1L, (-1L).pow(101L))
    }

    @Test
    fun `pow should work with infix notation`() {
        assertEquals(8L, 2L pow 3L)
        assertEquals(125L, 5L pow 3L)
        assertEquals(1024L, 2L pow 10L)
    }

    @Test
    fun `pow should handle powers of 2`() {
        assertEquals(2L, 2L.pow(1L))
        assertEquals(4L, 2L.pow(2L))
        assertEquals(8L, 2L.pow(3L))
        assertEquals(16L, 2L.pow(4L))
        assertEquals(32L, 2L.pow(5L))
        assertEquals(1024L, 2L.pow(10L))
        assertEquals(32768L, 2L.pow(15L))
    }

    @Test
    fun `pow should handle powers of 10`() {
        assertEquals(10L, 10L.pow(1L))
        assertEquals(100L, 10L.pow(2L))
        assertEquals(1000L, 10L.pow(3L))
        assertEquals(10000L, 10L.pow(4L))
        assertEquals(100000L, 10L.pow(5L))
        assertEquals(1000000L, 10L.pow(6L))
    }

    @Test
    fun `pow should handle large exponents efficiently`() {
        assertEquals(1L, 1L.pow(1000000L))
        assertEquals(0L, 0L.pow(1000000L))
    }

    @Test
    fun `pow should compute squares correctly`() {
        assertEquals(4L, 2L.pow(2L))
        assertEquals(9L, 3L.pow(2L))
        assertEquals(16L, 4L.pow(2L))
        assertEquals(25L, 5L.pow(2L))
        assertEquals(100L, 10L.pow(2L))
        assertEquals(10000L, 100L.pow(2L))
    }

    @Test
    fun `pow should compute cubes correctly`() {
        assertEquals(8L, 2L.pow(3L))
        assertEquals(27L, 3L.pow(3L))
        assertEquals(64L, 4L.pow(3L))
        assertEquals(125L, 5L.pow(3L))
        assertEquals(1000L, 10L.pow(3L))
    }

    @Test
    fun `pow with modulo should return correct result for basic cases`() {
        assertEquals(4L, 2L.pow(10L, 5L))
        assertEquals(4L, 3L.pow(4L, 7L))
        assertEquals(1L, 5L.pow(3L, 31L))
        assertEquals(8L, 2L.pow(3L, 10L))
    }

    @Test
    fun `pow with modulo should return 1 when power is 0`() {
        assertEquals(1L, 2L.pow(0L, 5L))
        assertEquals(1L, 5L.pow(0L, 7L))
        assertEquals(1L, 100L.pow(0L, 13L))
        assertEquals(1L, 999L.pow(0L, 1000L))
    }

    @Test
    fun `pow with modulo should return 0 when modulo is 1`() {
        assertEquals(0L, 2L.pow(3L, 1L))
        assertEquals(0L, 5L.pow(10L, 1L))
        assertEquals(0L, 100L.pow(100L, 1L))
    }

    @Test
    fun `pow with modulo should handle base of 0`() {
        assertEquals(0L, 0L.pow(5L, 7L))
        assertEquals(0L, 0L.pow(100L, 13L))
    }

    @Test
    fun `pow with modulo should handle base of 1`() {
        assertEquals(1L, 1L.pow(0L, 7L))
        assertEquals(1L, 1L.pow(1L, 7L))
        assertEquals(1L, 1L.pow(100L, 7L))
        assertEquals(1L, 1L.pow(1000L, 13L))
    }

    @Test
    fun `pow with modulo should reduce base larger than modulo`() {
        assertEquals(1L, 10L.pow(2L, 9L))
        assertEquals(5L, 15L.pow(2L, 11L))
        assertEquals(0L, 7L.pow(3L, 7L))
    }

    @Test
    fun `pow with modulo should handle large exponents efficiently`() {
        assertEquals(0L, 2L.pow(1000000L, 1L))
        assertEquals(1L, 2L.pow(1000000L, 5L))
    }

    @Test
    fun `pow with modulo should work with prime modulo`() {
        assertEquals(1L, 2L.pow(6L, 7L))
        assertEquals(1L, 3L.pow(10L, 11L))
        assertEquals(1L, 5L.pow(12L, 13L))
    }

    @Test
    fun `pow with modulo should handle modular arithmetic correctly`() {
        assertEquals(3L, 3L.pow(5L, 5L))
        assertEquals(1L, 4L.pow(3L, 9L))
        assertEquals(1L, 7L.pow(4L, 10L))
    }

    @Test
    fun `pow with modulo should prevent overflow for large bases and exponents`() {
        val modulo = 1000000007L
        assertEquals(49L, 7L.pow(2L, modulo))
        assertEquals(343L, 7L.pow(3L, modulo))

        val result = 2L.pow(100L, modulo)
        assertEquals(976371285L, result)
    }

    @Test
    fun `pow with modulo should handle consecutive modular exponentiation`() {
        // Testing that (a^b)^c ≡ a^(b*c) (mod m)
        val base = 3L
        val exp1 = 4L
        val exp2 = 2L
        val modulo = 13L

        val intermediate = base.pow(exp1, modulo)
        val result1 = intermediate.pow(exp2, modulo)
        val result2 = base.pow(exp1 * exp2, modulo)

        assertEquals(result2, result1)
    }

    @Test
    fun `pow with modulo should handle power of 1`() {
        assertEquals(2L, 2L.pow(1L, 7L))
        assertEquals(5L, 5L.pow(1L, 11L))
        assertEquals(10L, 10L.pow(1L, 13L))
    }

    @Test
    fun `pow with modulo should work for computing last digits`() {
        assertEquals(4L, 2L.pow(10L, 10L))
        assertEquals(1L, 7L.pow(100L, 10L))
        assertEquals(1L, 3L.pow(20L, 100L))
    }

    @Test
    fun `pow with modulo should compute RSA-like exponentiation`() {
        // Simulate simple RSA-like calculation
        val message = 42L
        val publicExp = 17L
        val modulus = 11 * 13L

        val encrypted = message.pow(publicExp, modulus)

        // Private exponent for these parameters
        val privateExp = 113L
        val decrypted = encrypted.pow(privateExp, modulus)

        assertEquals(message, decrypted)
    }

    @Test
    fun `pow with modulo should handle negative base correctly using mod()`() {
        // Using mod() ensures the result is always in the range [0, modulo)
        // This is different from % which can return negative results

        // (-3).mod(5) = 2, so (-3)^2 mod 5 = 2^2 mod 5 = 4
        assertEquals(4L, (-3L).pow(2L, 5L))

        // (-2).mod(7) = 5, so (-2)^3 mod 7 = 5^3 mod 7 = 125 mod 7 = 6
        assertEquals(6L, (-2L).pow(3L, 7L))

        // (-5).mod(13) = 8, so (-5)^2 mod 13 = 8^2 mod 13 = 64 mod 13 = 12
        assertEquals(12L, (-5L).pow(2L, 13L))
    }

    @Test
    fun `pow with modulo should handle negative base with odd power`() {
        // For odd powers, the mathematical result would be negative,
        // but mod() normalizes it to the range [0, modulo)

        // (-2).mod(5) = 3, so (-2)^3 mod 5 = 3^3 mod 5 = 27 mod 5 = 2
        assertEquals(2L, (-2L).pow(3L, 5L))

        // (-3).mod(7) = 4, so (-3)^5 mod 7 = 4^5 mod 7 = 1024 mod 7 = 2
        assertEquals(2L, (-3L).pow(5L, 7L))
    }

    @Test
    fun `pow with modulo should handle negative base with even power`() {
        // For even powers, the mathematical result would be positive

        // (-2).mod(5) = 3, so (-2)^4 mod 5 = 3^4 mod 5 = 81 mod 5 = 1
        assertEquals(1L, (-2L).pow(4L, 5L))

        // (-3).mod(7) = 4, so (-3)^6 mod 7 = 4^6 mod 7 = 4096 mod 7 = 1
        assertEquals(1L, (-3L).pow(6L, 7L))
    }

    @Test
    fun `pow with modulo should demonstrate difference between mod and remainder`() {
        // This test documents the behavior difference between % and mod()
        // % (remainder) can return negative values, while mod() always returns non-negative

        // Example: -3 % 5 = -3 (remainder), but (-3).mod(5) = 2 (modulo)
        // The function uses mod(), so negative bases are normalized to positive equivalents

        val negativeBase = -7L
        val power = 3L
        val modulo = 11L

        // (-7).mod(11) = 4, so the computation is equivalent to 4^3 mod 11
        val result = negativeBase.pow(power, modulo)
        val expectedEquivalent = 4L.pow(power, modulo)

        assertEquals(expectedEquivalent, result)
        assertEquals(9L, result)
    }

    @Test
    fun `pow should throw when power is negative`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L) }
    }

    @Test
    fun `pow should throw when power is very negative`() {
        assertFailsWith<IllegalArgumentException> { 5L.pow(-100L) }
    }

    @Test
    fun `pow with modulo should throw when power is negative`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L, 5L) }
    }

    @Test
    fun `pow with modulo should throw when modulo is zero`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(3L, 0L) }
    }

    @Test
    fun `pow with modulo should throw when modulo is negative`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(3L, -5L) }
    }

    @Test
    fun `pow with modulo should throw when both power and modulo are invalid`() {
        assertFailsWith<IllegalArgumentException> { 2L.pow(-1L, -5L) }
    }
}
