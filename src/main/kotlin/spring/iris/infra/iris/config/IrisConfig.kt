package spring.iris.infra.iris.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "iris")
data class IrisConfig(
    val websocketUrl: String,
    val restUrl: String,
    val url: String
)