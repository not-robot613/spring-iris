package spring.iris.infra.kakaoLink.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakaolink")
data class KakaoLinkConfig(
    val appKey: String,
    val origin: String
)