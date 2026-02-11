package spring.iris.bot.message.dto

import spring.iris.common.enums.Role
import spring.iris.infra.db.room.model.entity.RoomMember

data class MemberInfo(
    val clientId: Long,
    val internalId: Long,
    val name: String,
    val roles: Set<Role>
) {

    companion object {
        fun from(roomMember: RoomMember): MemberInfo {
            return MemberInfo(
                roomMember.member.clientId,
                roomMember.member.id!!,
                roomMember.member.name,
                roomMember.roles
            )
        }
    }

}
