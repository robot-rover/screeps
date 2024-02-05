package modules

import Module
import ModuleType
import screeps.api.*
import util.Cached
import util.KotlinMemory
import util.ModuleMap


object Birth: Module() {

    class CreepRequest(val name: String, val body: Array<BodyPartConstant>)

    private class BirthMemory {
        val requestQueues: ModuleMap<ArrayDeque<CreepRequest>> = ModuleMap { ArrayDeque() }
        val spawnTask: MutableMap<String, Pair<ModuleType, String>> = mutableMapOf()
    }

    private val mod_mem: BirthMemory = KotlinMemory.getModule(type)

    fun requestScreep(type: ModuleType, req: CreepRequest) {
        mod_mem.requestQueues.get(type).add(req)
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