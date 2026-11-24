/*
 * Copyright 2025-2026 Dmitry Nekrasov and kodvent library contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

@file:Suppress("RedundantVisibilityModifier")

package kodvent.datastructures

/**
 * A [binary heap](https://en.wikipedia.org/wiki/Binary_heap), the usual
 * [priority queue](https://en.wikipedia.org/wiki/Priority_queue): its top, which [peek] returns and [pop]
 * removes, is the smallest element by the comparator, so `Heap<Int>(reverseOrder())` is a max-heap. Equal
 * elements come out in no particular order.
 *
 * [push], [pop] and [replace] are O(log n), [peek] is O(1), and `toHeap()` builds a heap in O(n). For
 * shortest paths there is [kodvent.graph.dijkstra].
 *
 * A heap is not a collection: [toSortedList] is the way to see its elements, and two heaps are equal only
 * if they are the same object.
 *
 * @constructor Creates an empty heap ordered by [comparator]; for the natural order write `Heap<Int>()`.
 * @param comparator must be a total order in which the place of an element does not change while the
 * element is in the heap. A comparator that reads a distance array the caller keeps lowering breaks this:
 * push `(distance, state)` pairs instead and skip the stale ones on [pop].
 *
 * @sample samples.HeapSamples.basicUsage
 * @sample samples.HeapSamples.customOrder
 * @sample samples.HeapSamples.dijkstraOnGrid
 */
public class Heap<T>(private val comparator: Comparator<in T>) {
    private var elements = arrayOfNulls<Any?>(INITIAL_CAPACITY)
    private var _size = 0

    /**
     * The number of elements. O(1).
     *
     * @sample samples.HeapSamples.basicUsage
     */
    public val size: Int
        get() = _size

    /**
     * Returns `true` if this heap has no elements. O(1).
     *
     * @sample samples.HeapSamples.basicUsage
     */
    public fun isEmpty(): Boolean = _size == 0

    /**
     * Returns `true` if this heap has at least one element. O(1).
     *
     * @sample samples.HeapSamples.basicUsage
     */
    public fun isNotEmpty(): Boolean = _size != 0

    /**
     * Adds [element]. O(log n) amortized.
     *
     * @sample samples.HeapSamples.basicUsage
     */
    public fun push(element: T) {
        if (_size == elements.size) grow(_size + 1)
        // The size changes last: a comparator that throws on its first call leaves the heap as it was.
        siftUp(_size, element)
        _size++
    }

    /**
     * Removes and returns the top. O(log n).
     *
     * @throws NoSuchElementException if this heap is empty
     *
     * @sample samples.HeapSamples.basicUsage
     */
    @IgnorableReturnValue
    public fun pop(): T {
        val top = peek()
        val size = _size - 1
        val last = at(size)
        if (size > 0) siftDown(0, last, size)
        elements[size] = null
        _size = size
        return top
    }

    /**
     * Removes and returns the top, or returns `null` if this heap is empty. O(log n).
     *
     * @sample samples.HeapSamples.orNullVariants
     */
    @IgnorableReturnValue
    public fun popOrNull(): T? = if (_size == 0) null else pop()

    /**
     * Returns the top without removing it. O(1).
     *
     * @throws NoSuchElementException if this heap is empty
     *
     * @sample samples.HeapSamples.basicUsage
     */
    public fun peek(): T {
        if (_size == 0) throw NoSuchElementException("Heap is empty")
        return at(0)
    }

    /**
     * Returns the top without removing it, or `null` if this heap is empty. O(1).
     *
     * @sample samples.HeapSamples.orNullVariants
     */
    public fun peekOrNull(): T? = if (_size == 0) null else at(0)

    /**
     * Removes the top and adds [element] in one step, which is cheaper than [pop] followed by [push].
     * Returns the removed top, even when [element] is smaller than it. O(log n).
     *
     * @throws NoSuchElementException if this heap is empty
     *
     * @sample samples.HeapSamples.replaceKeepsTheLargest
     */
    @IgnorableReturnValue
    public fun replace(element: T): T {
        val top = peek()
        siftDown(0, element, _size)
        return top
    }

