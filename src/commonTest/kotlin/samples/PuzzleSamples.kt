/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package samples

import kodvent.graph.bfs
import kodvent.graph.dijkstra
import kodvent.grid.Direction
import kodvent.grid.Grid
import kodvent.grid.MutableGrid
import kodvent.grid.Position
import kodvent.grid.neighbors
import kodvent.grid.neighbors8
import kodvent.grid.toGrid
import kodvent.grid.toMutableGrid
import kodvent.search.partitionPoint
import kotlin.test.Test
import kotlin.test.assertEquals

// The solutions that Module.md prints under "With kodvent", on inputs made for these tests. Change both together.
class PuzzleSamples {

    @Test
    fun printingDepartment() {
        class Day04 {
            fun Grid<Char>.countAround(position: Position): Int = neighbors8(position).count { this[it] == '@' }

            fun part1(grid: Grid<Char>): Int = grid.positionsOf('@').count { grid.countAround(it) < 4 }

            fun part2(grid: MutableGrid<Char>): Int {
                var result = 0
                var wasRemoved: Boolean
                do {
                    wasRemoved = false
                    for (position in grid.positionsOf('@')) {
                        if (grid.countAround(position) < 4) {
                            grid[position] = '.'
                            wasRemoved = true
                            result++
                        }
                    }
                } while (wasRemoved)
                return result
            }
        }

        val rolls = listOf(
            "@@@@@.",
            "@@@@@.",
            "@@@@@@",
            "@@@@@.",
            ".@.@.@",
        )

        assertEquals(6, Day04().part1(rolls.toGrid()))
        assertEquals(8, Day04().part2(rolls.toMutableGrid()))
    }

    @Test
    fun factory() {
        fun fewestPresses(target: Int, transitions: List<Int>): Int =
            bfs(0) { lights -> transitions.map { lights xor it } }.getValue(target)

        assertEquals(2, fewestPresses(0b101, listOf(0b011, 0b110, 0b111)))
    }

    @Test
    fun ramRun() {
        fun List<String>.parse() = map { it.split(",").map(String::toInt).let { (x, y) -> Position(x, y) } }

        fun distance(n: Int, limit: Int, points: List<Position>): Int {
            val grid = MutableGrid(n, n) { '.' }
            for (point in points.take(limit)) grid[point] = '#'

            val steps = bfs(Position(0, 0)) { position -> grid.neighbors(position).filter { grid[it] == '.' } }
            return steps[Position(n - 1, n - 1)] ?: -1
        }

        fun part1(n: Int, limit: Int, points: List<Position>): Int = distance(n, limit, points)

        fun part2(n: Int, points: List<Position>): String {
            val blocked = partitionPoint(0, points.size) { limit -> distance(n, limit, points) != -1 }
            return points[blocked - 1].let { (x, y) -> "$x,$y" }
        }

        val bytes = listOf("1,0", "1,1", "1,2", "3,4", "3,3", "3,2", "1,3", "3,1", "2,0", "3,0", "0,3").parse()

        assertEquals(10, part1(5, 5, bytes))
        assertEquals("2,0", part2(5, bytes))
    }

    @Test
    fun reindeerMaze() {
        data class Reindeer(val position: Position, val direction: Direction)

        fun solve(input: List<String>): Pair<Int, Int> {
            val maze = input.toGrid()
            val start = Reindeer(maze.positionOf('S'), Direction.RIGHT)
            val ends = Direction.entries.map { Reindeer(maze.positionOf('E'), it) }

            fun Reindeer.moves(next: Position) = listOfNotNull(
                copy(direction = direction.turnLeft()) to 1000,
                copy(direction = direction.turnRight()) to 1000,
                (copy(position = next) to 1).takeIf { maze[next] != '#' },
            )

            val fromStart = dijkstra(start) { it.moves(it.position + it.direction) }
            val toEnd = dijkstra(starts = ends) { it.moves(it.position - it.direction) }

            val best = ends.minOf { fromStart.getValue(it) }
            val onBestPath = fromStart.keys.filter { fromStart.getValue(it) + toEnd.getValue(it) == best }
            return best to onBestPath.map { it.position }.toSet().size
        }

        // Two best paths, around the block from above and from below, and a dead end next to S
        val tiles = listOf(
            "#######",
            "#.....#",
            "#.###.#",
            "#S.##E#",
            "#.###.#",
            "#.....#",
            "#######",
        )

        assertEquals(3008 to 16, solve(tiles))
    }
}
