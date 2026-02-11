package spring.iris.bot.command.impl

import org.springframework.stereotype.Component
import spring.iris.bot.command.constant.CommandType
import spring.iris.bot.command.dto.CommandResult
import spring.iris.bot.command.interfaces.Command
import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo
import spring.iris.common.config.BotConfig
import spring.iris.infra.db.room.service.RoomMemberCoordinateService
import spring.iris.infra.iris.reply.client.ReplyClient
import spring.iris.infra.iris.reply.dto.response.TextIrisResponse

@Component
class AdminLoginCommand(
    private val replier: ReplyClient,
    private val config: BotConfig,
    private val roomMemberService: RoomMemberCoordinateService
) : Command {

    override val commandType = CommandType.ADMIN_LOGIN

    override fun matches(msg: MsgInfo): Boolean {
        return msg.content.startsWith(commandType.command)
    }

    override suspend fun execute(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): CommandResult {
        val args = msg.content.removePrefix(commandType.command).trim().split(" ", limit = 2)

        // 파라미터 검증
        if (args.size < 2 || args[0].isBlank() || args[1].isBlank()) {
            replier.reply(
                TextIrisResponse(
                    room = room.clientId,
                    data = "⚠️ 사용법: ${commandType.command} [비밀번호] [닉네임]"
                )
            )
            return CommandResult(quit = true)
        }

        val password = args[0]
        val nickname = args[1]

        // 비밀번호 확인
        if (password != config.adminPassword) {
            replier.reply(TextIrisResponse(room = room.clientId, data = "⚠️ 잘못된 비밀번호입니다."))
            return CommandResult(quit = true)
        }

        // 닉네임이 포함된 모든 멤버를 관리자로 승격
        val count = roomMemberService.promoteToAdminLike(nickname)

        if (count == 0L) {
            replier.reply(
                TextIrisResponse(
                    room = room.clientId,
                    data = "⚠️ '$nickname' 닉네임을 가진 멤버를 찾을 수 없습니다."
                )
            )
        } else {
            replier.reply(
                TextIrisResponse(
                    room = room.clientId,
                    data = "✅ '$nickname' 닉네임을 가진 모든 멤버 ${count}명이 봇 관리자로 승격되었습니다!"
                )
            )
        }

        return CommandResult(quit = true)
    }

    override fun getDescription(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): String {
        return "봇 관리자로 승격 (형식: ${commandType.command} [비밀번호] [닉네임])"
    }
}
