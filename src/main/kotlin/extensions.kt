@file:Suppress("RedundantVisibilityModifier")

public fun <T> MutableMap<T, Int>.increment(key: T) {
    this[key] = (this[key] ?: 0) + 1
}

public fun <T> MutableMap<T, Int>.decrement(key: T): Boolean {
    val count = this[key] ?: return false
    if (count > 1) {
        this[key] = count - 1
    } else {
        remove(key)
    }
    return true
}
