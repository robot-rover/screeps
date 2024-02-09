package modules

import Module
import ModuleMap
import ModuleType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import screeps.api.*
import screeps.api.structures.StructureSpawn
import util.*


object Birth : Module() {

    @Serializable
    class BirthQueue(private var _body: Array<BodyPartConstant>, var wantQuantity: Int, val creeps: MutableList<String>) {
        var body: Array<BodyPartConstant>
            get() = _body
            set(value) {
                _body = value
                spawnCost.recalculate()
            }

        @Transient
        val spawnCost: Cached<Int> = Cached {
            body.sumOf { bodyPartEnergy(it) }
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
    class CreepDefinition(val body: Array<BodyPartConstant>) {
        @Transient
        val spawnCost: Int = body.sumOf { bodyPartEnergy(it) }
        private var currentId: String? = null
        private var nextId: String? = null

        fun getCreep(): Creep? {
            val currentLocal = currentId
            if (currentLocal != null) {
                val currentCreep = Game.creeps[currentLocal]
                if (currentCreep != null) {
                    return currentCreep
                }
            }

            if (nextId != null) {
                currentId = nextId
                nextId = null
                return getCreep()
            } else {
                return null
            }
        }

        fun addId(newId: String) {
            getCreep()
            if (nextId != null) {
                logWarn("Overwriting secondary creep id ($nextId)", ModuleType.Birth)
            }
            nextId = newId
        }
    }

    @Serializable
    private class CreepLink(val gameId: String, val requestName: String, val requester: ModuleType)

    @Serializable
    private class BirthMemory {
        val existScreeps: ModuleMap<MutableMap<String, CreepDefinition>> = mutableMapOf()
        val spawnTasks: MutableMap<String, CreepLink> = mutableMapOf()
    }

    private fun genCreepId(request: CreepDefinition): String {
        TODO("Write genCreepName Logic")
    }

    private val energyEstimate: Root<Double> = Root {
        energyEstimateModule.sumOf { it.get() }
    }

    private val energyEstimateModule: List<Chain<Double>> = ModuleType.entries.map { modType ->
        Chain(energyEstimate) {
            (mod_mem.existScreeps[modType]?.values?.sumOf { it.spawnCost }?.toDouble() ?: 0.0) / CREEP_LIFETIME
        }
    }.toList()

    private val mod_mem: BirthMemory = KotlinMemory.getModule(type) { BirthMemory() }

    fun getScreeps(type: ModuleType): MutableMap<String, CreepDefinition> {
        return mod_mem.existScreeps.getOrPut(type) { mutableMapOf() }
    }

    fun setScreepsDirty(type: ModuleType) {
        energyEstimateModule[type.ordinal].setDirty()
    }

    override fun process() {
        mod_mem.spawnTasks.entries.removeAll { (spawnId, creepLink) ->
            val creep = Game.creeps[creepLink.gameId]
            if (creep == null) {
                // Spawn hasn't started spawning yet
                // TODO("Check on energy requests")
                val spawn = Game.getObjectById<StructureSpawn>(spawnId)
                if (spawn == null) {
                    logWarn("Spawn no longer exists (id: $spawnId)", ModuleType.Birth)
                    return@removeAll true
                }

                // TODO: Even out the spawning (round robin)
                val creepDef = mod_mem.existScreeps.getOrPut(creepLink.requester) { mutableMapOf() }[creepLink.requestName]
                if (creepDef == null) {
                    // Request was removed
                    return@removeAll true
                }

                if (spawn.room.energyCapacityAvailable >= creepDef.spawnCost) {
                    // Enough energy, attempt to spawn
                    val result = spawn.spawnCreep(creepDef.body, creepLink.gameId)

                    // Remove if there is an error
                    !isCodeSuccess("spawnCreep", result, ModuleType.Birth)
                } else {
                    // Still not enough energy
                    false
                }
            } else if (creep.spawning) {
                // Spawning is in progress
                false
            } else {
                // Spawn is done!
                val requestedCreep = mod_mem.existScreeps.getOrPut(creepLink.requester) { mutableMapOf() }[creepLink.requestName]
                if (requestedCreep == null) {
                    logWarn("Created a creep with no request", ModuleType.Birth)
                } else {
                    requestedCreep.addId(creepLink.gameId)
                }
                true
            }
        }

        val openSpawns =
            Game.spawns.entries.filter { (spawnId, spawn) -> spawn.spawning == null && spawnId !in mod_mem.spawnTasks }
        val creepsToSpawn = mod_mem.existScreeps.entries.asSequence()
            .flatMap { (modType, creepDefs) -> creepDefs.asIterable().map { (creepName, creepDef) -> Triple(modType, creepName, creepDef) } }
            .filter { (_, creepName, creepDef) -> creepDef.getCreep() == null }.take(openSpawns.size).toList()

        openSpawns.zip(creepsToSpawn) { (spawnId, _), (modType, creepName, creepDef) ->
            val gameId = genCreepId(creepDef)
            mod_mem.spawnTasks.put(spawnId, CreepLink(gameId, creepName, modType))
        }

        if (energyEstimate.checkDirty()) {
            TODO("Communicate w/ Energy")
        }

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