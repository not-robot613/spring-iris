package spring.iris.infra.db.member.entity

import jakarta.persistence.*
import spring.iris.infra.db.common.entity.BaseEntity

@Entity
class Member(
    @Column(nullable = false, unique = true)
    var clientId: Long,

    @Column(nullable = false)
    var name: String
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set
}

