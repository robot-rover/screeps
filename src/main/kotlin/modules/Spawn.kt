package modules

import screeps.api.BodyPartConstant
import screeps.api.Game;
import screeps.api.GlobalMemory
import screeps.api.SpawnMemory
import screeps.api.global.Memory
import screeps.utils.memory.memory

data class ScreepRequest(val name: String, val body: Array<BodyPartConstant>)

private val GlobalMemory.spawn_queue: ArrayDeque<ScreepRequest> by memory { ArrayDeque() }
private val SpawnMemory.in_progress: ScreepRequest? by memory()

object Spawn {

    fun requestScreep(req: ScreepRequest) {
        Memory.spawn_queue.add(req)
    }

    fun process() {

    }
}

private fun Spawn.next_state()