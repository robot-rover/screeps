package modules

import Module
import ModuleMap
import ModuleType
import kotlinx.serialization.Contextual
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

    override fun process() {
        for ((sourceId, minerCreeps) in mod_mem.minerCreeps) {
            val source = Game.getObjectById<Source>(sourceId)
            if (source == null) {
                logWarn("Source is null (id: $sourceId)", ModuleType.Eco)
                continue
            }
            val creepDefs = Birth.getScreeps(ModuleType.Eco)
            for (creepName in minerCreeps) {
                val creepDef = creepDefs[creepName]
                if (creepDef == null) {
                    logWarn("Creep Def for $creepName doesn't exist", ModuleType.Eco)
                    continue
                }
                val creep = creepDef.getCreep() ?: continue

                if (distance(creep.pos, source.pos) > 1) {
                    creep.moveTo(source.pos)
                } else {
                    val capacityLeft = creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0
                    val willHarvest = creep.body.count { it == WORK } * WorkPart.HARVEST_ENERGY
                    val hasRoom = capacityLeft >= willHarvest
                    val hasEnergy = source.energy > 0

                    if (capacityLeft >= willHarvest && source.energy > 0) {
                        val harvestCode = creep.harvest(source)
                        isCodeSuccess("harvest", harvestCode, ModuleType.Eco)
                    }
                }
            }
        }

        for (entry in mod_mem.haulerCreeps) {
            val creepName = entry.component1()
            val creepDef = Birth.getScreeps(ModuleType.Eco)[creepName]
            if (creepDef == null) {
                logWarn("Creep Def for $creepName doesn't exist", ModuleType.Eco)
                continue
            }

            val creep = creepDef.getCreep() ?: continue

            val memory = entry.component2()
            when (memory) {
                is HaulerMemory.Loading -> {
                    val targetCreep = Game.creeps[memory.target];
                }
                is HaulerMemory.Delivering -> {
                    val targetStructure = Game.getObjectById<StoreOwner>(memory.target)
                }
            }
        }
    }

    @Serializable
    class EnergyEstimate(val pos: Pos, val perTick: Double)

    @Serializable
    private sealed class HaulerMemory {
        class Delivering(val target: String): HaulerMemory()
        class Loading(val target: String): HaulerMemory()
    }

    @Serializable
    private class EcoMemory {
        val energyRequests: ModuleMap<MutableMap<String, Int>> = mutableMapOf()
        val minerCreeps: MutableMap<String, MutableList<String>> = mutableMapOf()
        val haulerCreeps: MutableMap<String, HaulerMemory> = mutableMapOf()
    }

    val energyUsed: ModuleMap<Int> = mutableMapOf()
    val energyEstimates: ModuleMap<MutableList<EnergyEstimate>> = mutableMapOf()

    fun getEstimates(type: ModuleType): MutableList<EnergyEstimate> {
        return energyEstimates.getOrPut(type) { mutableListOf() }
    }

    fun getRequests(type: ModuleType): MutableMap<String, Int> {
        return mod_mem.energyRequests.getOrPut(type) { mutableMapOf() }
    }

    private val mod_mem: EcoMemory = KotlinMemory.getModule(type) { EcoMemory() }

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }
}