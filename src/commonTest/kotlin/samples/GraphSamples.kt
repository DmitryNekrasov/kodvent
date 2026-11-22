/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.graph.bfs
import kodvent.graph.dijkstra
import kodvent.grid.Direction
import kodvent.grid.Grid
import kodvent.grid.Position
import kodvent.grid.neighbors
import kodvent.grid.toGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        assertNull(steps[Position(1, 4)])
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
            listOf(3 to big, small to 5, 0 to big, small to 0, small - toBig to big + toBig, small + toSmall to big - toSmall)
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
        // With a limit on steps in a row, one cell can be worth reaching twice
        data class Cart(val position: Position, val direction: Direction, val run: Int)

        fun cheapestRoute(grid: Grid<Int>, maxRun: Int): Int {
            val origin = Position(0, 0)
            val starts = listOf(Cart(origin, Direction.RIGHT, 0), Cart(origin, Direction.DOWN, 0))
            val costs = dijkstra(starts = starts) { (position, direction, run) ->
                listOf(direction, direction.turnLeft(), direction.turnRight())
                    .map { Cart(position + it, it, if (it == direction) run + 1 else 1) }
                    .filter { it.position in grid && it.run <= maxRun }
                    .map { it to grid[it.position] }
            }
            return costs.filterKeys { it.position == grid.positions.last() }.values.min()
        }

        val grid = listOf(
            "1111",
            "9951",
            "9991",
        ).toGrid { it.digitToInt() }

        assertEquals(5, cheapestRoute(grid, maxRun = 3))

        // Three steps to the right in a row are not allowed, so the route goes through the 5
        assertEquals(9, cheapestRoute(grid, maxRun = 2))
    }

    @Test
    fun cellsOnAnyCheapestPath() {
        data class Walker(val position: Position, val direction: Direction)

        val maze = listOf(
            "#######",
            "#.....#",
            "#S.#.E#",
            "#.....#",
            "#######",
        ).toGrid()

        fun turns(walker: Walker) = listOf(walker.direction.turnLeft(), walker.direction.turnRight())
            .map { Walker(walker.position, it) to 10 }

        fun forward(walker: Walker): List<Pair<Walker, Int>> =
            turns(walker) + (Walker(walker.position + walker.direction, walker.direction) to 1)

        fun backward(walker: Walker): List<Pair<Walker, Int>> =
            turns(walker) + (Walker(walker.position - walker.direction, walker.direction) to 1)

        fun open(steps: List<Pair<Walker, Int>>) = steps.filter { (walker, _) -> maze[walker.position] != '#' }

        val end = maze.positionOf('E')
        val fromStart = dijkstra(Walker(maze.positionOf('S'), Direction.RIGHT)) { open(forward(it)) }
        val best = fromStart.filterKeys { it.position == end }.values.min()

        // Searching back from every cheapest arrival gives the cost from each state to the end
        val arrivals = fromStart.filter { (walker, cost) -> walker.position == end && cost == best }.keys
        val toEnd = dijkstra(starts = arrivals) { open(backward(it)) }
        val cells = toEnd.filter { (walker, cost) -> fromStart[walker]?.plus(cost) == best }.keys.map { it.position }

        assertEquals(36, best)
        assertEquals(13, cells.toSet().size)
    }
}
