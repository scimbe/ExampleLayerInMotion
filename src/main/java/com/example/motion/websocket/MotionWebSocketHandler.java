package com.example.motion.websocket;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.model.MotionState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MotionWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<WebSocketSession, UUID> sessionCharacterMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ICharacterMotionService motionService;

    public MotionWebSocketHandler(ObjectMapper objectMapper, ICharacterMotionService motionService) {
        this.objectMapper = objectMapper;
        this.motionService = motionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Optional: Subscribe to character updates
    }

    public void sendMotionUpdate(UUID characterId, MotionState state) {
        MotionUpdate update = new MotionUpdate(
            "POSITION_UPDATE",
            characterId,
            new Position(
                state.getPosition().getX(),
                state.getPosition().getY(),
                state.getPosition().getZ()
            )
        );

        sendUpdateToSubscribers(characterId, update);
    }

    public void sendAnimationUpdate(UUID characterId, String animationId, float progress) {
        AnimationUpdate update = new AnimationUpdate(
            "ANIMATION_UPDATE",
            characterId,
            animationId,
            progress
        );

        sendUpdateToSubscribers(characterId, update);
    }

    public void sendLayerUpdate(UUID characterId, String activeLayer) {
        LayerUpdate update = new LayerUpdate(
            "LAYER_UPDATE",
            characterId,
            activeLayer
        );

        sendUpdateToSubscribers(characterId, update);
    }

    private void sendUpdateToSubscribers(UUID characterId, Object update) {
        sessionCharacterMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(characterId))
            .forEach(entry -> {
                try {
                    String message = objectMapper.writeValueAsString(update);
                    entry.getKey().sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    // Log error
                }
            });
    }

    // Update message classes
    private record MotionUpdate(
        String type,
        UUID characterId,
        Position position
    ) {}

    private record AnimationUpdate(
        String type,
        UUID characterId,
        String animationId,
        float progress
    ) {}

    private record LayerUpdate(
        String type,
        UUID characterId,
        String activeLayer
    ) {}

    private record Position(
        float x,
        float y,
        float z
    ) {}
}
