package spring.iris.bot.message.handler

import mu.KLogging
import org.springframework.stereotype.Component
import spring.iris.bot.command.CommandRegistry
import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo
import spring.iris.common.config.BotConfig
import spring.iris.infra.iris.reply.client.ReplyClient
import spring.iris.infra.iris.reply.dto.response.TextIrisResponse

/**
 * 메시지 이벤트 핸들러
 *
 * 개발자는 이 클래스의 메서드들만 수정하면 됩니다. 각 메서드는 room, sender, msg 파라미터를 받아서 비즈니스 로직을 처리합니다.
 */
@Component
class MessageHandler(
    private val replier: ReplyClient,
    private val commandRegistry: CommandRegistry,
    private val botConfig: BotConfig
) : KLogging() {

    /** 일반 채팅 메시지 처리 */
    suspend fun handleGeneralMessage(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        // 사용법 명령어
        if (msg.content == "${botConfig.commandPrefix}사용법") {

            val response = TextIrisResponse(
                room = room.clientId,
                data = commandRegistry.getHelp(msg, room, sender)
            )

            replier.reply(response)
            return
        }

        // 등록된 명령어 실행
        commandRegistry.execute(msg, room, sender)
    }

    /** 새 멤버 입장 처리 */
    suspend fun handleNewMember(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "새 멤버 입장: ${sender.name} in ${room.name}" }
    }

    /** 멤버 퇴장 처리 */
    suspend fun handleExitMember(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "멤버 퇴장: ${sender.name} from ${room.name}" }
    }

    /** 관리자 권한 변경 처리 */
    suspend fun handleManagerAuthority(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "관리자 권한 변경: ${sender.name} in ${room.name}" }
    }

    /** 메시지 숨김 처리 */
    suspend fun handleChatHide(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "메시지 숨김: ${room.name}" }
    }

    /** 메시지 삭제 처리 */
    suspend fun handleChatDelete(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "메시지 삭제: ${room.name}" }
    }

    /** 메시지 수정 처리 */
    suspend fun handleChatModify(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "메시지 수정: ${room.name}" }
    }

    /** 봇 메시지 처리 */
    suspend fun handleBotMessage(room: RoomInfo, sender: MemberInfo, msg: MsgInfo) {
        logger.info { "봇의 응답 메시지: ${msg.content.replace("\u200b", "").take(100)}..." }
    }
}
