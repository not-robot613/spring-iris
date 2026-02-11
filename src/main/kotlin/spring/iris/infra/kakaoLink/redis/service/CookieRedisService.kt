package spring.iris.infra.kakaoLink.redis.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import spring.iris.infra.kakaoLink.interfaces.KakaoLinkCookieStorage
import spring.iris.infra.kakaoLink.redis.model.entity.CookieSession
import spring.iris.infra.kakaoLink.redis.repository.CookieSessionRepository

private const val SESSION_ID = "SINGLE_SESSION"

@Service
@Primary
class CookieRedisService(
    private val repository: CookieSessionRepository
) : KakaoLinkCookieStorage {

    override suspend fun save(cookies: Map<String, String>) {
        withContext(Dispatchers.IO) {
            val session = CookieSession(id = SESSION_ID, cookies = cookies)
            repository.save(session)
        }
    }

    override suspend fun load(): Map<String, String> = withContext(Dispatchers.IO) {
        return@withContext repository.findById(SESSION_ID).orElse(null)?.cookies ?: emptyMap()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        repository.deleteById(SESSION_ID)
    }
}