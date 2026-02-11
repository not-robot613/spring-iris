package spring.iris.infra.iris.websocket.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.websocket.ContainerProvider
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import spring.iris.infra.iris.config.IrisConfig
import spring.iris.infra.iris.websocket.request.IrisRequest
import spring.iris.infra.iris.websocket.scope.WebSocketCoroutineScope
import spring.iris.infra.iris.websocket.service.WebSocketService
import java.time.Instant

@Service
class WebSocketHandler(
    private val properties: IrisConfig,
    private val objectMapper: ObjectMapper,
    private val taskScheduler: TaskScheduler,
    private val service: WebSocketService,
    private val scope: WebSocketCoroutineScope
) : TextWebSocketHandler() {

    companion object {
        private val logger = KotlinLogging.logger {}
        const val BUFFER_SIZE = 64 * 1024
    }

    private val webSocketClient by lazy {
        val webSocketContainer = ContainerProvider.getWebSocketContainer()
        webSocketContainer.defaultMaxBinaryMessageBufferSize = BUFFER_SIZE
        webSocketContainer.defaultMaxTextMessageBufferSize = BUFFER_SIZE
        StandardWebSocketClient(webSocketContainer)
    }

    private var currentSession: WebSocketSession? = null


    private var initialDelay = 5000L
    private var currentDelay = initialDelay
    private val maxDelay = 60000L

    @PostConstruct
    fun init() {
        connect()
    }

    fun connect() {
        try {
            logger.info { "웹소켓 연결 시도... URL: ${properties.websocketUrl}" }
            webSocketClient.execute(this, properties.websocketUrl)
        } catch (e: Exception) {
            logger.error(e) { "웹소켓 연결 실패. ${currentDelay / 1000}초 후 재시도합니다." }
            scheduleReconnect()
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info { "웹소켓 연결 성공: ${session.id}" }
        this.currentSession = session
        this.currentDelay = initialDelay
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
//        logger.info { "raw 메세지 -> ${message.payload}" }
        val irisRequest = try {
            objectMapper.readValue(message.payload, IrisRequest::class.java)
        } catch (e: Exception) {
            logger.error(e) { "메시지 파싱 오류: ${message.payload}" }
            return
        }

        scope.launch { service.process(irisRequest) }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.warn { "웹소켓 연결 종료: ${session.id}, Status: $status. 재연결을 시도합니다." }
        this.currentSession = null
        scheduleReconnect()
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(exception) { "💥 웹소켓 전송 오류 발생. 재연결을 시도합니다." }
    }

    private fun scheduleReconnect() {
        val nextExecutionTime = Instant.now().plusMillis(currentDelay)
        logger.info { "${currentDelay / 1000}초 후 재연결합니다." }

        taskScheduler.schedule(this::connect, nextExecutionTime)

        currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
    }
}