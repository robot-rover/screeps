@file:OptIn(ExperimentalJsExport::class)

import util.jsprint
import util.logError


var doneInit: Boolean = false

@JsExport
fun loop() {
    if (!doneInit) {
        jsprint("Doing Init")
        MODULES.forEach { it.init() }
        doneInit = true
    }

    for (module in MODULES) {
        jsprint("Running ${module.type}")
        try {
            module.process()
        } catch (exception: Exception) {
            logError("Error in ${module.type}#process()\n$exception")
        } catch (todo: NotImplementedError) {
            logError("TODO in ${module.type}#process()\n$todo")
        } catch (throwable: Throwable) {
            logError("Throwable in ${module.type}#process()\n$throwable")
        }
    }
    for (module in MODULES) {
        module.commitMemory()
    }
}
