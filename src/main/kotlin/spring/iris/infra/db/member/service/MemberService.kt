package spring.iris.infra.db.member.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import spring.iris.infra.db.member.entity.Member
import spring.iris.infra.db.member.repository.MemberRepository
import spring.iris.infra.message.dto.internal.MemberDTO

@Service
class MemberService(
    private val repository: MemberRepository
) {

    @Transactional
    fun getMemberOrCreate(dto: MemberDTO): Member {

        if (!repository.existsByClientId(dto.id)) {
            val member = Member(dto.id, dto.name)
            return repository.save(member)
        }

        val member = repository.findByClientId(dto.id)

        if (dto.name != member.name) {
            member.name = dto.name

            repository.save(member)
        }

        return member
    }


}