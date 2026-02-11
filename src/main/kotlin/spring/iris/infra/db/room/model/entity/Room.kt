package spring.iris.infra.db.room.model.entity

import jakarta.persistence.*
import spring.iris.common.enums.RoomType
import spring.iris.infra.db.common.entity.BaseEntity

@Entity
class Room(
    @Column(nullable = false, unique = true)
    var clientId: Long,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RoomType

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

}
