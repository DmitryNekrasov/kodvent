/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.graph.bfs
import kodvent.graph.dijkstra
import kodvent.grid.Direction
import kodvent.grid.Position
import kodvent.grid.neighbors
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphSamples {

    @Test
    fun shortestPathInMaze() {
        val maze = listOf(
            ".#...",
            ".#.#.",
            "...#.",
            "####.",
            "#..#.",
        ).toGrid()

        val steps = bfs(Position(0, 0)) { p -> maze.neighbors(p).filter { maze[it] == '.' } }

        assertEquals(12, steps[Position(4, 4)])
        assertEquals(-1, steps[Position(1, 4)] ?: -1)
    }

    @Test
    fun floodFill() {
        val garden = listOf(
            "AAB",
            "ABB",
            "CCB",
        ).toGrid()

        val seen = mutableSetOf<Position>()
        val areas = mutableListOf<Int>()
        for (position in garden.positions) {
            if (position in seen) continue
            val region = bfs(position) { p -> garden.neighbors(p).filter { garden[it] == garden[p] } }.keys
            seen += region
            areas += region.size
        }

        assertEquals(listOf(3, 4, 2), areas)
    }

    @Test
    fun statesThatAreNotCells() {
        // Jugs of 3 and 5 liters: fill one, empty one, or pour one into the other
        val steps = bfs(0 to 0) { (small, big) ->
            val toBig = minOf(small, 5 - big)
            val toSmall = minOf(big, 3 - small)
            listOf(
                3 to big, small to 5, 0 to big, small to 0,
                small - toBig to big + toBig, small + toSmall to big - toSmall,
            )
        }

        assertEquals(6, steps.filterKeys { (_, big) -> big == 4 }.values.min())
    }

    @Test
    fun severalStarts() {
        val map = listOf(
            "S...",
            "....",
            "...S",
        ).toGrid()

        val toNearest = bfs(starts = map.positionsOf('S')) { p -> map.neighbors(p) }

        assertEquals(0, toNearest[Position(3, 2)])
        assertEquals(2, toNearest[Position(3, 0)])
        assertEquals(2, toNearest.values.max())
    }

    @Test
    fun dijkstraOnGraph() {
        val edges = listOf(
            listOf(1 to 7, 2 to 9, 5 to 14),
            listOf(0 to 7, 2 to 10, 3 to 15),
            listOf(0 to 9, 1 to 10, 3 to 11, 5 to 2),
            listOf(1 to 15, 2 to 11, 4 to 6),
            listOf(3 to 6, 5 to 9),
            listOf(0 to 14, 2 to 2, 4 to 9),
        )

        val costs = dijkstra(0) { vertex -> edges[vertex] }

        assertEquals(listOf(0, 7, 9, 20, 20, 11), edges.indices.map { costs.getValue(it) })
    }

    @Test
    fun dijkstraOverStates() {
        // The cheapest route to the bottom-right cell with at most two steps in a row in one direction
        data class Cart(val position: Position, val direction: Direction, val run: Int)

        val grid = listOf(
            "1111",
            "9951",
            "9991",
        ).toGrid { it.digitToInt() }

        val origin = Position(0, 0)
        val starts = listOf(Cart(origin, Direction.RIGHT, 0), Cart(origin, Direction.DOWN, 0))
        val costs = dijkstra(starts = starts) { (position, direction, run) ->
            listOf(direction, direction.turnLeft(), direction.turnRight())
                .map { Cart(position + it, it, if (it == direction) run + 1 else 1) }
                .filter { it.position in grid && it.run <= 2 }
                .map { it to grid[it.position] }
        }

        // Three steps to the right in a row are not allowed, so the route goes through the 5
        assertEquals(9, costs.filterKeys { it.position == grid.positions.last() }.values.min())
    }
}
