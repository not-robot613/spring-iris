package spring.iris.infra.db.room.service.internal

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.infra.db.room.model.entity.Room
import spring.iris.infra.db.room.repository.RoomRepository

@Service
class RoomDataService(
    private val repository: RoomRepository
) {

    @Transactional
    fun save(room: Room): Room {
        return repository.save(room)
    }

    @Transactional(readOnly = true)
    fun getByInternalId(internalId: Long): Room {
        return repository.findById(internalId).get()
    }

    @Transactional(readOnly = true)
    fun get(clientId: Long): Room {
        return repository.findByClientId(clientId)!!
    }

    @Transactional(readOnly = true)
    fun get(roomName: String): Room? {
        return repository.findByName(roomName)
    }

    @Transactional(readOnly = true)
    fun exists(clientId: Long): Boolean {
        return repository.existsByClientId(clientId)
    }

}