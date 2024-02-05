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

//@Suppress("unused")
@JsExport
fun loop() {
    for (module in MODULES) {
        module.process()
    }
    for (module in MODULES) {
        module.commitMemory()
    }
}
