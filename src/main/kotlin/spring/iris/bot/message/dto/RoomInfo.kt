package spring.iris.bot.message.dto

import spring.iris.common.enums.RoomType
import spring.iris.infra.db.room.model.entity.Room

data class RoomInfo(
    val internalId: Long,
    val clientId: Long,
    val name: String,
    val type: RoomType,
    val url: String? = null
) {

    companion object {
        fun from(room: Room): RoomInfo {
            return RoomInfo(
                room.id!!,
                room.clientId,
                room.name,
                room.type
            )
        }

        fun from(room: Room, url: String?): RoomInfo {
            return RoomInfo(
                room.id!!,
                room.clientId,
                room.name,
                room.type,
                url
            )
        }
    }
}
