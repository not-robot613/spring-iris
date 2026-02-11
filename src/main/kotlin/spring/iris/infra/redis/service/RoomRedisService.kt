package spring.iris.infra.redis.service


import mu.KotlinLogging
import org.springframework.stereotype.Service
import spring.iris.common.enums.RoomType
import spring.iris.infra.redis.exception.DataAlreadyHereException
import spring.iris.infra.redis.model.entity.Room
import spring.iris.infra.redis.repository.RoomRedisRepository
import kotlin.jvm.optionals.getOrNull

private val logger = KotlinLogging.logger {}

@Service
class RoomRedisService(
    private val roomRedisRepository: RoomRedisRepository,
) {

    fun saveRoom(room: Room) {

        if (roomRedisRepository.existsById(room.id))
            throw DataAlreadyHereException("캐시된 방이 이미 존재합니다. -> %s".format(room))

        logger.info { "새로운 방 데이터 캐싱 -> $room" }

        roomRedisRepository.save(room)
    }

    fun getRoomTypeById(id: Long): RoomType? {
        val room = roomRedisRepository.findById(id).getOrNull()

        return room?.let {
            roomRedisRepository.save(it)
            it.type
        }

    }

}