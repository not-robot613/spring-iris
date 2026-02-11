package spring.iris.infra.redis.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.redis.model.entity.Room

@Repository
interface RoomRedisRepository : CrudRepository<Room, Long> {

    override fun existsById(id: Long): Boolean

}