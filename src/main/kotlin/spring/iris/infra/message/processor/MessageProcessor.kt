package spring.iris.infra.message.processor

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.launch
import mu.KLogging
import org.springframework.stereotype.Component
import spring.iris.bot.message.handler.MessageHandler
import spring.iris.infra.iris.websocket.service.WebSocketService
import spring.iris.infra.message.dto.internal.ChatType.*
import spring.iris.infra.message.dto.internal.SaveMessageResultDTO
import spring.iris.infra.message.scope.MessageProcessorScope
import spring.iris.infra.message.service.MessageProcessingService

/**
 * 메시지 프로세서
 *
 * WebSocketService의 Flow를 구독하여 메시지를 처리합니다. 개발자는 이 클래스를 수정할 필요가 없습니다.
 */
@Component
class MessageProcessor(
    private val messageProcessingService: MessageProcessingService,
    private val messageHandler: MessageHandler,
    private val webSocketService: WebSocketService,
    private val scope: MessageProcessorScope
) : KLogging() {

    @PostConstruct
    fun startProcessing() {
        scope.launch {
            webSocketService.messageFlow.collect { message ->
                try {
                    val result: SaveMessageResultDTO = messageProcessingService.saveMessage(message.dto)
                    val room = result.roomInfo
                    val sender = result.memberInfo
                    val msg = result.msgInfo

                    when (message.type) {
                        GENERAL_MESSAGE -> messageHandler.handleGeneralMessage(room, sender, msg)
                        NEW_MEMBER -> messageHandler.handleNewMember(room, sender, msg)
                        EXIT_MEMBER -> messageHandler.handleExitMember(room, sender, msg)
                        MANAGER_AUTHORITY -> messageHandler.handleManagerAuthority(room, sender, msg)
                        CHAT_HIDE -> messageHandler.handleChatHide(room, sender, msg)
                        CHAT_DELETE -> messageHandler.handleChatDelete(room, sender, msg)
                        CHAT_MODIFY -> messageHandler.handleChatModify(room, sender, msg)
                        BOT_MESSAGE -> messageHandler.handleBotMessage(room, sender, msg)
                    }
                } catch (e: Exception) {
                    logger.error(e) { "메시지 처리 중 오류 발생: ${e.message}" }
                }
            }
        }
    }
}
