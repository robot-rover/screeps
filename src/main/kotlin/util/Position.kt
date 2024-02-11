package util

import screeps.api.RoomPosition
import kotlin.math.abs
import kotlin.math.max

typealias Pos = Int

inline fun RoomPosition.toPos(): Pos = this.x + this.y*50
fun Pos.toRoomPos(room: String): RoomPosition = RoomPosition(this.room_x, this.room_y, room)
inline val Pos.room_x: Int get() = this % 50
inline val Pos.room_y: Int get() = this / 50
fun makePos(x: Int, y: Int): Pos = x + y*50

fun distance(lhs: RoomPosition, rhs: RoomPosition): Int {
    return max(abs(lhs.x - rhs.x), abs(lhs.y - rhs.y))
}