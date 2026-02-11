package spring.iris.infra.message.dto.internal

/**
 * 채팅 메시지 데이터
 *
 * Flow를 통해 전달되는 메시지 정보를 담는 데이터 클래스
 */
data class ChatMessage(val type: ChatType, val dto: ChatData)

/** 채팅 타입 */
enum class ChatType {
    /** 일반 채팅 메시지 */
    GENERAL_MESSAGE,

    /** 새 멤버 입장 */
    NEW_MEMBER,

    /** 멤버 퇴장 */
    EXIT_MEMBER,

    /** 관리자 권한 변경 */
    MANAGER_AUTHORITY,

    /** 메시지 숨김 */
    CHAT_HIDE,

    /** 메시지 삭제 */
    CHAT_DELETE,

    /** 메시지 수정 */
    CHAT_MODIFY,

    /** 봇 메시지 */
    BOT_MESSAGE
}

data class ChatData(
    val sender: MemberDTO,
    val msg: MessageDTO,
    val room: RoomDTO,
    val startTime: Long
)

