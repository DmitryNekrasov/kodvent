@file:Suppress("RedundantVisibilityModifier")

package kodvent.math

public fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

public fun lcm(a: Int, b: Int): Int = a / gcd(a, b) * b

public fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

public fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b
