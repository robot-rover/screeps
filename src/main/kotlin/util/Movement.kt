package util

import screeps.api.*

fun Creep.moveExt(navigationTarget: NavigationTarget): Boolean {
    if (this.fatigue > 0) return false

    return this.moveTo(navigationTarget, opts = options { visualizePathStyle = options {  } })
        .isCodeSuccess("moveExt")
}