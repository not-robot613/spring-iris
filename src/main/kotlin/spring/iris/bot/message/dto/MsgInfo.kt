package spring.iris.bot.message.dto

import spring.iris.infra.db.message.model.entity.Message
import spring.iris.infra.iris.websocket.enums.MessageType
import java.time.ZonedDateTime

data class MsgInfo(
    val internalId: Long,
    val clientId: Long,
    val type: MessageType,
    val content: String,
    val createdAt: ZonedDateTime,
    val isMine: Boolean,
    val referenceClientId: Long?
) {
    companion object {
        fun from(message: Message): MsgInfo {
            return MsgInfo(
                internalId = message.id!!,
                clientId = message.clientId,
                type = message.type,
                content = message.content,
                createdAt = message.clientCreatedAt,
                isMine = message.isMine,
                referenceClientId = message.referenceClientId
            )
        }
    }
}
