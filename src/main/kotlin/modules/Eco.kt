package modules

import MODULES
import Module
import ModuleType
import kotlinx.serialization.Serializable
import screeps.api.*
import util.*


object Eco: Module() {
    override val type: ModuleType get() = ModuleType.Eco

    private val energyRate: Cached<Double> = Cached {
        Game.rooms.values
            .filter { it.controller?.my ?: false }
            .sumOf { it.energyCapacityAvailable } / 300.0
    }

    private fun nextRequest(it: Iterator<EcoRequest>): EcoRequest? {
        while (it.hasNext()) {
            val ecoRequest = it.next()
            if (ecoRequest.claims.sumOf { it.second } < ecoRequest.amount) {
                return ecoRequest
            }
        }

        return null
    }

    override fun init() {
        // TODO: When to do this
        mod_mem.haulerQueue.body.set(minerBody)
        mod_mem.haulerQueue.wantQuantity.set(3)
    }

    override fun process() {
        // TODO: Calc the actual quantity
        // TODO: When to reset sourceInfos
        val sourceMemLive: List<Triple<Source, SourceMemory, SourceData>> = sources.get().entries.mapNotNull { (sourceId, info) ->
            val source = Game.getObjectById<Source>(sourceId)
            if (source == null) {
                logWarn("sourceId is null: $sourceId", ModuleType.Eco)
                return@mapNotNull null
            }

            val mem = mod_mem.sourceMemory.getOrPut(sourceId) { SourceMemory(Birth.BirthQueue(), mutableListOf()) }

            Triple(source, mem, info)
        }

        val haulersWithTasks = mutableSetOf<String>()

        for (module in MODULES) {
            for (request in module.ecoSequence()) {
                val target = Game.getObjectById<StoreOwner>(request.target)

                if (target == null) {
                    logWarn("target ${request.target} of ${module.type} is null")
                    continue
                }

                request.claims.removeAll { (creepId, energyAmount) ->
                    val creep = Game.creeps[creepId] ?: return@removeAll true

                    if (creep.store.getUsedCapacity(RESOURCE_ENERGY) < energyAmount) {
                        return@removeAll true
                    }

                    haulersWithTasks.add(creepId)

                    if (distance(creep.pos, target.pos) <= 1) {
                        val moveToCode = creep.moveTo(target, opts = options { visualizePathStyle = options { } } )
                        isCodeSuccess("moveTo", moveToCode, ModuleType.Eco) // TODO Handle fatigue
                        false
                    } else {
                        creep.transfer(target, RESOURCE_ENERGY, energyAmount)
                        true
                    }

                }
            }
        }

        for ((source, sourceMem, sourceInfo) in sourceMemLive) {
            // TODO: When to do this
            sourceMem.miners.body.set(minerBody)
            sourceMem.miners.wantQuantity.set(1)
            // TODO: Traffic Jam
            // TODO: Memorize position so we don't shift
            for ((idx, miner) in sourceMem.miners.getCreeps().withIndex()) {
                if (idx < sourceInfo.harvestPos.size) {
                    val targetPos = sourceInfo.harvestPos[idx]
                    if (miner.pos.toPos() != targetPos || miner.pos.roomName != source.room.name) {
                        val moveToCode = miner.moveTo(targetPos.toRoomPos(source.room.name), opts = options { visualizePathStyle = options { } })
                        isCodeSuccess("moveTo", moveToCode, ModuleType.Eco)
                    } else {
                        val tickHarvest = WorkPart.HARVEST_ENERGY * miner.body.count { it == WORK }
                        if (miner.store.getFreeCapacity(RESOURCE_ENERGY) >= tickHarvest) {
                            miner.harvest(source)
                        }
                    }
                } else if (distance(miner.pos, source.pos) > 5) {
                    val moveToCode = miner.moveTo(source.pos)
                    isCodeSuccess("moveTo", moveToCode, ModuleType.Eco)
                }
            }
        }

        // TODO: Priority
        val requestIterator = MODULES.asSequence().flatMap { it.ecoSequence() }.asIterable().iterator()
        var currentRequest = nextRequest(requestIterator)

        for (creep in mod_mem.haulerQueue.getCreeps()) {
            if (haulersWithTasks.contains(creep.name)) continue

            val energyHas = creep.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0
            if (energyHas > 0) {
                if (currentRequest != null) {
                    currentRequest.claims.add(creep.name to energyHas)
                    if (currentRequest.amount <= currentRequest.claims.sumOf { it.second }) {
                        currentRequest = nextRequest(requestIterator)
                    }
                }
            } else {
                val sourceMem = mod_mem.sourceMemory.entries.minBy { it.value.claims.size }.value
                sourceMem.claims.add(creep.name)
            }
        }

    }

    @Serializable
    class EcoRequest private constructor(val target: String, val amount: Int, val claims: MutableList<Pair<String, Int>>) {
        constructor(target: String, amount: Int): this(target, amount, mutableListOf())
    }

    @Serializable
    class SourceMemory(val miners: Birth.BirthQueue, val claims: MutableList<String>)

    val haulerBody: Array<BodyPartConstant> = arrayOf(CARRY, CARRY, CARRY, MOVE, MOVE, MOVE)
    val minerBody: Array<BodyPartConstant> = arrayOf(CARRY, WORK, WORK, MOVE)
    @Serializable
    private class EcoMemory {
        val sourceMemory: MutableMap<String, SourceMemory> = mutableMapOf()
        val haulerQueue: Birth.BirthQueue = Birth.BirthQueue()
    }

    private val mod_mem: EcoMemory = KotlinMemory.getModule(type) { EcoMemory() }

    private class SourceData(source: Source) {
         val harvestPos: List<Pos>
         init {
            val sourcePos = source.pos
            val terrain = source.room.getTerrain()
            val offsets = arrayOf(-1, 0, 1)
            val harvestPosMut: MutableList<Pos> = mutableListOf()
            for (xOffset in offsets) {
                for (yOffset in offsets) {
                    val x = sourcePos.x + xOffset
                    val y = sourcePos.y + yOffset
                    if (x in 0..49 && y in 0..49 && !(xOffset == 0 && yOffset == 0) && terrain[x, y] == TERRAIN_MASK_NONE) {
                        harvestPosMut.add(makePos(x, y))
                    }
                }
            }
            harvestPos = harvestPosMut
        }
    }

    private val sources: Cached<Map<String, SourceData>> = Cached {
        Game.rooms.values.asSequence()
            .filter { it.controller?.my ?: false }
            .flatMap { it.find(FIND_SOURCES).asSequence() }
            .associate { it.id to SourceData(it) }
    }

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }

    override fun getCreepQueues(): Sequence<String> {
        return sequenceOf("hauler") + mod_mem.sourceMemory.keys.asSequence()
    }
    override fun getCreeps(queueName: String): Birth.BirthQueue? {
        if (queueName == "hauler") {
            return mod_mem.haulerQueue
        } else {
            return mod_mem.sourceMemory[queueName]?.miners
        }
    }
}