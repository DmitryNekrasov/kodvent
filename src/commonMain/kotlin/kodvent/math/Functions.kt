/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.math

import kotlin.math.abs

/**
 * Computes the greatest common divisor of [a] and [b] using the Euclidean algorithm.
 *
 * @see lcm
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 */
public tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) abs(a) else gcd(b, a % b)

/**
 * Computes the least common multiple of [a] and [b], as `abs(a) / gcd(a, b) * abs(b)` to reduce the risk of
 * overflow.
 *
 * @see gcd
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 */
public fun lcm(a: Int, b: Int): Int = if (a == 0 || b == 0) 0 else abs(a) / gcd(a, b) * abs(b)

/**
 * See [gcd] for Int parameters
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 */
public tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

/**
 * See [lcm] for Int parameters
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 */
public fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0L else abs(a) / gcd(a, b) * abs(b)

/**
 * Raises this Long to the given [power] (which must be non-negative) using
 * [binary exponentiation](https://en.wikipedia.org/wiki/Exponentiation_by_squaring), in O(log n) time.
 *
 * **Warning**: does not check for overflow — large values wrap around.
 *
 * @see pow overload with modulo parameter for modular exponentiation
 *
 * @sample samples.PowSamples.powBasicUsage
 */
public infix fun Long.pow(power: Long): Long {
    require(power >= 0) { "Power must be non-negative, but was $power" }
    return binaryExponentiation(this, power) { x, y -> x * y }
}

/**
 * Raises this Long to the given [power] (non-negative) modulo [modulo] (positive), using binary
 * exponentiation in O(log power) multiplications; the result is in `[0, modulo)`. The modulus is applied at
 * each step to avoid overflow: for a [modulo] above 3,037,000,500, where a product of two remainders can
 * overflow a Long, a multiplication takes O(log modulo) additions.
 *
 * @see pow overload without a modulo parameter for regular exponentiation
 *
 * @sample samples.PowSamples.powModuloBasicUsage
 */
public fun Long.pow(power: Long, modulo: Long): Long {
    require(power >= 0) { "Power must be non-negative, but was $power" }
    require(modulo > 0) { "Modulo must be positive, but was $modulo" }
    if (modulo == 1L) return 0L
    val base = this.mod(modulo)
    // The product of two remainders fits in a Long only while (modulo - 1)^2 <= Long.MAX_VALUE.
    if (modulo > 3_037_000_500L) return powByDoubling(base, power, modulo)
    return binaryExponentiation(base, power) { x, y -> x * y % modulo }
}

// Not a second loop inside pow: that made a call with a literal modulo of 998,244,353 a fifth slower on the JVM.
private fun powByDoubling(base: Long, power: Long, modulo: Long): Long =
    binaryExponentiation(base, power) { x, y -> multiplyModulo(x, y, modulo) }

private fun multiplyModulo(x: Long, y: Long, modulo: Long): Long {
    var a = x
    var b = y
    var result = 0L
    while (b > 0) {
        if (b and 1L == 1L) {
            result = addModulo(result, a, modulo)
        }
        a = addModulo(a, a, modulo)
        b = b shr 1
    }
    return result
}

// x + y can exceed Long.MAX_VALUE, modulo - y cannot.
private fun addModulo(x: Long, y: Long, modulo: Long): Long = if (x >= modulo - y) x - (modulo - y) else x + y

private inline fun binaryExponentiation(base: Long, power: Long, multiply: (Long, Long) -> Long): Long {
    var a = base
    var b = power
    var result = 1L
    while (b > 0) {
        if (b and 1L == 1L) {
            result = multiply(result, a)
        }
        a = multiply(a, a)
        b = b shr 1
    }
    return result
}

/**
 * Returns the square of [this] Int value.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 */
public fun Int.sqr(): Int = this * this

/**
 * Returns the square of [this] Long value.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 */
public fun Long.sqr(): Long = this * this

/**
 * Returns the square of [this] Double value.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 */
public fun Double.sqr(): Double = this * this
