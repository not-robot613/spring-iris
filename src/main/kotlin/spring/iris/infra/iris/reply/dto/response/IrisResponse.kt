package spring.iris.infra.iris.reply.dto.response

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import spring.iris.infra.iris.reply.enums.ReplyType

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TextIrisResponse::class, name = "TEXT"),
    JsonSubTypes.Type(value = ImageIrisResponse::class, name = "IMAGE"),
    JsonSubTypes.Type(value = ImageMultipleIrisResponse::class, name = "IMAGE_MULTIPLE")
)
sealed interface IrisResponse {
    val type: ReplyType
    val room: Long
}

data class TextIrisResponse(
    override val type: ReplyType = ReplyType.TEXT,
    override val room: Long,
    val data: String
) : IrisResponse

data class ImageIrisResponse(
    override val type: ReplyType = ReplyType.IMAGE,
    override val room: Long,
    val data: String
) : IrisResponse

data class ImageMultipleIrisResponse(
    override val type: ReplyType = ReplyType.IMAGE_MULTIPLE,
    override val room: Long,
    val data: List<String>
) : IrisResponse