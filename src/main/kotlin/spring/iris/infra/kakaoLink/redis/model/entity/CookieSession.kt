package spring.iris.infra.kakaoLink.redis.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("kakaolink:session")
data class CookieSession(
    @Id
    val id: String = "SINGLE_SESSION",
    val cookies: Map<String, String> = mapOf()
)