package spring.iris.infra.kakaoLink.redis.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import spring.iris.infra.kakaoLink.redis.model.entity.CookieSession

@Repository
interface CookieSessionRepository : CrudRepository<CookieSession, String>