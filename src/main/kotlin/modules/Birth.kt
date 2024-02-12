package modules

import MODULES
import Module
import ModuleType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import screeps.api.*
import screeps.api.structures.StructureSpawn
import util.*


object Birth : Module() {

    @Serializable
    class BirthQueue private constructor(val creeps: MutableList<String>, var lastSpawn: Int) {
        constructor(): this(mutableListOf(), 0)

        @Transient
        val body: Leaf<Array<BodyPartConstant>> = Leaf(arrayOf())

        @Transient
        val wantQuantity: Leaf<Int> = Leaf(0)

        @Transient
        val spawnCost: Chain<Int> = Chain(arrayOf(body)) {
            body.get().sumOf { bodyPartEnergy(it) }
        }


        @Transient
        val energyEstimate: Root<Int> = Root(arrayOf(spawnCost, wantQuantity)) {
            spawnCost.get() * wantQuantity.get() / CREEP_LIFETIME
        }

        fun getCreeps(): MutableList<Creep> {
            val creepList: MutableList<Creep> = mutableListOf()
            creeps.removeAll {
                val creep = Game.creeps[it]
                if (creep == null) {
                    true
                } else {
                    creepList.add(creep)
                    false
                }
            }

            return creepList
        }
    }

    @Serializable
    private class CreepLink private constructor(val creepName: String, val queueName: String, val requester: ModuleType, val ecoRequest: Eco.EcoRequest) {
        companion object {
            fun makeCreepLink(queuePair: Pair<String, BirthQueue>, requester: ModuleType, spawn: StructureSpawn): CreepLink {
                // TODO inefficient
                val (queueName, queue) = queuePair
                val name = generateSequence(0) { it + 1 }.map { "${requester}_$queueName$it" }.first { !queue.creeps.contains(it) }
                return CreepLink(name, queueName, requester, Eco.EcoRequest(spawn.id, queue.spawnCost.get()))
            }
        }
    }

    @Serializable
    private class BirthMemory {
        val spawnTasks: MutableMap<String, CreepLink> = mutableMapOf()
    }

//    private val energyEstimate: Root<Double> = Root {
//        energyEstimateModule.sumOf { it.get() }
//    }

    private val mod_mem: BirthMemory = KotlinMemory.getModule(type) { BirthMemory() }

    private fun getRequest(module: ModuleType): Pair<String, BirthQueue>? {
        return MODULES[module.ordinal].creepSequence().filter { (_, queue) -> queue.creeps.size < queue.wantQuantity.get() }.minByOrNull { (_, queue) -> queue.lastSpawn }
    }

    override fun process() {
        // TODO: Support more modules (not just eco)

        mod_mem.spawnTasks.entries.removeAll { (spawnName, creepLink) ->
            val creep = Game.creeps[creepLink.creepName]
            if (creep == null) {
                // Spawn hasn't started spawning yet
                // TODO("Check on energy requests")
                val spawn = Game.spawns[spawnName]
                if (spawn == null) {
                    logWarn("Spawn no longer exists (id: $spawnName)")
                    return@removeAll true
                }

                // TODO: Even out the spawning (round robin)
                val creepQueue = MODULES[creepLink.requester.ordinal].getCreeps(creepLink.queueName)
                if (creepQueue == null) {
                    logWarn("Creep Queue no longer exists (id: ${creepLink.requester}/${creepLink.queueName})")
                    return@removeAll true
                }

                if (spawn.room.energyAvailable >= creepQueue.spawnCost.get()) {
                    jsprint("Spawning ${creepLink.creepName}")
                    // Enough energy, attempt to spawn
                    // Remove if there is an error
                    !spawn.spawnCreep(creepQueue.body.get(), creepLink.creepName).isCodeSuccess("spawnCreep")
                } else {
                    // Still not enough energy
                    false
                }
            } else if (creep.spawning) {
                // Spawning is in progress
                false
            } else {
                // Spawn is done!
                jsprint("Creep ${creepLink.creepName} finished spawning")
                val queue = MODULES[creepLink.requester.ordinal].getCreeps(creepLink.queueName)
                if (queue != null) {
                    queue.creeps.add(creep.name)
                } else {
                    logWarn("Creep Queue no longer exists (id: ${creepLink.requester}/${creepLink.queueName})")
                }
                true
            }
        }

        Game.spawns.entries
            .filter { (spawnId, spawn) -> spawn.spawning == null && spawnId !in mod_mem.spawnTasks }
            .forEach { (spawnId, spawn) ->
                val pair = getRequest(ModuleType.Eco) ?: return@forEach
                pair.second.lastSpawn = Game.time
                val link = CreepLink.makeCreepLink(pair, ModuleType.Eco, spawn)
                jsprint("Creating new spawn task: ${link.creepName}")
                mod_mem.spawnTasks[spawnId] = link
        }

    }

//    val maxBodySize: Cached<Int> = Cached {
//        Game.spawns.values.maxOf {
//            it.room.energyCapacityAvailable
//        }
//    }

    override fun ecoSequence(): Sequence<Eco.EcoRequest> {
        return mod_mem.spawnTasks.values.map { it.ecoRequest }.asSequence()
    }

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }

    override val type: ModuleType
        get() = ModuleType.Birth

}