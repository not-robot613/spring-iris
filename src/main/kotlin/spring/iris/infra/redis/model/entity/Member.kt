package spring.iris.infra.redis.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import spring.iris.common.enums.Role


@RedisHash("member")
data class Member(
    @Id
    val id: Long,
    val role: Role
)