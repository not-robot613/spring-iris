package spring.iris.infra.db.message.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.infra.db.message.model.entity.Message
import spring.iris.infra.db.message.repository.MessageRepository
import spring.iris.infra.db.room.model.entity.RoomMember
import spring.iris.infra.message.dto.internal.MessageDTO

@Service
class MessageService(
    private val repository: MessageRepository
) {

    @Transactional
    fun save(roomMember: RoomMember, msgDTO: MessageDTO) : Message {

        val entity = Message(
            clientId = msgDTO.id,
            roomMember = roomMember,
            type = msgDTO.type,
            content = msgDTO.content,
            clientCreatedAt = msgDTO.createdAt,
            isMine = msgDTO.isMine,
            referenceClientId = msgDTO.referenceId
        )

        return repository.save(entity)
    }

}