package spring.iris.infra.iris.websocket.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

@Configuration
class WebSocketConfig {

    @Bean
    fun createWebSocketContainer(): ServletServerContainerFactoryBean {
        val container = ServletServerContainerFactoryBean()

        container.setMaxTextMessageBufferSize(1024 * 1024)
        container.setMaxBinaryMessageBufferSize(1024 * 1024)

        return container
    }
}