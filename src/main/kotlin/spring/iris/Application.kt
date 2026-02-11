package spring.iris

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import spring.iris.common.config.BotConfig
import spring.iris.infra.iris.config.IrisConfig
import spring.iris.infra.kakaoLink.config.KakaoLinkConfig

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties(
    IrisConfig::class,
    KakaoLinkConfig::class,
    BotConfig::class
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}