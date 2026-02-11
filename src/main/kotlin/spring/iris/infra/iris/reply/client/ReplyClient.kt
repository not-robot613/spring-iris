package spring.iris.infra.iris.reply.client

import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import spring.iris.infra.iris.config.IrisConfig
import spring.iris.infra.iris.reply.dto.response.ImageIrisResponse
import spring.iris.infra.iris.reply.dto.response.IrisResponse
import spring.iris.infra.iris.reply.dto.response.TextIrisResponse


@Component
class ReplyClient(
    config: IrisConfig,
    webClientBuilder: WebClient.Builder,
) {

    private companion object {
        val logger = KotlinLogging.logger {}
        const val FILTERED_INFO = "전체보기를 눌러주세요!"
    }

    private val client: WebClient = webClientBuilder
        .baseUrl(config.restUrl)
        .build()

    suspend fun reply(irisResponse: IrisResponse): Result<Unit> {

        logResponse(irisResponse)

        val filteredResponse = filterTextData(irisResponse)

        runCatching {
            client.post()
                .uri("/reply")
                .bodyValue(filteredResponse)
                .retrieve()
                .onStatus(HttpStatusCode::isError) { response ->
                    throw RuntimeException("Iris API Error: ${response.statusCode()}")
                }
                .awaitBodilessEntity()
        }.onFailure { e ->
            logger.error(e) { "Iris 응답 에러 발생" }

            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    private fun filterTextData(response: IrisResponse): IrisResponse {

        if (response is TextIrisResponse) {
            val totalWeight = response.data.length + 19 * response.data.count { it == '\n' }
            if (totalWeight > 250) {
                val newData =
                    FILTERED_INFO + "\u200b".repeat(1000) + "\n\n" + "_".repeat(35) + "\n\n" + response.data
                return response.copy(data = newData)
            }
        }

        return response
    }

    private fun logResponse(irisResponse: IrisResponse) {
        when (irisResponse) {
            is TextIrisResponse -> logger.info {
                "TextIrisResponse -> ${
                    irisResponse.data.replace("\u200b", "").take(1000)
                }..."
            }

            is ImageIrisResponse -> logger.info { "ImageIrisResponse -> ${irisResponse.data.length} bytes" }
            else -> logger.info { "Unknown IrisResponse" }
        }
    }
}