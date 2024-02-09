@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class Module {
    abstract fun commitMemory()
    abstract val type: ModuleType
    abstract fun process()
}

typealias ModuleMap<T> = MutableMap<ModuleType, T>

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class ModuleType {
    Eco, Birth
}


val PRIORITY: Array<Array<Pair<ModuleType, Double>>> = arrayOf(
    arrayOf(Pair(ModuleType.Eco, 1.0)),
    arrayOf(Pair(ModuleType.Birth, 1.0))
)