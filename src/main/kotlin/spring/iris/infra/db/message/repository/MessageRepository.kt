package spring.iris.infra.db.message.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.db.message.model.entity.Message


@Repository
interface MessageRepository : JpaRepository<Message, Long>