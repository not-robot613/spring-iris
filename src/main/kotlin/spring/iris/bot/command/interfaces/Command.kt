package spring.iris.bot.command.interfaces

import spring.iris.bot.command.constant.CommandType
import spring.iris.bot.command.dto.CommandResult
import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo

interface Command {
    val commandType: CommandType
    fun matches(msg: MsgInfo): Boolean
    suspend fun execute(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): CommandResult
    fun getDescription(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): String
}