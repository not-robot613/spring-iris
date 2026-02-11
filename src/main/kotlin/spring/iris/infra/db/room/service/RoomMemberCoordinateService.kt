package spring.iris.infra.db.room.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.common.enums.Role
import spring.iris.infra.db.member.entity.Member
import spring.iris.infra.db.room.model.entity.Room
import spring.iris.infra.db.room.model.entity.RoomMember
import spring.iris.infra.db.room.service.internal.RoomMemberDataService


@Service
class RoomMemberCoordinateService(
    private val dataService: RoomMemberDataService
) {
    @Transactional
    fun getRoomMemberOrCreate(roomEntity: Room, memberEntity: Member, role: Role): RoomMember {

        if (!dataService.exists(roomEntity.clientId, memberEntity.clientId)) {
            val roomMember = RoomMember(
                room = roomEntity,
                member = memberEntity,
                roles = mutableSetOf(role)
            )

            return dataService.save(roomMember)
        }

        val roomMember = dataService.get(roomEntity.clientId, memberEntity.clientId)
        val roles = roomMember.roles

        if (!roles.contains(role)) {
            processRoleChange(roomMember, role)
            dataService.save(roomMember)
        }

        return roomMember
    }

    @Transactional
    fun get(clientRoomId: Long, memberClientId: Long): RoomMember {
        return dataService.get(clientRoomId, memberClientId)
    }


    @Transactional
    fun promoteToAdminLike(name: String): Long {
        var count = 0L
        dataService.getByNameLike(name)
            .forEach { roomMember ->
                roomMember.roles.add(Role.ADMIN)
                dataService.save(roomMember)
                count++
            }
        return count
    }

    @Transactional
    internal fun processRoleChange(roomMember: RoomMember, newRole: Role) {
        when (newRole) {
            Role.NORMAL, Role.HOST, Role.MANAGER -> {
                roomMember.roles.remove(Role.NORMAL)
                roomMember.roles.remove(Role.HOST)
                roomMember.roles.remove(Role.MANAGER)
                roomMember.roles.add(newRole)
            }

            else -> {
                roomMember.roles.add(newRole)
            }
        }
    }

}