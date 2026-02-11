package spring.iris.infra.message.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.stereotype.Component

/**
 * MessageProcessor용 CoroutineScope
 *
 * SupervisorJob을 사용하여 한 작업의 실패가 다른 작업에 영향을 주지 않도록 함
 */
@Component
class MessageProcessorScope : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
}
