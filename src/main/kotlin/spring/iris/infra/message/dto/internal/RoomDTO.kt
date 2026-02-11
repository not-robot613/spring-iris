package spring.iris.infra.message.dto.internal

import spring.iris.common.enums.RoomType


data class RoomDTO(
    val id: Long,
    val name: String,
    val type: RoomType,
)
