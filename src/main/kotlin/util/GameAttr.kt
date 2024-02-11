package util

import screeps.api.*

/**
 * Time a creep lives (in ticks)
 */
const val CREEP_LIFETIME: Int = 1500

/**
 * Maximum number of body parts in a creep
 */
const val CREEP_MAX_SIZE: Int = 50

/**
 * Hit points per creep body part
 */
const val CREEP_PART_HITS: Int = 100

object MovePart {
    const val FATIGUE: Int = 2
}

object WorkPart {
    const val HARVEST_ENERGY: Int = 2
    const val HARVEST_RESOURCE: Int = 1
    const val BUILD_STRUCTURE: Int = 5
    const val BUILD_ENERGY: Int = -5
    const val REPAIR_STRUCTURE: Int = 100
    const val REPAIR_ENERGY: Int = -1
    const val DISMANTLE_STRUCTURE: Int = 100
    const val DISMANTLE_ENERGY: Double = 0.25
    const val UPGRADE_CONTROLLER: Int = 1
    const val UPGRADE_ENERGY: Int = -1
}

object CarryPart {
    const val RESOURCE_CAPACITY: Int = 50
}

object AttackPart {
    const val ATTACK_DAMAGE: Int = 30
}

object RangedAttackPart {
    const val ATTACK_DAMAGE: Int = 10
    val SPLASH_DAMAGE_CLOSE: Int = 10
    val SPLASH_DAMAGE_MID: Int = 3
    val SPLASH_DAMAGE_FAR: Int = 1
}

object HealPart {
    const val HEAL: Int = 12
    const val HEAL_RANGED: Int = 4
}

object ClaimPart {
    const val RESERVE: Int = 1
    const val ATTACK_HOSTILE: Int = 300
    const val ATTACK_NEUTRAL: Int = 1
}

/**
 * The energy required to spawn a body part
 */
fun bodyPartEnergy(bodyPart: BodyPartConstant): Int {
    return when (bodyPart) {
        MOVE -> 50
        WORK -> 100
        CARRY -> 50
        ATTACK -> 80
        RANGED_ATTACK -> 150
        HEAL -> 250
        CLAIM -> 600
        TOUGH -> 10
        else -> {
            logError("Encountered invalid BodyPartConstant: $bodyPart")
            0
        }
    }
}

const val CREEP_PART_SPAWN_TICKS: Int = 3