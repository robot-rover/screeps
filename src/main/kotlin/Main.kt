@file:OptIn(ExperimentalJsExport::class)

import screeps.api.Memory
import screeps.api.get
import util.jsprint
import util.logError


var doneInit: Boolean = false

@JsExport
fun loop() {
    if (!doneInit) {
        jsprint("Running initializers")
        MODULES.forEach {
            it.init()
        }
        doneInit = true
    }

    for (module in MODULES) {
        try {
            module.process()
        } catch (todo: NotImplementedError) {
            logError("TODO in ${module.type}#process()\n$todo")
        } catch (throwable: Throwable) {
            logError("Error in ${module.type}#process()\n$throwable")
        }
    }
    for (module in MODULES) {
        module.commitMemory()
    }
}
