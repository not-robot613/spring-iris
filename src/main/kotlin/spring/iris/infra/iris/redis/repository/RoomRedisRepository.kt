package spring.iris.infra.iris.redis.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.iris.redis.model.entity.Room

@Repository
interface RoomRedisRepository : CrudRepository<Room, Long> {

    override fun existsById(id: Long): Boolean

}