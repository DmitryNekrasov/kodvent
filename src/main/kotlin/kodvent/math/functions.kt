@file:Suppress("RedundantVisibilityModifier")

package kodvent.math

/**
 * Computes the greatest common divisor (GCD) of two integers using the Euclidean algorithm.
 *
 * The GCD is the largest positive integer that divides both numbers without a remainder.
 *
 * @param a the first integer
 * @param b the second integer
 *
 * @return the greatest common divisor of [a] and [b]
 *
 * @see lcm
 *
 * @sample samples.GcdAndLcmSamples.gcdBasicUsage
 * @sample samples.GcdAndLcmSamples.gcdSimplifyingFractions
 * @sample samples.GcdAndLcmSamples.gcdReducingRatios
 */
public tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/**
 * Computes the least common multiple (LCM) of two integers.
 *
 * The LCM is the smallest positive integer that is divisible by both numbers.
 * This function uses the relationship: `lcm(a, b) = (a * b) / gcd(a, b)`,
 * but calculates it as `(a / gcd(a, b)) * b` to reduce the risk of integer overflow.
 *
 * @param a the first integer
 * @param b the second integer
 *
 * @return the least common multiple of [a] and [b]
 *
 * @see gcd
 *
 * @sample samples.GcdAndLcmSamples.lcmBasicUsage
 * @sample samples.GcdAndLcmSamples.lcmSchedulingProblem
 */
public fun lcm(a: Int, b: Int): Int = a / gcd(a, b) * b

/**
 * See [gcd] for Int parameters
 */
public tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

/**
 * See [lcm] for Int parameters
 */
public fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b
