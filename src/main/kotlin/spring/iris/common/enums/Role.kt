package spring.iris.common.enums

enum class Role(val value: Int, val stringName: String, val level: Int) {
    HOST(1, "방장", 10),
    NORMAL(2, "일반 유저", 1),
    MANAGER(4, "부방장", 5),
    ADMIN(6, "봇 관리자", 100),
    BOT(8, "봇", 1),
    UNKNOWN(0, "알 수 없음", 1),
    REAL_PROFILE(-1, "실제 프로필", 1);

    companion object {
        fun fromInt(value: Int): Role {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}