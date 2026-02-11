package spring.iris.infra.db.room.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.db.room.model.entity.Room
import java.util.*

@Repository
interface RoomRepository : JpaRepository<Room, Long> {

    fun existsByClientId(clientId: Long): Boolean
    fun findByClientId(clientId: Long): Room?
    override fun findById(id: Long): Optional<Room>
    fun findByName(name: String): Room?

}