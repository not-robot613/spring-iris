package spring.iris.infra.iris.websocket.scope

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class WebSocketCoroutineScope: CoroutineScope {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "웹소켓 스코프에서 처리되지 않은 예외 발생" }
    }

    override val coroutineContext = SupervisorJob() + Dispatchers.IO + exceptionHandler

    @PreDestroy
    fun cancelScope() {
        this.cancel()
    }
}