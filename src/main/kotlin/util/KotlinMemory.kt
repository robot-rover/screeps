package util

import ModuleType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import screeps.api.*

@OptIn(ExperimentalSerializationApi::class)
object KotlinMemory {
    inline fun <reified T> getModule(module: ModuleType, default: () -> T): T {
        return Json.decodeFromDynamic<T>(Memory[module.toString()] ?: default())
    }

    inline fun <reified T> setModule(module: ModuleType, memory: T) {
        Memory[module.toString()] = Json.encodeToDynamic<T>(memory)
    }
}