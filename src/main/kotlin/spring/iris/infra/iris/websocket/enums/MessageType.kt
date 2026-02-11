package spring.iris.infra.iris.websocket.enums

enum class MessageType {
    MEMBER_EVENT,
    GENERAL_MESSAGE,
    PHOTO,
    VIDEO,
    EMOTICON,
    NESTED_CHAT,
    MULTIPLE_PHOTO,
    HASH_SEARCH_OR_KALING,
    NA;

    companion object {

        fun from(code: Int): MessageType {
            return when (code) {
                -1 -> NA
                0 -> MEMBER_EVENT
                1 -> GENERAL_MESSAGE
                2 -> PHOTO
                3 -> VIDEO
                12, 20 -> EMOTICON
                26 -> NESTED_CHAT
                27 -> MULTIPLE_PHOTO
                71 -> HASH_SEARCH_OR_KALING
                else -> GENERAL_MESSAGE
            }
        }
    }
}