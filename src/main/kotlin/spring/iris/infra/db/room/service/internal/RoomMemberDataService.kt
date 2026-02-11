package spring.iris.infra.db.room.service.internal

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.common.enums.Role
import spring.iris.infra.db.room.model.entity.RoomMember
import spring.iris.infra.db.room.repository.RoomMemberRepository

@Service
class RoomMemberDataService(private val repository: RoomMemberRepository) {

    @Transactional
    fun save(roomMember: RoomMember): RoomMember {
        return repository.save(roomMember)
    }

    @Transactional
    fun get(roomClientId: Long, memberClientId: Long): RoomMember {
        return repository.findByRoomClientIdAndMemberClientId(roomClientId, memberClientId)
    }

    @Transactional
    fun getByNameLike(name: String): List<RoomMember> {
        return repository.findByMemberNameContaining(name)
    }

    @Transactional(readOnly = true)
    fun exists(roomClientId: Long, memberClientId: Long): Boolean {
        return repository.existsByRoomClientIdAndMemberClientId(roomClientId, memberClientId)
    }

    @Transactional
    fun addRole(roomClientId: Long, memberClientId: Long, role: Role) {
        val roomMember =
                repository.findByRoomClientIdAndMemberClientId(roomClientId, memberClientId)
        roomMember.roles.add(role)
        repository.save(roomMember)
    }
}
