package spring.iris.infra.kakaoLink.interfaces

interface KakaoLinkCookieStorage {
    suspend fun save(cookies: Map<String, String>)
    suspend fun load(): Map<String, String>
    suspend fun clear()
}