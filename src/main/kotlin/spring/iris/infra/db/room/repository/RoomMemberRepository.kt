package spring.iris.infra.db.room.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.db.room.model.entity.RoomMember

@Repository
interface RoomMemberRepository : JpaRepository<RoomMember, Long> {

    override fun findAll(): List<RoomMember>
    fun existsByRoomClientIdAndMemberClientId(roomClientId: Long, memberClientId: Long): Boolean
    fun findByRoomClientIdAndMemberClientId(roomClientId: Long, memberClientId: Long): RoomMember
    fun findByMemberNameContaining(name: String): List<RoomMember>

}