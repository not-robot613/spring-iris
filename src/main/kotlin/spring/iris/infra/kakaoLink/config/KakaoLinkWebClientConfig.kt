package spring.iris.infra.kakaoLink.config

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import mu.KLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import spring.iris.infra.kakaoLink.redis.service.CookieRedisService
import reactor.netty.http.client.HttpClient

@Configuration
class KakaoLinkWebClientConfig(
    private val cookieRedisService: CookieRedisService,
) : KLogging() {

    @Bean
    fun kakaoLinkWebClient(): WebClient {
        val cookieFilter = ExchangeFilterFunction { request, next ->
            mono {
                val storedCookies = cookieRedisService.load()
                val requestBuilder = ClientRequest.from(request)
                if (storedCookies.isNotEmpty()) {
                    logger.debug { "Sending cookies: $storedCookies" }
                    val cookieHeader =
                        storedCookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    requestBuilder.header("Cookie", cookieHeader)
                }
                val modifiedRequest = requestBuilder.build()
                next.exchange(modifiedRequest).awaitSingle()
            }
        }

        val responseFilter = ExchangeFilterFunction.ofResponseProcessor { response ->
            mono {
                val newCookies = response.cookies()
                    .mapValues { entry -> entry.value.firstOrNull()?.value ?: "" }
                    .filterValues { it.isNotEmpty() }

                if (newCookies.isNotEmpty()) {
                    val existingCookies = cookieRedisService.load().toMutableMap()
                    existingCookies.putAll(newCookies)
                    cookieRedisService.save(existingCookies)
                    logger.debug { "Merged and saved cookies: $existingCookies" }
                }
                response
            }
        }

        val httpClient = HttpClient.create()
            .compress(true)
            .followRedirect(true)

        val connector = ReactorClientHttpConnector(httpClient)

        return WebClient.builder()
            .defaultHeader(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
            )
            .defaultHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            .defaultHeader("Accept-Encoding", "gzip, deflate")
            .filter(cookieFilter)
            .filter(responseFilter)
            .clientConnector(connector)
            .build()
    }
}
