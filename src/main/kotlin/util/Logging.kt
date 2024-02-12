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

fun log(prefix: String, message: String) {
    val stackTrace = getStackTrace(1)
    jsprint("$prefix: $message\n\t$stackTrace")
}

fun ScreepsReturnCode.isCodeSuccess(function: String): Boolean {
    return if (this == OK) {
        true
    } else {
        log("Code", "Encountered unexpected return code from $function(): $this")
        false
    }
}

fun logError(message: String) = log("Error", message)
fun logWarn(message: String) = log("Error", message)
fun logInfo(message: String) = log("Info", message)
inline fun logDebug(messageFn: () -> String) {
    if (DEBUG_ENABLE) {
        log("Debug", messageFn())
    }
}

private val chromeFramePattern = Regex("^\\s*at (\\S+) \\(eval at exports\\.evalCode")
private val firefoxFramePattern = Regex("^\\s*([^@]+)@blob:")
fun getStackTrace(extraDrop: Int = 0): List<String> {
    val trace = Throwable().asDynamic().stack as String? ?: return emptyList()
    // Drop Throwable() and getStackTrace()
    return trace.lineSequence().drop(2 + extraDrop).mapNotNull { line ->
        chromeFramePattern.find(line).let { if (it != null) return@mapNotNull it.groupValues[1] }
        firefoxFramePattern.find(line).let { if (it != null) return@mapNotNull it.groupValues[1] }
        null
    }.toList()
}