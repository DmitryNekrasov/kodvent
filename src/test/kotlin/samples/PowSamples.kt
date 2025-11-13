package samples

import kodvent.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

class PowSamples {

    @Test
    fun powBasicUsage() {
        val result1 = 2L.pow(3L)
        assertEquals(8L, result1)

        val result2 = 5L.pow(3L)
        assertEquals(125L, result2)

        val result3 = 10L.pow(6L)
        assertEquals(1000000L, result3)
    }

    @Test
    fun powInfixNotation() {
        // Use infix notation for cleaner syntax
        val result1 = 2L pow 10L
        assertEquals(1024L, result1)

        val result2 = 3L pow 4L
        assertEquals(81L, result2)

        val area = 5L pow 2L
        assertEquals(25L, area)
    }

    @Test
    fun powModuloBasicUsage() {
        // Compute powers modulo a number to prevent overflow
        val base = 2L
        val power = 100L
        val modulo = 1000000007L

        val result = base.pow(power, modulo)
        assertEquals(976371285L, result)
    }

    @Test
    fun powModuloLastDigit() {
        // Find the last digit of a large power
        val base = 7L
        val power = 100L
        val modulo = 10L  // The last digit is the number mod 10

        val lastDigit = base.pow(power, modulo)

        // The last digit of 7^100 is 1
        assertEquals(1L, lastDigit)
    }

    @Test
    fun powModuloFermatLittleTheorem() {
        // Verify Fermat's Little Theorem: a^(p-1) ≡ 1 (mod p) for prime p
        // https://en.wikipedia.org/wiki/Fermat%27s_little_theorem
        val a = 5L
        val p = 13L

        val result = a.pow(p - 1, p)

        assertEquals(1L, result)
    }
}
