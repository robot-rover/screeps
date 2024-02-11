import kotlinx.serialization.internal.throwMissingFieldException
import modules.Birth
import modules.Eco

abstract class Module {
    abstract fun commitMemory()
    abstract val type: ModuleType
    abstract fun process()

    open fun init() {}

    open fun getCreepQueues(): Sequence<String> = emptySequence()
    open fun getCreeps(queueName: String): Birth.BirthQueue? = null
    fun creepSequence(): Sequence<Pair<String, Birth.BirthQueue>> = this.getCreepQueues().mapNotNull { it to (this.getCreeps(it) ?: return@mapNotNull null) }

    open fun ecoSequence(): Sequence<Eco.EcoRequest> = emptySequence()
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