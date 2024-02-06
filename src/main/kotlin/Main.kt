import modules.Eco

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */

val MODULES: Array<Module> = arrayOf(
    modules.Eco, modules.Birth
)

@OptIn(ExperimentalJsExport::class)
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
