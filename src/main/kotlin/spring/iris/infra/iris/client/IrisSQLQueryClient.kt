package spring.iris.infra.iris.client


import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import spring.iris.infra.exception.RequestFailedException
import spring.iris.infra.iris.config.IrisConfig
import spring.iris.infra.iris.dto.request.QueryRequest
import spring.iris.infra.iris.dto.response.QueryResponse

@Component
class IrisSQLQueryClient(
    config: IrisConfig,
    webClientBuilder: WebClient.Builder
) : KLogging() {

    private val client = webClientBuilder.baseUrl(config.restUrl).build()

    suspend fun query(request: QueryRequest): QueryResponse {
        return try {
            client.post()
                .uri("/query")
                .bodyValue(request)
                .retrieve()
                .awaitBody<QueryResponse>()
        } catch (e: WebClientResponseException) {
            val errorBody = e.responseBodyAsString
            val headers = e.headers
            val status = e.statusCode

            logger.error { "응답 실패 -> $status, 헤더 -> $headers, body -> $errorBody" }

            throw RequestFailedException("Iris 쿼리 API 호출 실패: $errorBody")
        }
    }

}