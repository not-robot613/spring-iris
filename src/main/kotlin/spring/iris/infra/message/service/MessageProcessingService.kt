package spring.iris.infra.message.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo
import spring.iris.infra.db.member.service.MemberService
import spring.iris.infra.db.message.service.MessageService
import spring.iris.infra.db.room.service.RoomCoordinateService
import spring.iris.infra.db.room.service.RoomMemberCoordinateService
import spring.iris.infra.message.dto.internal.ChatData
import spring.iris.infra.message.dto.internal.SaveMessageResultDTO

@Service
class MessageProcessingService(
    private val roomService: RoomCoordinateService,
    private val memberService: MemberService,
    private val msgService: MessageService,
    private val roomMemberService: RoomMemberCoordinateService
) {

    @Transactional
    fun saveMessage(data: ChatData): SaveMessageResultDTO {
        val room = roomService.getRoomOrCreate(data.room)

        val member = memberService.getMemberOrCreate(data.sender)

        val roomMember = roomMemberService.getRoomMemberOrCreate(room, member, data.sender.role)

        val message = msgService.save(roomMember, data.msg)

        return SaveMessageResultDTO(RoomInfo.from(room), MemberInfo.from(roomMember), MsgInfo.from(message))
    }
}
