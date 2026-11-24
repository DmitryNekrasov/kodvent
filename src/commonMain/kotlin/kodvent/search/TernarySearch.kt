/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.search

/**
 * Returns the argument in [[left], [right]] that maximizes the unimodal function [f] (strictly increasing
 * then strictly decreasing), found by ternary search: the interval is narrowed to `1e-9`, or to two
 * adjacent doubles where they are more than `1e-9` apart.
 *
 * @throws IllegalArgumentException if [left] > [right].
 *
 * @sample samples.TernarySearchSamples.maximizeQuadratic
 * @sample samples.TernarySearchSamples.minimizeByNegation
 */
public inline fun ternarySearch(left: Double, right: Double, f: (Double) -> Double): Double {
    require(left <= right) { "left bound ($left) is greater than right bound ($right)." }
    val eps = 1e-9
    var l = left
    var r = right
    while (r - l > eps) {
        val m1 = l + (r - l) / 3
        val m2 = r - (r - l) / 3
        // Beyond 2^23 in magnitude adjacent doubles are more than eps apart: the interval cannot get that narrow.
        if (m1 <= l || m2 >= r) break
        if (f(m1) < f(m2)) {
            l = m1
        } else {
            r = m2
        }
    }
    // Not (l + r) / 2: the sum overflows for bounds near Double.MAX_VALUE.
    return l + (r - l) / 2
}
