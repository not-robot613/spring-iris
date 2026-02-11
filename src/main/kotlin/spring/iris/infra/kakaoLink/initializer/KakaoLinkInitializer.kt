package spring.iris.infra.kakaoLink.initializer

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import spring.iris.infra.kakaoLink.client.KakaoLink
import spring.iris.infra.kakaoLink.config.KakaoLinkConfig

@Component
class KakaoLinkInitializer(
    private val kakaoLink: KakaoLink,
    private val config: KakaoLinkConfig
) {
    private val logger = KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    suspend fun onApplicationReady() {

        if (config.appKey == "none" || config.origin == "none") {
            logger.info { "카카오링크 init을 건너뜁니다." }
            return
        }

        logger.info { "카카오링크 로그인을 시작합니다." }
        try {
            kakaoLink.login()
            logger.info { "카카오링크 초기 로그인 성공." }
        } catch (e: Exception) {
            logger.error(e) { "카카오링크 초기 로그인 실패." }
        }
    }
}