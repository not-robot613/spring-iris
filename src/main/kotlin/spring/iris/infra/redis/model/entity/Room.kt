package spring.iris.infra.redis.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import spring.iris.common.enums.RoomType

@RedisHash("room")
data class Room(
    @Id
    val id: Long,
    val type: RoomType
)
