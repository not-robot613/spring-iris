//https://github.com/dolidolih/irispy-client

package spring.iris.infra.kakaoLink.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.*
import spring.iris.infra.iris.config.IrisConfig
import spring.iris.infra.kakaoLink.auth.KakaoLinkAuthorizationProvider
import spring.iris.infra.kakaoLink.config.KakaoLinkConfig
import spring.iris.infra.kakaoLink.enums.SearchFrom
import spring.iris.infra.kakaoLink.enums.SearchRoomType
import spring.iris.infra.kakaoLink.exception.*
import java.net.URI
import java.net.URLEncoder
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
@OptIn(ExperimentalEncodingApi::class)
class KakaoLink(
    private val webClient: WebClient,
    private val properties: KakaoLinkConfig,
    private val objectMapper: ObjectMapper,
    iris: IrisConfig
) : KLogging() {
    companion object {
        const val KAKAOTALK_VERSION = "11.4.2"
        const val ANDROID_SDK_VER = 33
        const val ANDROID_WEBVIEW_UA =
            "Mozilla/5.0 (Linux; Android 13; SM-G998B Build/TP1A.220624.014; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/114.0.5735.60 Mobile Safari/537.36"
    }

    private val sendLock = Mutex()
    private val authorizationProvider = KakaoLinkAuthorizationProvider(iris.url)
    private var accountId: Long? = null

    suspend fun send(
        receiverName: String,
        templateId: Int,
        templateArgs: Map<String, Any>
    ) {
        val origin = properties.origin
        val appKey = properties.appKey

        if (!checkAuthorized()) {
            logger.warn { "세션이 유효하지 않아 로그인을 재시도합니다." }
            login()
        }

        val ka = getKa(origin)

        sendLock.withLock {
            val (pickerData, pickerUri) = getPickerData(appKey, ka, templateId, templateArgs)
            val checksum = pickerData.get("checksum")?.asText()
                ?: throw KakaoLinkSendException("checksum을 찾을 수 없습니다")
            val csrf = pickerData.get("csrfToken")?.asText()
                ?: throw KakaoLinkSendException("csrfToken을 찾을 수 없습니다")
            val shortKey = pickerData.get("shortKey")?.asText()
                ?: throw KakaoLinkSendException("shortKey를 찾을 수 없습니다")

            val receiver = pickerDataSearch(receiverName, pickerData)

            logger.info { "checksum: $checksum, csrf: $csrf, shortKey: $shortKey, receiver: $receiver, pickerUri: $pickerUri" }
            pickerSend(appKey, shortKey, checksum, csrf, receiver, pickerUri)
        }
    }

    private suspend fun pickerSend(
        appKey: String,
        shortKey: String,
        checksum: String,
        csrf: String,
        receiver: JsonNode,
        pickerUri: URI
    ) {
        val receiverData =
            Base64.UrlSafe.encode(objectMapper.writeValueAsString(receiver).toByteArray())

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("app_key", appKey)
            add("short_key", shortKey)
            add("checksum", checksum)
            add("_csrf", csrf)
            add("receiver", receiverData)
        }

        webClient.post().uri("https://sharer.kakao.com/picker/send")
            .headers { headers ->
                setWebHeaders(headers)
                headers.set(HttpHeaders.REFERER, pickerUri.toString())
            }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .awaitExchange { response ->
                if (!response.statusCode().is2xxSuccessful) {
                    if (response.statusCode() == HttpStatus.BAD_REQUEST) {
                        logger.warn { "카카오링크 BAD REQUEST (성공 처리됨)" }
                        return@awaitExchange
                    }

                    val errorBody = response.awaitBodyOrNull<String>()
                    logger.debug { "카카오링크 전송 실패: ${response.statusCode()}, $errorBody" }
                    throw KakaoLinkSendException()
                }
            }
    }

    private fun pickerDataSearch(
        receiverName: String,
        pickerData: JsonNode,
        searchExact: Boolean = true,
        searchFrom: SearchFrom = SearchFrom.ALL,
        searchRoomType: SearchRoomType = SearchRoomType.ALL
    ): JsonNode {
        val searchTargets = mutableListOf<JsonNode>()
        if (searchFrom in listOf(SearchFrom.ALL, SearchFrom.CHATROOMS)) pickerData.get("chats")
            ?.forEach { searchTargets.add(it) }
        if (searchFrom in listOf(SearchFrom.ALL, SearchFrom.FRIENDS)) pickerData.get("friends")
            ?.forEach { searchTargets.add(it) }
        for (receiver in searchTargets) {
            val currentChatType = receiver.get("chat_room_type")?.asText()
            val currentTitle =
                receiver.get("title")?.asText() ?: receiver.get("profile_nickname")?.asText() ?: ""
            if (currentChatType != null && searchRoomType != SearchRoomType.ALL) {
                if (searchRoomType.name != currentChatType) continue
            }
            val isMatch =
                if (searchExact) currentTitle == receiverName else receiverName in currentTitle
            if (isMatch) return receiver
        }
        throw KakaoLinkReceiverNotFoundException()
    }

    private suspend fun getPickerData(
        appKey: String,
        ka: String,
        templateId: Int,
        templateArgs: Map<String, Any>
    ): Pair<JsonNode, URI> {
        val validationParams =
            mapOf("link_ver" to "4.0", "template_id" to templateId, "template_args" to templateArgs)
        val initialFormData = LinkedMultiValueMap<String, String>().apply {
            add("app_key", appKey); add("ka", ka); add(
            "validation_action",
            "custom"
        ); add("validation_params", objectMapper.writeValueAsString(validationParams))
        }
        return recursiveGetPickerData(
            URI.create("https://sharer.kakao.com/picker/link"),
            "POST",
            initialFormData
        )
    }

    private suspend fun recursiveGetPickerData(
        uri: URI,
        method: String,
        formData: LinkedMultiValueMap<String, String>? = null,
        retryCount: Int = 0
    ): Pair<JsonNode, URI> {
        if (retryCount > 3) throw KakaoLinkException("인증 과정을 3번 이상 재시도했지만 피커 데이터를 가져올 수 없습니다.")

        val request = if (method == "POST") {
            webClient.post().uri(uri).headers { setWebHeaders(it) }
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData!!))
        } else {
            webClient.get().uri(uri).headers { setWebHeaders(it) }
        }

        // 응답을 원시 문자열로 받아 디버깅용 로그를 남김
        val clientResponse = request.exchangeToMono { resp ->
            resp.bodyToMono<String>().map { body -> Pair(resp, body) }
        }.awaitSingle()

        val response = clientResponse.first
        val body = clientResponse.second

        val statusCode = response.statusCode()
        logger.info { "요청 URI: $uri, 응답 코드: $statusCode, bodyLength=${body?.length ?: 0}" }

        if (!body.isNullOrBlank()) {
            logger.debug { "응답 본문: $body" }
        } else {
            logger.warn { "응답 본문이 비어있거나 공백입니다. headers: ${response.headers().asHttpHeaders()}" }
        }

        if (statusCode.is3xxRedirection) {
            var location = response.headers().header("Location").firstOrNull()
                ?: throw KakaoLinkLoginException("리다이렉션 응답에 Location 헤더가 없습니다.")
            if (location.startsWith("/")) location = "https://${uri.host}$location"
            return recursiveGetPickerData(URI.create(location), "GET", retryCount = retryCount + 1)
        }

        if (statusCode.is2xxSuccessful) {
            if (body.isNullOrBlank()) {
                throw KakaoLinkException("요청은 성공했으나 응답 본문이 비어있습니다. URI: $uri")
            }
//            logger.info { "응답 body : $body" }
            if (body.contains("/talk_tms_auth/service")) {
                val continueUrl = solveTwoFactorAuth(body)
                return recursiveGetPickerData(
                    URI.create(continueUrl),
                    "GET",
                    retryCount = retryCount + 1
                )
            }
            if (body.contains("window.serverData")) {
                logger.info("피커 데이터 수신 성공. 파싱을 시작합니다.")
                val serverData = extractServerData(body)
                val decodedData = decodeBase64UrlSafe(serverData)
                return objectMapper.readTree(decodedData).get("data") to uri
            }
            throw KakaoLinkException("알 수 없는 페이지입니다. URI: $uri")
        }

        if (formData != null) {
            logger.error { "요청 데이터: $formData" }
        }
        throw KakaoLinkException("서버에서 에러 응답을 받았습니다. 상태 코드: $statusCode, 내용: $body")
    }

    internal suspend fun login() {
        val authorization = authorizationProvider.getAuthorization()
        val tgtToken = getTgtToken(authorization)
        submitTgtToken(tgtToken)
        if (!checkAuthorized()) throw KakaoLinkLoginException("TGT 토큰 제출 후에도 로그인에 실패했습니다.")
    }

    private suspend fun solveTwoFactorAuth(tfaHtml: String): String {
        try {
            val scriptStart =
                tfaHtml.indexOf("<script id=\"__NEXT_DATA__\" type=\"application/json\">")
            val scriptEnd = tfaHtml.indexOf("</script>", scriptStart)
            val jsonData = tfaHtml.substring(scriptStart + 52, scriptEnd).trim()
            val props = objectMapper.readTree(jsonData)
            val context = props.get("props").get("pageProps").get("pageContext").get("context")
            val commonContext =
                props.get("props").get("pageProps").get("pageContext").get("commonContext")
            val token = context.get("token").asText()
            val continueUrl = context.get("continueUrl").asText()
            val csrf = commonContext.get("_csrf").asText()
            confirmToken(token)
            val pollData = mapOf("_csrf" to csrf, "token" to token)
            val pollResponse = webClient.post()
                .uri("https://accounts.kakao.com/api/v2/talk_tms_auth/poll_from_service.json")
                .headers { setWebHeaders(it) }.contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pollData)
                .retrieve().awaitBody<JsonNode>()
            if (pollResponse.get("status").asInt() != 0) throw KakaoLink2FAException()
            return continueUrl
        } catch (e: Exception) {
            logger.error { "에러 -> ${e.stackTraceToString()}" }
            throw KakaoLink2FAException("2차 인증 토큰 파싱 실패")
        }
    }

    private suspend fun confirmToken(twoFactorToken: String) {
        val params = mapOf(
            "os" to "android",
            "country_iso" to "KR",
            "lang" to "ko",
            "v" to KAKAOTALK_VERSION,
            "os_version" to ANDROID_SDK_VER.toString(),
            "page" to "additional_auth_with_token",
            "additional_auth_token" to twoFactorToken,
            "close_on_completion" to "true",
            "talk_tms_auth_type" to "from_service"
        )
        val queryString =
            params.map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }.joinToString("&")
        val response =
            webClient.get().uri("https://auth.kakao.com/fa/main.html?$queryString").retrieve()
                .awaitBody<String>()
        try {
            val csrfStart = response.indexOf("<meta name=\"csrf-token\" content=\"")
            val csrfEnd = response.indexOf("\"", csrfStart + 33)
            val csrf = response.substring(csrfStart + 33, csrfEnd)
            val optionsStart = response.indexOf("var options =")
            val optionsEnd = response.indexOf("new PageBuilder()", optionsStart)
            val optionsJson =
                response.substring(optionsStart + 13, optionsEnd).trim().removeSuffix(";")
            val data = objectMapper.readTree(optionsJson)
            val formData = LinkedMultiValueMap<String, String>().apply {
                add("client_id", data.get("client_id").asText()); add("lang", "ko"); add(
                "os",
                "android"
            ); add("v", KAKAOTALK_VERSION); add("webview_v", "2"); add(
                "token",
                data.get("additionalAuthToken").asText()
            ); add("talk_tms_auth_type", "from_service"); add("authenticity_token", csrf)
            }
            val confirmResponse =
                webClient.post().uri("https://auth.kakao.com/talk_tms_auth/confirm_token.json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve().awaitBody<JsonNode>()
            if (confirmResponse.get("status").asInt() != 0) throw KakaoLink2FAException()
        } catch (e: Exception) {
            logger.error { "에러 -> ${e.stackTraceToString()}" }
            throw KakaoLink2FAException("csrf, client_id 데이터 파싱 실패")
        }
    }

    private suspend fun checkAuthorized(): Boolean {
        val response = webClient.get().uri("https://e.kakao.com/api/v1/users/me")
            .headers { headers ->
                setWebHeaders(headers)
                headers.set("referer", "https://e.kakao.com/")
            }.retrieve().awaitBodyOrNull<JsonNode>()

        if (response == null) return false
        logger.info { "로그인 상태 확인: $response" }

        val resultNode = response.get("result")
        val isValid = resultNode?.get("status")?.asText() == "VALID"

        if (isValid) {

            this.accountId = resultNode.get("id")?.asLong()

            logger.info { "로그인 성공. Account ID: ${this.accountId}" }
        }

        return isValid
    }

    private suspend fun submitTgtToken(tgtToken: String) {
        webClient.get().uri("https://e.kakao.com")
            .headers { headers ->
                setWebHeaders(headers)
                headers.set("ka-tgt", tgtToken)
            }.awaitExchange {}
    }

    private suspend fun getTgtToken(token: String): String {
        val formData = LinkedMultiValueMap<String, String>().apply {
            add("key_type", "talk_session_info"); add("key", token); add("referer", "talk")
        }

        val response = webClient.post().uri("https://api-account.kakao.com/v1/auth/tgt")
            .headers { headers -> setAppHeaders(headers, token) }
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .awaitBody<JsonNode>()

        logger.info { "getTgtToken 전체 응답: $response" }

        if (response.get("code").asInt() != 0) {
            throw KakaoLinkLoginException("TGT 토큰 발급 실패: $response")
        }
        return response.get("token").asText()
    }

    private fun getKa(origin: String): String {
        val encodedOrigin = URLEncoder.encode(origin, "UTF-8")
        return "sdk/1.43.5 os/javascript sdk_type/javascript lang/ko-KR device/Linux armv7l origin/$encodedOrigin"
    }


    private fun setAppHeaders(headers: HttpHeaders, token: String) {
        headers.set("A", "android/$KAKAOTALK_VERSION/ko")
        headers.set("C", UUID.randomUUID().toString())
        headers.set("User-Agent", "KT/$KAKAOTALK_VERSION An/13 ko")
        headers.set("Authorization", token)
    }


    private fun setWebHeaders(headers: HttpHeaders) {
        headers.set("User-Agent", "$ANDROID_WEBVIEW_UA KAKAOTALK/$KAKAOTALK_VERSION (INAPP)")
        headers.set("X-Requested-With", "com.kakao.talk")
    }


    private fun extractContinueUrl(location: String): String {
        return location.substringAfter("continue=")
            .substringBefore("&")
            .let { URLEncoder.encode(it, "UTF-8") }
    }


    private fun extractServerData(html: String): String {
        val start = html.indexOf("window.serverData = \"")
        val end = html.indexOf("\"", start + 21)
        return html.substring(start + 21, end)
    }

    private fun decodeBase64UrlSafe(input: String): ByteArray {
        var s = input.replace('-', '+').replace('_', '/')
        val padding = (4 - s.length % 4) % 4
        s += "=".repeat(padding)
        return Base64.decode(s)
    }

}