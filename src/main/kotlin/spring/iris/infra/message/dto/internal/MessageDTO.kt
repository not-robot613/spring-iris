package spring.iris.infra.message.dto.internal

import spring.iris.infra.iris.websocket.enums.MessageType
import java.time.ZonedDateTime


data class MessageDTO (
    val id: Long,
    val type: MessageType,
    var content: String,
    val createdAt: ZonedDateTime,
    val isMine: Boolean,
    val referenceId: Long?
)