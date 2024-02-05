package modules

import Module
import ModuleType
import screeps.api.FIND_SOURCES
import screeps.api.Game
import screeps.api.iterator
import screeps.api.values
import util.Cached
import util.KotlinMemory
import util.ModuleMap


object Eco: Module() {
    override val type: ModuleType get() = ModuleType.Eco

    private val energyRate: Cached<Double> = Cached {
        Game.rooms.values
            .filter { it.controller?.my ?: false }
            .sumOf { it.energyCapacityAvailable } / 300.0
    }

    override fun process() {
        TODO("Not yet implemented")
    }

    class EnergyRequest(val structureId: String, val amount: Int)
    private class EcoMemory {
        val energyRequests: ModuleMap<ArrayDeque<EnergyRequest>> = ModuleMap { ArrayDeque() }
        val energyUsed: ModuleMap<Int> = ModuleMap { 0 }
    }
    private val mod_mem: EcoMemory = KotlinMemory.getModule(type)

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }
}