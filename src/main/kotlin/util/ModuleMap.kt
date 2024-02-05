package util

import ModuleType

class ModuleMap<T>(private val default: () -> T) {
    private val data: MutableMap<ModuleType, T> = mutableMapOf()

    fun get(type: ModuleType): T {
        return data.getOrPut(type, default)
    }
}