    /**
     * Removes all elements. O(n).
     *
     * @sample samples.HeapSamples.clearAndReuse
     */
    public fun clear() {
        elements.fill(null, 0, _size)
        _size = 0
    }

    /**
     * Returns a new list of the elements sorted by the comparator; this heap does not change. The order of
     * equal elements is unspecified and may differ from the order in which [pop] returns them. O(n log n).
     *
     * @sample samples.HeapSamples.sortedSnapshot
     */
    public fun toSortedList(): List<T> {
        val result = MutableList(_size) { at(it) }
        result.sortWith(comparator)
        return result
    }

    /**
     * Returns the elements in the format of a list, as in `[1, 3, 2]`: the top first, the rest in no
     * particular order. It does not call the comparator; for sorted output print [toSortedList]. O(n).
     *
     * @sample samples.HeapSamples.sortedSnapshot
     */
    override fun toString(): String = List(_size) { at(it) }.toString()

    internal fun heapify(source: Iterable<T>) {
        if (source is Collection<T>) grow(_size + source.size)
        for (element in source) {
            if (_size == elements.size) grow(_size + 1)
            elements[_size++] = element
        }
        for (index in (_size ushr 1) - 1 downTo 0) siftDown(index, at(index), _size)
    }

    private fun at(index: Int): T = elements.at(index)

    @Suppress("UNCHECKED_CAST")
    private fun Array<Any?>.at(index: Int): T = this[index] as T

    private fun grow(minCapacity: Int) {
        if (minCapacity <= elements.size) return
        elements = elements.copyOf(maxOf(minCapacity, elements.size + (elements.size shr 1)))
    }

    // Both sift loops copy the fields into locals first: measured against the same loops on the fields, that
    // made push and pop with a lambda comparator about 6% faster on the JVM.
    private fun siftUp(start: Int, element: T) {
        val elements = elements
        val comparator = comparator
        var index = start
        while (index > 0) {
            val parentIndex = (index - 1) ushr 1
            val parent = elements.at(parentIndex)
            if (comparator.compare(element, parent) >= 0) break
            elements[index] = parent
            index = parentIndex
        }
        elements[index] = element
    }

    private fun siftDown(start: Int, element: T, size: Int) {
        val elements = elements
        val comparator = comparator
        var index = start
        val firstLeaf = size ushr 1
        while (index < firstLeaf) {
            var childIndex = 2 * index + 1
            var child = elements.at(childIndex)
            val rightIndex = childIndex + 1
            if (rightIndex < size) {
                val right = elements.at(rightIndex)
                if (comparator.compare(child, right) > 0) {
                    childIndex = rightIndex
                    child = right
                }
            }
            if (comparator.compare(element, child) <= 0) break
            elements[index] = child
            index = childIndex
        }
        elements[index] = element
    }
}

/**
 * Creates an empty heap with the natural order of its elements, smallest on top, as in `Heap<Int>()`.
 *
 * @sample samples.HeapSamples.basicUsage
 */
@Suppress("FunctionName")
public fun <T : Comparable<T>> Heap(): Heap<T> = Heap(naturalOrder())

/**
 * Returns a heap of these elements in their natural order, smallest on top. O(n).
 *
 * @sample samples.HeapSamples.buildFromCollection
 */
public fun <T : Comparable<T>> Iterable<T>.toHeap(): Heap<T> = toHeap(naturalOrder())

/**
 * Returns a heap of these elements ordered by [comparator], which must follow the rules the [Heap]
 * constructor states for it. O(n).
 *
 * @sample samples.HeapSamples.buildWithComparator
 */
public fun <T> Iterable<T>.toHeap(comparator: Comparator<in T>): Heap<T> {
    val heap = Heap<T>(comparator)
    heap.heapify(this)
    return heap
}

private const val INITIAL_CAPACITY = 16
