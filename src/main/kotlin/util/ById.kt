package util

import kotlinx.serialization.Serializable
import screeps.api.Game
import screeps.api.Identifiable

@Serializable
value class ById<T: Identifiable> private constructor(private val id: String) {
    constructor(target: T): this(target.id)

    fun get(): T? {
        return Game.getObjectById(id)
    }

}