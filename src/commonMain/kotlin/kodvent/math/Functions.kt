/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.math

import kotlin.math.abs

/**
 * Returns the greatest common divisor of [a] and [b]. If one is 0, the result is the absolute value of the
 * other. It is never negative, except when it would be 2^31: `gcd(Int.MIN_VALUE, 0)` is `Int.MIN_VALUE`.
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 */
public tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) abs(a) else gcd(b, a % b)

/**
 * Returns the least common multiple of [a] and [b], or 0 if either is 0. It wraps around only when it does
 * not fit in an Int, and it is never negative otherwise.
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 */
public fun lcm(a: Int, b: Int): Int = if (a == 0 || b == 0) 0 else abs(a) / gcd(a, b) * abs(b)

/**
 * Returns the greatest common divisor of [a] and [b]. If one is 0, the result is the absolute value of the
 * other. It is never negative, except when it would be 2^63: `gcd(Long.MIN_VALUE, 0L)` is `Long.MIN_VALUE`.
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 */
public tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

/**
 * Returns the least common multiple of [a] and [b], or 0 if either is 0. It wraps around only when it does
 * not fit in a Long, and it is never negative otherwise.
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 */
public fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0L else abs(a) / gcd(a, b) * abs(b)

/**
 * Returns this value raised to [power], wrapping around on overflow. As an infix call it binds looser than
 * arithmetic operators: `2L pow n - 1` is `2L pow (n - 1)`. O(log power).
 *
 * @throws IllegalArgumentException if [power] is negative
 *
 * @sample samples.PowSamples.powBasicUsage
 */
public infix fun Long.pow(power: Long): Long {
    require(power >= 0) { "Power must be non-negative, but was $power" }
    return binaryExponentiation(this, power) { x, y -> x * y }
}

/**
 * Returns this value raised to [power] modulo [modulo]. The result is in `0..<modulo`, also for a negative
 * base, and exact for every [modulo]. O(log power) multiplications. Above a [modulo] of 3,037,000,500, where
 * a product of two remainders could overflow a Long, each multiplication takes O(log modulo) additions.
 *
 * @throws IllegalArgumentException if [power] is negative or [modulo] is not positive
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
 * Returns the square of [this] Int value, wrapping around when its absolute value exceeds 46,340.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 */
public fun Int.sqr(): Int = this * this

/**
 * Returns the square of [this] Long value, wrapping around when its absolute value exceeds 3,037,000,499.
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
