package com.example.motion.config;

import com.example.motion.websocket.MotionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MotionWebSocketHandler motionWebSocketHandler;

    public WebSocketConfig(MotionWebSocketHandler motionWebSocketHandler) {
        this.motionWebSocketHandler = motionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(motionWebSocketHandler, "/motion-updates")
               .setAllowedOrigins("*");  // In Produktion einschränken!
    }
}