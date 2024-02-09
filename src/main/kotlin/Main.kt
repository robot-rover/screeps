@file:OptIn(ExperimentalJsExport::class)

/**
 * Entry point
 * is called by screeps
 */

@JsExport
val MODULES: Array<Module> = arrayOf(
    modules.Eco, modules.Birth
)

@JsExport
fun loop() {
    for (module in MODULES) {
        try {
            module.process()
        } catch (exception: Exception) {
            print("Error in ${module.type}#process()\n$exception")
        } catch (todo: NotImplementedError) {
            print("Error in ${module.type}#process()\n$todo")
        }
    }
    for (module in MODULES) {
        module.commitMemory()
    }
}
