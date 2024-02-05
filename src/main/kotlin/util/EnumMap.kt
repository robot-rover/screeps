package util

class EnumMap<K: Enum<K>, V>(private val default: () -> V) {
    private val data: MutableMap<K, V> = mutableMapOf()


    fun set(key: K, value: V) {
        data[key] = value
    }
    fun get(key: K): V {
        return data.getOrPut(key, default)
    }
}