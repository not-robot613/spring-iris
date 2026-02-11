package spring.iris.common.enums

enum class RoomType(private val value: String) {
    OPEN_DM("OD"),
    REAL_CHAT("DirectChat"),
    OPEN_GROUP_CHAT("OM"),
    ETC("ETC");

    companion object {
        fun fromString(value: String): RoomType {
            return entries.find { it.value == value } ?: ETC
        }
    }
}