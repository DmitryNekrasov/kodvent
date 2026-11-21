/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package kodvent.datastructures

import kodvent.grid.Position
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HeapTest {

    private data class State(val name: String, val cost: Int)

    private fun <T> Heap<T>.drain(): List<T> = List(size) { pop() }

    // Every list over {0, 1, 2} of length 0..6: with three values every arrangement of smaller, equal and larger
    // neighbors occurs.
    private fun smallInputs(): List<List<Int>> = buildList {
        for (length in 0..6) {
            var count = 1
            repeat(length) { count *= 3 }
            for (code in 0..<count) {
                var rest = code
                add(List(length) { (rest % 3).also { rest /= 3 } })
            }
        }
    }

    private fun <T> List<T>.notACollection(): Iterable<T> = asSequence().asIterable()

    @Test
    fun `a new heap is empty`() {
        val heap = Heap<Int>()

        assertEquals(0, heap.size)
        assertTrue(heap.isEmpty())
        assertFalse(heap.isNotEmpty())
    }

    @Test
    fun `pop peek and replace throw on an empty heap`() {
        val heap = Heap<Int>()

        assertFailsWith<NoSuchElementException> { heap.pop() }
        assertFailsWith<NoSuchElementException> { heap.peek() }
        assertFailsWith<NoSuchElementException> { heap.replace(1) }
        assertTrue(heap.isEmpty())
    }

    @Test
    fun `popOrNull and peekOrNull return null on an empty heap`() {
        val heap = Heap<Int>()

        assertNull(heap.popOrNull())
        assertNull(heap.peekOrNull())
    }

    @Test
    fun `popOrNull and peekOrNull return the top of a heap that is not empty`() {
        val heap = listOf(2, 1).toHeap()

        assertEquals(1, heap.peekOrNull())
        assertEquals(1, heap.popOrNull())
        assertEquals(2, heap.popOrNull())
        assertNull(heap.popOrNull())
    }

    @Test
    fun `a single element is peeked and then popped`() {
        val heap = Heap<Int>()
        heap.push(42)

        assertEquals(1, heap.size)
        assertTrue(heap.isNotEmpty())
        assertEquals(42, heap.peek())
        assertEquals(1, heap.size)
        assertEquals(42, heap.pop())
        assertTrue(heap.isEmpty())
    }

    @Test
    fun `elements come out in ascending order whatever the order they went in`() {
        val ascending = (1..100).toList()
        val inputs = listOf(ascending, ascending.reversed(), ascending.shuffled(Random(1)))

        for (input in inputs) {
            val heap = Heap<Int>()
            for (element in input) heap.push(element)

            assertEquals(ascending, heap.drain())
        }
    }

    @Test
    fun `duplicates all come out`() {
        val input = listOf(3, 1, 3, 2, 1, 3, 2, 2, 1)
        val heap = Heap<Int>()
        for (element in input) heap.push(element)

        assertEquals(input.sorted(), heap.drain())
    }

    @Test
    fun `a heap of equal elements keeps every one of them`() {
        val heap = Heap<Int>()
        repeat(50) { heap.push(7) }

        assertEquals(List(50) { 7 }, heap.drain())
    }

    @Test
    fun `reverse order makes a max-heap`() {
        val heap = Heap<Int>(reverseOrder())
        for (element in listOf(3, 9, 1, 7, 5)) heap.push(element)

        assertEquals(9, heap.peek())
        assertEquals(listOf(9, 7, 5, 3, 1), heap.drain())
    }

    @Test
    fun `elements with equal priorities are all kept`() {
        val heap = Heap<Pair<Int, String>>(compareBy { it.first })
        val input = listOf(2 to "a", 1 to "b", 2 to "c", 1 to "d", 1 to "e", 3 to "f")
        for (element in input) heap.push(element)

        val drained = heap.drain()

        assertEquals(listOf(1, 1, 1, 2, 2, 3), drained.map { it.first })
        assertEquals(input.map { it.second }.sorted(), drained.map { it.second }.sorted())
    }

    @Test
    fun `every small input drains in sorted order`() {
        for (input in smallInputs()) {
            val pushed = Heap<Int>()
            for (element in input) pushed.push(element)

            assertEquals(input.sorted(), pushed.drain(), "pushed $input")
            assertEquals(input.sorted(), input.toHeap().drain(), "toHeap of $input")
            assertEquals(input.sorted(), input.notACollection().toHeap().drain(), "toHeap of a sequence of $input")
        }
    }

    @Test
    fun `sizes around every growth of the storage work`() {
        for (n in listOf(15, 16, 17, 23, 24, 25, 36, 37, 1000)) {
            val input = (0..<n).shuffled(Random(n))
            val pushed = Heap<Int>()
            for (element in input) pushed.push(element)

            for (heap in listOf(pushed, input.toHeap(), input.notACollection().toHeap())) {
                heap.push(-1)
                heap.push(n)

                assertEquals(n + 2, heap.size, "size $n")
                assertEquals((-1..n).toList(), heap.drain(), "size $n")
            }
        }
    }

    @Test
    fun `replace on a single element returns it and keeps the new one`() {
        val heap = listOf(5).toHeap()

        assertEquals(5, heap.replace(9))
        assertEquals(listOf(9), heap.drain())
    }

    @Test
    fun `replace returns the old top and puts the new element in its place in the order`() {
        val heap = listOf(1, 5).toHeap()

        assertEquals(1, heap.replace(7))
        assertEquals(listOf(5, 7), heap.drain())
    }

    @Test
    fun `replace returns the old top even when the new element is smaller`() {
        val heap = listOf(1, 5).toHeap()

        assertEquals(1, heap.replace(0))
        assertEquals(listOf(0, 5), heap.drain())
    }

    @Test
    fun `replace keeps the size and the order of a larger heap`() {
        val heap = (1..20).toList().toHeap()

        assertEquals(1, heap.replace(11))
        assertEquals(20, heap.size)
        assertEquals(((2..20) + 11).sorted(), heap.drain())
    }

    @Test
    fun `a drained heap can be filled again`() {
        val heap = listOf(2, 1, 3).toHeap()

        assertEquals(listOf(1, 2, 3), heap.drain())
        assertFailsWith<NoSuchElementException> { heap.pop() }

        heap.push(8)
        heap.push(4)

        assertEquals(listOf(4, 8), heap.drain())
    }

    @Test
    fun `clear empties the heap and it can be used again`() {
        val heap = (1..40).toList().toHeap()

        heap.clear()

        assertEquals(0, heap.size)
        assertTrue(heap.isEmpty())
        assertNull(heap.peekOrNull())

        heap.push(6)
        heap.push(2)

        assertEquals(listOf(2, 6), heap.drain())
    }

    @Test
    fun `toHeap of an empty collection is an empty heap`() {
        val heap = emptyList<Int>().toHeap()

        assertTrue(heap.isEmpty())
        assertNull(heap.popOrNull())
    }

    @Test
    fun `toHeap orders by the given comparator`() {
        val heap = listOf("ccc", "a", "bb", "dddd").toHeap(compareByDescending { it.length })

        assertEquals(listOf("dddd", "ccc", "bb", "a"), heap.drain())
    }

    @Test
    fun `toHeap accepts an iterable that is not a collection and is longer than the first storage`() {
        val input = (0..<100).shuffled(Random(1))

        val heap = input.notACollection().toHeap()

        assertEquals((0..<100).toList(), heap.drain())
    }

    @Test
    fun `toHeap leaves its source alone and does not follow its later changes`() {
        val source = mutableListOf(3, 1, 2)

        val heap = source.toHeap()
        source[0] = -5

        assertEquals(listOf(-5, 1, 2), source)
        assertEquals(listOf(1, 2, 3), heap.drain())
    }

    @Test
    fun `toHeap builds the heap in a linear number of comparisons`() {
        val n = 4096
        var comparisons = 0

        // A descending input is the worst case. Pushing its elements one by one takes about 41,000
        // comparisons, while building the heap bottom-up never takes 2n.
        val heap = (n downTo 1).toList().toHeap { a, b ->
            comparisons++
            a.compareTo(b)
        }

        assertTrue(comparisons < 2 * n, "$comparisons comparisons")
        assertEquals((1..n).toList(), heap.drain())
    }

    @Test
    fun `a comparator that rejects the pushed element leaves the heap as it was`() {
        val heap = Heap<Int> { a, b -> if (a == 13 || b == 13) error("13 has no place") else a.compareTo(b) }
        heap.push(2)
        heap.push(1)

        assertFailsWith<IllegalStateException> { heap.push(13) }

        assertEquals(2, heap.size)
        assertEquals(listOf(1, 2), heap.drain())
    }

    @Test
    fun `a comparator that fails on the first comparison of pop or replace leaves the heap as it was`() {
        var failing = false
        val heap = Heap<Int> { a, b -> if (failing) error("no order for now") else a.compareTo(b) }
        for (element in listOf(1, 2, 3)) heap.push(element)

        failing = true
        assertFailsWith<IllegalStateException> { heap.pop() }
        assertFailsWith<IllegalStateException> { heap.replace(9) }
        failing = false

        assertEquals(3, heap.size)
        assertEquals(listOf(1, 2, 3), heap.drain())
    }

    @Test
    fun `toSortedList returns the sorted elements and leaves the heap as it was`() {
        val heap = listOf(4, 1, 3, 5, 2).toHeap()

        val sorted = heap.toSortedList()
        heap.push(0)

        assertEquals(listOf(1, 2, 3, 4, 5), sorted)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), heap.drain())
    }

    @Test
    fun `toSortedList follows the comparator`() {
        val heap = listOf(4, 1, 3).toHeap(reverseOrder())

        assertEquals(listOf(4, 3, 1), heap.toSortedList())
    }

    @Test
    fun `toString lists every element with the top first`() {
        val printed = listOf(4, 2, 5, 1, 3).toHeap().toString()

        assertTrue(printed.startsWith("[1, "), printed)
        assertEquals(listOf(1, 2, 3, 4, 5), printed.removeSurrounding("[", "]").split(", ").map { it.toInt() }.sorted())
        assertEquals("[7]", listOf(7).toHeap().toString())
        assertEquals("[]", Heap<Int>().toString())
    }

    @Test
    fun `toString does not call the comparator`() {
        var comparisons = 0
        val heap = listOf(4, 2, 5, 1, 3).toHeap { a, b ->
            comparisons++
            a.compareTo(b)
        }
        val before = comparisons

        val printed = heap.toString()

        assertEquals(before, comparisons)
        assertTrue(printed.startsWith("[1, "), printed)
    }

    @Test
    fun `nullable elements are ordered by a comparator that accepts null`() {
        val heap = Heap<String?>(nullsFirst(naturalOrder()))
        heap.push("b")
        heap.push(null)
        heap.push("a")

        assertEquals(3, heap.size)
        assertNull(heap.peek())
        assertEquals(listOf(null, "a", "b"), heap.drain())
    }

    @Test
    fun `the removed element can be ignored`() {
        val heap = listOf(4, 2, 7, 1).toHeap()

        heap.pop()
        heap.popOrNull()
        heap.replace(9)
        if (heap.size > 1) heap.pop() else heap.push(0)

        assertEquals(listOf(9), heap.drain())
    }

    @Test
    fun `a heap can be created in every documented way`() {
        val natural = Heap<Int>()
        natural.push(2)
        natural.push(1)
        assertEquals(1, natural.pop())

        val declared: Heap<Int> = Heap()
        declared.push(2)
        declared.push(1)
        assertEquals(1, declared.pop())

        val weight = intArrayOf(5, 1, 3)
        val byWeight = Heap<Int> { a, b -> weight[a].compareTo(weight[b]) }
        for (index in weight.indices) byWeight.push(index)
        assertEquals(listOf(1, 2, 0), byWeight.drain())

        val byTypedLambda = Heap { a: State, b: State -> a.cost.compareTo(b.cost) }
        byTypedLambda.push(State("far", 9))
        byTypedLambda.push(State("near", 2))
        assertEquals("near", byTypedLambda.pop().name)

        val byComparator = Heap(compareBy<State> { it.cost })
        byComparator.push(State("far", 9))
        byComparator.push(State("near", 2))
        assertEquals("near", byComparator.pop().name)

        val pairs = Heap<Pair<Int, Position>>(compareBy { it.first })
        pairs.push(4 to Position(0, 0))
        pairs.push(3 to Position(1, 1))
        assertEquals(Position(1, 1), pairs.pop().second)

        val fromDeclaredType: Heap<IndexedValue<Char>> = Heap { v1, v2 ->
            if (v1.value == v2.value) v2.index compareTo v1.index else v1.value compareTo v2.value
        }
        for (entry in "abca".withIndex()) fromDeclaredType.push(entry)
        assertEquals(listOf(3, 0, 1, 2), fromDeclaredType.drain().map { it.index })

        val converted = "abca".withIndex().toHeap(compareBy({ it.value }, { it.index }))
        assertEquals(listOf(0, 3, 1, 2), converted.drain().map { it.index })
    }

    @Test
    fun `random operations agree with a list model`() {
        val byKey = compareBy<Pair<Int, Int>> { it.first }

        for (seed in 0..<16) {
            val random = Random(seed)
            var nextId = 0
            fun newEntry(): Pair<Int, Int> = random.nextInt(8) to nextId++

            // Keys repeat and ids do not: the heap is checked by key, where ties may come out in any order,
            // and the model drops exactly the entry the heap returned.
            val model = MutableList(random.nextInt(41)) { newEntry() }
            var heap = model.toHeap(byKey)

            repeat(1500) { step ->
                val at = "seed $seed step $step"
                val pushChance = if (step / 250 % 2 == 0) 550 else 150
                val roll = random.nextInt(1000)
                when {
                    roll < pushChance -> {
                        val entry = newEntry()
                        heap.push(entry)
                        model.add(entry)
                    }

                    roll < 750 -> if (model.isEmpty()) {
                        assertNull(heap.peekOrNull(), at)
                        assertNull(heap.popOrNull(), at)
                        assertFailsWith<NoSuchElementException>(at) { heap.pop() }
                    } else {
                        val top = heap.peek()
                        val popped = heap.pop()
                        assertSame(top, popped, at)
                        assertEquals(model.minOf { it.first }, popped.first, at)
                        assertTrue(model.remove(popped), at)
                    }

                    roll < 850 -> if (model.isNotEmpty()) {
                        val entry = newEntry()
                        val old = heap.replace(entry)
                        assertEquals(model.minOf { it.first }, old.first, at)
                        assertTrue(model.remove(old), at)
                        model.add(entry)
                    }

                    roll < 930 -> {
                        val sorted = heap.toSortedList()
                        assertEquals(model.map { it.first }.sorted(), sorted.map { it.first }, at)
                        assertEquals(model.map { it.second }.sorted(), sorted.map { it.second }.sorted(), at)
                    }

                    roll < 995 -> {
                        val shuffled = model.shuffled(random)
                        heap = (if (random.nextBoolean()) shuffled else shuffled.notACollection()).toHeap(byKey)
                    }

                    else -> {
                        heap.clear()
                        model.clear()
                    }
                }
                assertEquals(model.size, heap.size, at)
                assertEquals(model.isEmpty(), heap.isEmpty(), at)
            }

            val drained = heap.drain()
            assertEquals(model.map { it.first }.sorted(), drained.map { it.first }, "seed $seed")
            assertEquals(model.map { it.second }.sorted(), drained.map { it.second }.sorted(), "seed $seed")
        }
    }

    @Test
    fun `dijkstra with distance entries is right when it stops at the target`() {
        // On this graph a heap of vertex numbers ordered by a distance array that keeps changing pops R at
        // distance 5 while X at distance 2 is still inside.
        val s = 0
        val u = 1
        val r = 2
        val y = 3
        val x = 4
        val t = 5
        val edges = listOf(
            listOf(u to 1, r to 5, y to 7, x to 6),
            listOf(x to 1),
            listOf(t to 1),
            emptyList(),
            listOf(r to 1),
            emptyList(),
        )

        fun distanceTo(target: Int): Int {
            val distance = IntArray(edges.size) { Int.MAX_VALUE }
            val heap = Heap<Pair<Int, Int>>(compareBy { it.first })
            distance[s] = 0
            heap.push(0 to s)
            while (heap.isNotEmpty()) {
                val (d, vertex) = heap.pop()
                if (d > distance[vertex]) continue
                if (vertex == target) return d
                for ((next, weight) in edges[vertex]) {
                    if (d + weight < distance[next]) {
                        distance[next] = d + weight
                        heap.push(d + weight to next)
                    }
                }
            }
            error("vertex $target is unreachable")
        }

        assertEquals(listOf(0, 1, 3, 7, 2, 4), listOf(s, u, r, y, x, t).map { distanceTo(it) })
    }
}
