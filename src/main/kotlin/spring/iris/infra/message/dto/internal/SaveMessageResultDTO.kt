package spring.iris.infra.message.dto.internal

import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo

data class SaveMessageResultDTO(
    val roomInfo: RoomInfo,
    val memberInfo: MemberInfo,
    val msgInfo: MsgInfo
)