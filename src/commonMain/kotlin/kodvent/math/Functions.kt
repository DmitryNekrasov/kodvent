/*
 * Copyright 2025 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.math

/**
 * Computes the greatest common divisor (GCD) of two integers, [a] and [b], using the Euclidean algorithm.
 *
 * The GCD is the largest positive integer that divides both numbers without a remainder.
 *
 * @see lcm
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 * @sample samples.GcdAndLcmSamples.gcdSimplifyingFractions
 * @sample samples.GcdAndLcmSamples.gcdReducingRatios
 */
public tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/**
 * Computes the least common multiple (LCM) of two integers, [a] and [b].
 *
 * The LCM is the smallest positive integer that is divisible by both numbers.
 * This function uses the relationship: `lcm(a, b) = (a * b) / gcd(a, b)`,
 * but calculates it as `(a / gcd(a, b)) * b` to reduce the risk of integer overflow.
 *
 * @see gcd
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 * @sample samples.GcdAndLcmSamples.lcmSchedulingProblem
 */
public fun lcm(a: Int, b: Int): Int = if (a == 0 || b == 0) 0 else a / gcd(a, b) * b

/**
 * See [gcd] for Int parameters
 */
public tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

/**
 * See [lcm] for Int parameters
 */
public fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0L else a / gcd(a, b) * b

/**
 * Raises this Long to the given [power] (which must be non-negative) using binary exponentiation.
 *
 * This function uses the fast binary exponentiation algorithm
 * (also known as [exponentiation by squaring](https://en.wikipedia.org/wiki/Exponentiation_by_squaring)),
 * which computes the result in O(log n) time, where n is the exponent.
 *
 * **Warning**: This function does not check for overflow. For large bases or exponents,
 * the result may overflow and wrap around.
 *
 * @see pow overload with modulo parameter for modular exponentiation
 *
 * @sample samples.PowSamples.powBasicUsage
 * @sample samples.PowSamples.powInfixNotation
 */
public infix fun Long.pow(power: Long): Long {
    require(power >= 0) { "Power must be non-negative, but was $power" }
    return binaryExponentiation(this, power) { x, y -> x * y }
}

/**
 * Raises this Long to the given [power] (which must be non-negative), modulo [modulo] (which must be
 * positive), using binary exponentiation; the result is in the range `[0, modulo)`.
 *
 * This function uses the fast binary exponentiation algorithm with modular arithmetic,
 * computing the result in O(log n) time, where n is the exponent. The modulo operation
 * is applied at each step to prevent overflow.
 *
 * @see pow overload without a modulo parameter for regular exponentiation
 *
 * @sample samples.PowSamples.powModuloBasicUsage
 * @sample samples.PowSamples.powModuloLastDigit
 * @sample samples.PowSamples.powModuloFermatLittleTheorem
 */
public fun Long.pow(power: Long, modulo: Long): Long {
    require(power >= 0) { "Power must be non-negative, but was $power" }
    require(modulo > 0) { "Modulo must be positive, but was $modulo" }
    return binaryExponentiation(this.mod(modulo), power) { x, y -> x * y % modulo }
}

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
 * @sample samples.SqrSamples.sqrDistanceCalculation
 * @sample samples.SqrSamples.sqrAreaCalculation
 */
public fun Int.sqr(): Int = this * this

/**
 * Returns the square of [this] Long value.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 * @sample samples.SqrSamples.sqrDistanceCalculation
 * @sample samples.SqrSamples.sqrAreaCalculation
 */
public fun Long.sqr(): Long = this * this

/**
 * Returns the square of [this] Double value.
 *
 * @sample samples.SqrSamples.sqrBasicUsage
 * @sample samples.SqrSamples.sqrDistanceCalculation
 * @sample samples.SqrSamples.sqrAreaCalculation
 */
public fun Double.sqr(): Double = this * this
