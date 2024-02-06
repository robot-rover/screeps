package modules

import Module
import ModuleMap
import ModuleType
import kotlinx.serialization.Serializable
import screeps.api.*
import util.Cached
import util.KotlinMemory


object Birth: Module() {

    @Serializable
    class CreepRequest(val name: String, val body: Array<BodyPartConstant>)

    @Serializable
    private class BirthMemory {
        val requestQueues: ModuleMap<MutableList<CreepRequest>> = mutableMapOf()
        val spawnTask: MutableMap<String, Pair<ModuleType, String>> = mutableMapOf()
    }

    private val mod_mem: BirthMemory = KotlinMemory.getModule(type) { BirthMemory() }

    fun requestScreep(type: ModuleType, req: CreepRequest) {
        mod_mem.requestQueues.getOrPut(type) { mutableListOf() }.add(req)
    }

    override fun process() {
        TODO()
    }

    val maxBodySize: Cached<Int> = Cached {
        Game.spawns.values.maxOf {
            it.room.energyCapacityAvailable
        }
    }

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }

    override val type: ModuleType
        get() = ModuleType.Birth

}