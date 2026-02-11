package spring.iris.infra.db.message.model.entity

import jakarta.persistence.*
import spring.iris.infra.db.common.entity.BaseEntity
import spring.iris.infra.db.room.model.entity.RoomMember
import spring.iris.infra.iris.websocket.enums.MessageType
import java.time.ZonedDateTime

@Entity
class Message(
    @Column(nullable = false, unique = true)
    var clientId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_member_id", nullable = false)
    var roomMember: RoomMember,

    @Enumerated(EnumType.STRING)
    var type: MessageType,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var clientCreatedAt: ZonedDateTime,

    @Column(nullable = false)
    var isMine: Boolean,

    @Column(nullable = true)
    var referenceClientId: Long?

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

}
