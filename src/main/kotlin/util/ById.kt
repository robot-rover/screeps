package util

import kotlinx.serialization.Serializable
import screeps.api.Creep
import screeps.api.Game
import screeps.api.Identifiable
import screeps.api.get
import screeps.api.structures.StructureSpawn

@Serializable
value class ById<T: Identifiable> private constructor(private val id: String) {
    constructor(target: T): this(target.id)

    fun get(): T? {
        return Game.getObjectById(id)
    }
}

@Serializable
value class CreepByName private constructor(private val name: String) {
    constructor(target: Creep): this(target.name)

    fun get(): Creep? {
        return Game.creeps[name]
    }
}

@Serializable
value class SpawnByName private constructor(private val name: String) {
    constructor(target: StructureSpawn): this(target.name)

    fun get(): StructureSpawn? {
        return Game.spawns[name]
    }
}
