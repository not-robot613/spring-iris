package spring.iris.infra.iris.redis.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import spring.iris.common.enums.Role


@RedisHash("member")
data class Member(
    @Id
    val id: Long,
    val role: Role,

    @TimeToLive
    val ttl: Long = 86400L  // 1일 = 86400초
)