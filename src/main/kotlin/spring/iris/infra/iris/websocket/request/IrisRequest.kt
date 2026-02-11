package spring.iris.infra.iris.websocket.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging.logger
import spring.iris.infra.iris.websocket.enums.MessageType
import spring.iris.infra.iris.websocket.enums.Origin

private val logger = logger {}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IrisRequest(
    val msg: String,
    val room: String,
    val sender: String?,
    val json: ChatDetails
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChatDetails(
    val chatId: String,
    val userId: String,

    @field:JsonProperty("_id")
    val internalId: String,
    val id: String,
    val type: Int,
    val message: String,
    val attachment: String,
    val createdAt: String,
    val deletedAt: String,
    val clientMessageId: String,
    val prevId: String,
    val referer: String,
    val supplement: String?,
    val v: String
) {
    @get:JsonIgnore
    val parsedVData: VData by lazy {
        jacksonObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .readValue(v)
    }

    @get:JsonIgnore
    val parsedAttachment: AttachmentData? by lazy {
        if (attachment.isNotBlank() && attachment.startsWith("{")) {
            try {
                jacksonObjectMapper()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .readValue(attachment)
            } catch (e: Exception) {
                logger.info { "Attachment 예외 -> ${e.message}" }
                null
            }
        } else {
            null
        }
    }

    fun getRequestType(): MessageType {
        return MessageType.from(type)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class VData(
    val notDecoded: Boolean,
    val origin: Origin,
    val c: String,
    val modifyRevision: Int,
    val isSingleDefaultEmoticon: Boolean,
    val defaultEmoticonsCount: Int,
    val isMine: Boolean,
    val enc: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AttachmentData(

    @field:JsonProperty("src_logId")
    val srcLogId: Long?,
    val srcUserId: Long?,
    val srcType: Int?,
    val srcMessage: String?
) {
    fun getRequestType(): MessageType {
        return MessageType.from(srcType ?: -1)
    }
}