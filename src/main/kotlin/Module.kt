import kotlinx.serialization.internal.throwMissingFieldException
import modules.Birth
import modules.Eco

abstract class Module {
    abstract fun commitMemory()
    abstract val type: ModuleType
    abstract fun process()

    fun init() {}

    fun getCreepQueues(): Sequence<String> = emptySequence()
    fun getCreeps(queueName: String): Birth.BirthQueue = throw IllegalArgumentException("$queueName is not a valid creep queue")
    fun creepSequence(): Sequence<Pair<String, Birth.BirthQueue>> = this.getCreepQueues().map { it to this.getCreeps(it) }

    fun getEcoReqs(): Sequence<String> = emptySequence()
    fun getEco(reqName: String): Eco.EcoRequest = throw IllegalArgumentException("$reqName is not a valid eco request")
    fun ecoSequence(): Sequence<Pair<String, Eco.EcoRequest>> = this.getEcoReqs().map { it to this.getEco(it) }
}

typealias ModuleMap<T> = MutableMap<ModuleType, T>

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ModuleType {
    Eco, Birth
}

private fun genModules(): Array<Module> {
    val allModules = arrayOf(modules.Eco, modules.Birth)

    if (allModules.size != ModuleType.entries.size) {
        throw RuntimeException("MODULES order is incorrect")
    }

    allModules.iterator().withIndex().forEach {
        if(it.index != it.value.type.ordinal) {
            throw RuntimeException("MODULES order is incorrect")
        }
    }

    return allModules
}

val MODULES: Array<Module> = genModules()


val PRIORITY: Array<Array<Pair<ModuleType, Double>>> = arrayOf(
    arrayOf(Pair(ModuleType.Eco, 1.0)),
    arrayOf(Pair(ModuleType.Birth, 1.0))
)