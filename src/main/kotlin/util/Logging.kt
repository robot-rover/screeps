package util

import ModuleType
import screeps.api.OK
import screeps.api.ScreepsReturnCode

var DEBUG_ENABLE: Boolean = false

external object console {
    fun log(message: String)
    fun error(message: String)
}
inline fun jsprint(message: String) {
    console.log(message)
}

fun log(prefix: String, message: String, module: ModuleType?) {
    val postfix = if (module != null) { " in $module" } else { "" }
    jsprint("$prefix: $message$postfix")
}

fun isCodeSuccess(function: String, code: ScreepsReturnCode, module: ModuleType? = null): Boolean {
    return if (code == OK) {
        true
    } else {
        log("Code", "Encountered unexpected return code from $function(): $code", module)
        false
    }
}

fun logError(message: String, module: ModuleType? = null) = log("Error", message, module)
fun logWarn(message: String, module: ModuleType? = null) = log("Error", message, module)
fun logInfo(message: String, module: ModuleType? = null) = log("Info", message, module)
inline fun logDebug(module: ModuleType? = null, messageFn: () -> String) {
    if (DEBUG_ENABLE) {
        log("Debug", messageFn(), module)
    }
}
