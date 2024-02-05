package util

import ModuleType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import screeps.api.*

@OptIn(ExperimentalSerializationApi::class)
object KotlinMemory {
    inline fun <reified T> getModule(module: ModuleType): T {
        return Json.decodeFromDynamic<T>(global.Memory[module.toString()])
    }

    inline fun <reified T> setModule(module: ModuleType, memory: T) {
        global.Memory[module.toString()] = Json.encodeToDynamic<T>(memory)
    }
}