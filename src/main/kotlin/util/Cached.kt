package util

class Cached<T>(val calcFunc: () -> T) {
    var value: T? = null

    fun get(): T {
        val valueLocal = value
        if (valueLocal == null) {
            val nonNullValue = calcFunc()
            value = nonNullValue
            return nonNullValue
        } else {
            return valueLocal
        }
    }

    fun invalidate() {
        value = null
    }
}