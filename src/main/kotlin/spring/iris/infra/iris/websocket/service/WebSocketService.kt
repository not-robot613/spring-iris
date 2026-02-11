package spring.iris.infra.iris.websocket.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service
import spring.iris.infra.iris.service.ChatRequestProcessService
import spring.iris.infra.iris.websocket.enums.Origin
import spring.iris.infra.iris.websocket.enums.Origin.*
import spring.iris.infra.iris.websocket.request.IrisRequest
import spring.iris.infra.message.dto.internal.ChatData
import spring.iris.infra.message.dto.internal.ChatMessage
import spring.iris.infra.message.dto.internal.ChatType.*

/**
 * WebSocket 메시지 처리 서비스
 *
 * 카카오톡 메시지를 수신하여 Flow로 발행합니다.
 */
@Service
class WebSocketService(private val chatRequestProcessService: ChatRequestProcessService) {

    private val _messageFlow = MutableSharedFlow<ChatMessage>(replay = 0)
    val messageFlow: SharedFlow<ChatMessage> = _messageFlow.asSharedFlow()

    suspend fun process(request: IrisRequest) {
        val origin: Origin = request.json.parsedVData.origin
        val data: ChatData = chatRequestProcessService.makeChatData(request)

        val chatType = when (origin) {
            MSG -> GENERAL_MESSAGE
            NEWMEM -> NEW_MEMBER
            DELMEM -> EXIT_MEMBER
            WRITE -> BOT_MESSAGE
            POST -> BOT_MESSAGE
            SYNCDLMSG -> CHAT_DELETE
            SYNCREWR -> CHAT_HIDE
            SYNCMEMT -> MANAGER_AUTHORITY
            SYNCMODMSG -> CHAT_MODIFY
        }

        _messageFlow.emit(ChatMessage(chatType, data))
    }
}
