package util

class Cached<T>(val calcFunc: () -> T) {
    private var value: T = calcFunc()

    fun get(): T {
        return value
    }

    fun recalculate() {
        value = calcFunc()
    }
}