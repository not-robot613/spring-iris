package spring.iris.infra.message.scope

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class EventCoroutineScope : CoroutineScope, KLogging() {
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "이벤트 스코프에서 처리되지 않은 예외 발생" }
    }

    override val coroutineContext = SupervisorJob() + Dispatchers.IO + exceptionHandler

    @PreDestroy
    fun cancelScope() {
        this.cancel()
    }
}