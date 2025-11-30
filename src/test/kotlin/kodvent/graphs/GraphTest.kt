package kodvent.graphs

import kotlin.test.Test

class GraphTest {

    @Test
    fun foo() {
        val graph = Graph<Int>()
        graph.addEdge(1, 2)
        graph.addEdge(2, 3)
        graph.addEdge(3, 4)
        graph.addEdge(4, 1)
        graph.addEdge(1, 5)
        graph.addEdge(5, 20)
        println(graph.size)
    }
}
