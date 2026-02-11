package spring.iris.bot.command

import org.springframework.stereotype.Component
import spring.iris.bot.command.dto.CommandResult
import spring.iris.bot.command.impl.AdminLoginCommand
import spring.iris.bot.command.interfaces.Command
import spring.iris.bot.message.dto.MemberInfo
import spring.iris.bot.message.dto.MsgInfo
import spring.iris.bot.message.dto.RoomInfo
import spring.iris.common.config.BotConfig

@Component
class CommandRegistry(
    private val botConfig: BotConfig,
    adminLoginCommand: AdminLoginCommand
) {

    private val commands: List<Command> = listOf(adminLoginCommand)

    private val helpPrefix = """
        ${botConfig.name} 의 사용법 입니다.
        """.trimIndent()

    suspend fun execute(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): CommandResult {
        for (command in commands) {
            if (command.matches(msg)) {
                val result = command.execute(msg, room, sender)
                if (result.quit) {
                    return result
                }
            }
        }
        return CommandResult(quit = false)
    }

    suspend fun getHelp(msg: MsgInfo, room: RoomInfo, sender: MemberInfo): String {
        if (commands.isEmpty()) {
            return helpPrefix
        }

        val descriptions =
                commands
                        .mapIndexed { index, command ->
                            "${index + 1}. ${botConfig.commandPrefix}${command.commandType.command} :  ${
                    command.getDescription(
                        msg,
                        room,
                        sender
                    )
                }"
                        }
                        .toList()

        return helpPrefix + descriptions.joinToString("\n\n")
    }
}
