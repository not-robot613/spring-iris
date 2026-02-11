package spring.iris.infra.iris.redis.service


import mu.KotlinLogging
import org.springframework.stereotype.Service
import spring.iris.infra.iris.redis.exception.DataAlreadyHereException
import spring.iris.infra.iris.redis.model.entity.Member
import spring.iris.infra.iris.redis.repository.MemberRedisRepository

private val logger = KotlinLogging.logger {}

@Service
class MemberRedisService(
    private val memberRedisRepository: MemberRedisRepository
) {

    fun saveMember(member: Member) {
        if (memberRedisRepository.existsById(member.id))
            throw DataAlreadyHereException("캐시된 회원이 이미 존재합니다. -> %s".format(member))

        logger.info { "새로운 회원 데이터 캐싱 -> $member" }

        memberRedisRepository.save(member)
    }

    fun getMemberById(id: Long): Member? {
        return memberRedisRepository.findById(id).orElse(null)
    }

}