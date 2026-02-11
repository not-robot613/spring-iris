package spring.iris.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot")
class BotConfig(val name: String, val commandPrefix: String, val adminPassword: String)
