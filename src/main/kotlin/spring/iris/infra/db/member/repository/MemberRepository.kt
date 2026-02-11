package spring.iris.infra.db.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.db.member.entity.Member

@Repository
interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByClientId(clientId: Long): Boolean
    fun findByClientId(clientId: Long) : Member

}