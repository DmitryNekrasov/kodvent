/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.graph

import kodvent.datastructures.Heap

/**
 * [Breadth-first search](https://en.wikipedia.org/wiki/Breadth-first_search) from [start]: returns the
 * fewest steps from [start] to every state it reaches, with [start] at 0. A state that cannot be reached is
 * not in the map, so read it with `[end] ?: -1`, or with `getValue(end)`, which throws.
 *
 * [neighbors] returns the states one step away from a state; it is called once for every reached state.
 * A state is any value whose `equals` and `hashCode` do not change, such as a `Position`, a data class or a
 * list. An array compares by identity, so use a list where states with the same contents must be equal. A
 * state that is itself a collection needs a named argument, `bfs(start = listOf(0, 0))`.
 * The search visits every reachable state, so [neighbors] has to keep an endless space finite. The order of
 * the map is unspecified. O(V + E) for V reached states and E steps out of them.
 *
 * @sample samples.GraphSamples.shortestPathInMaze
 * @sample samples.GraphSamples.floodFill
 * @sample samples.GraphSamples.statesThatAreNotCells
 */
public fun <S> bfs(start: S, neighbors: (state: S) -> Iterable<S>): Map<S, Int> =
    bfs(starts = listOf(start), neighbors = neighbors)

/**
 * [Breadth-first search](https://en.wikipedia.org/wiki/Breadth-first_search) from all of [starts] at once:
 * returns the fewest steps from the nearest start to every state the search reaches, with each start at 0.
 * A start that repeats counts once, and no starts give an empty map. Otherwise it works like [bfs] from a
 * single start. A collection of starts with a lambda that does not give the type of its parameter fits both
 * overloads, so name the argument: `bfs(starts = sources) { ... }`. O(V + E).
 *
 * @sample samples.GraphSamples.severalStarts
 */
public fun <S> bfs(starts: Iterable<S>, neighbors: (state: S) -> Iterable<S>): Map<S, Int> {
    // A LinkedHashMap iterates in insertion order on every platform; the order of a HashMap differs between them.
    val distances = LinkedHashMap<S, Int>()
    val queue = ArrayDeque<S>()
    for (start in starts) if (distances.put(start, 0) == null) queue.addLast(start)
    var distance = 0
    while (queue.isNotEmpty()) {
        distance++
        repeat(queue.size) {
            for (next in neighbors(queue.removeFirst())) {
                if (next !in distances) {
                    distances[next] = distance
                    queue.addLast(next)
                }
            }
        }
    }
    return distances
}

/**
 * [Dijkstra's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) from [start]: returns the
 * cost of a cheapest path from [start] to every state it reaches, with [start] at 0. A state that cannot be
 * reached is not in the map.
 *
 * [neighbors] returns the steps out of a state as `next to cost` pairs; it is called once for every reached
 * state, when its cost is final. A cost may be zero but not negative. States follow the same rules as in
 * [bfs], and the order of the map is unspecified. O((V + E) log(V + E)) for V reached states and E steps
 * out of them.
 *
 * @throws IllegalArgumentException if a step has a negative cost
 * @throws ArithmeticException if the cost of a cheapest path to a reachable state exceeds [Int.MAX_VALUE]
 *
 * @sample samples.GraphSamples.dijkstraOnGraph
 * @sample samples.GraphSamples.cellsOnAnyCheapestPath
 */
public fun <S> dijkstra(start: S, neighbors: (state: S) -> Iterable<Pair<S, Int>>): Map<S, Int> =
    dijkstra(starts = listOf(start), neighbors = neighbors)

/**
 * [Dijkstra's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) from all of [starts] at once:
 * returns the cost of a cheapest path from the nearest start to every state the search reaches, with each
 * start at 0. A start that repeats counts once, and no starts give an empty map. Otherwise it works like
 * [dijkstra] from a single start. A collection of starts with a lambda that does not give the type of its
 * parameter fits both overloads, so name the argument: `dijkstra(starts = sources) { ... }`.
 * O((V + E) log(V + E)).
 *
 * @throws IllegalArgumentException if a step has a negative cost
 * @throws ArithmeticException if the cost of a cheapest path to a reachable state exceeds [Int.MAX_VALUE]
 *
 * @sample samples.GraphSamples.dijkstraOverStates
 * @sample samples.GraphSamples.cellsOnAnyCheapestPath
 */
public fun <S> dijkstra(starts: Iterable<S>, neighbors: (state: S) -> Iterable<Pair<S, Int>>): Map<S, Int> {
    val costs = LinkedHashMap<S, Int>()
    val heap = Heap<Entry<S>>(byCost)
    var overflowed: MutableSet<S>? = null
    for (start in starts) if (costs.put(start, 0) == null) heap.push(Entry(0, start))
    while (heap.isNotEmpty()) {
        val entry = heap.pop()
        val state = entry.state
        val cost = entry.cost
        if (cost > costs.getValue(state)) continue
        for ((next, step) in neighbors(state)) {
            require(step >= 0) { "The step from $state to $next has a negative cost: $step" }
            val candidate = cost + step
            val known = costs[next]
            // Both terms are non-negative, so a negative sum is an overflow. It is an error only if no other
            // path reaches next, which is known when the search ends.
            if (candidate < 0) {
                if (known == null) overflowed = (overflowed ?: mutableSetOf()).apply { add(next) }
            } else if (known == null || candidate < known) {
                costs[next] = candidate
                heap.push(Entry(candidate, next))
            }
        }
    }
    val tooExpensive = overflowed?.firstOrNull { it !in costs }
    if (tooExpensive != null) throw ArithmeticException("The cost of $tooExpensive exceeds Int.MAX_VALUE")
    return costs
}

private class Entry<S>(val cost: Int, val state: S)

private val byCost = Comparator<Entry<*>> { a, b -> a.cost.compareTo(b.cost) }
