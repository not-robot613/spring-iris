package spring.iris.infra.db.room.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.common.enums.RoomType
import spring.iris.infra.db.room.model.entity.Room
import spring.iris.infra.db.room.service.internal.RoomDataService
import spring.iris.infra.message.dto.internal.RoomDTO


@Service
class RoomCoordinateService(
    private val dataService: RoomDataService
) {

    @Transactional
    fun getRoomOrCreate(dto: RoomDTO): Room {

        if (!dataService.exists(dto.id)) {
            val room = Room(dto.id, dto.name, dto.type)
            return dataService.save(room)
        }

        val room = dataService.get(dto.id)

        if (room.name != dto.name)
            room.name = dto.name

        return room
    }

    @Transactional(readOnly = true)
    fun getByInternalId(internalId: Long): Room {
        return dataService.get(internalId)
    }

    @Transactional(readOnly = true)
    fun get(clientId: Long): Room {
        return dataService.get(clientId)
    }

    @Transactional(readOnly = true)
    fun get(roomName: String): Room? {
        val room = dataService.get(roomName) ?: return null
        if (room.type == RoomType.OPEN_GROUP_CHAT)
            return room
        return null
    }

}