package spring.iris.infra.kakaoLink.auth

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class KakaoLinkAuthorizationProvider(irisUrl: String) {
    private val webClient = WebClient.builder()
        .baseUrl("http://$irisUrl")
        .build()
    
    suspend fun getAuthorization(): String {
        val response = webClient.get()
            .uri("/aot")
            .retrieve()
            .awaitBody<JsonNode>()
        
        val aot = response.get("aot")
        val accessToken = aot.get("access_token").asText()
        val dId = aot.get("d_id").asText()
        
        return "$accessToken-$dId"
    }
}