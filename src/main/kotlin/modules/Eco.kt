package modules

import Module
import ModuleMap
import ModuleType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import screeps.api.Game
import screeps.api.values
import util.Cached
import util.KotlinMemory


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

    @Serializable
    class EnergyRequest(val structureId: String, val amount: Int)

    @Serializable
    private class EcoMemory {
        val energyRequests: ModuleMap<MutableList<EnergyRequest>> = mutableMapOf()
        val energyUsed: ModuleMap<Int> = mutableMapOf()
    }
    private val mod_mem: EcoMemory = KotlinMemory.getModule(type) { EcoMemory() }

    override fun commitMemory() {
        KotlinMemory.setModule(type, mod_mem)
    }
}