package com.skill.kairo.infrastructure.config;

import com.skill.kairo.infrastructure.adapter.in.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // A rota que o Next.js vai usar: ws://localhost:8080/api/v1/chat/stream
        registry.addHandler(chatWebSocketHandler, "/api/v1/chat/stream")
                .setAllowedOrigins("http://localhost:3000"); // Permite o Next.js
    }
}