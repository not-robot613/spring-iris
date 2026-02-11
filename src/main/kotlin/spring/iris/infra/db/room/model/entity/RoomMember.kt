package spring.iris.infra.db.room.model.entity

import jakarta.persistence.*
import spring.iris.common.enums.Role
import spring.iris.infra.db.common.entity.BaseEntity
import spring.iris.infra.db.member.entity.Member

@Entity
class RoomMember(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: Room,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "room_member_roles",
        joinColumns = [JoinColumn(name = "room_member_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var roles: MutableSet<Role> = mutableSetOf()
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set
}
