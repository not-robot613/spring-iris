package spring.iris.infra.iris.redis.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import spring.iris.common.enums.RoomType

@RedisHash("room")
data class Room(
    @Id
    val id: Long,
    val type: RoomType,

    @TimeToLive
    val ttl: Long = 86400L  // 1일 = 86400초
)
