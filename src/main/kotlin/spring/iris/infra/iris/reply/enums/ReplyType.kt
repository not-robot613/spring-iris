package spring.iris.infra.iris.reply.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ReplyType(
    @JsonValue
    val value: String
) {
    TEXT("text"),
    IMAGE("image"),
    IMAGE_MULTIPLE("image_multiple");
}