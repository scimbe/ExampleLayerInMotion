package com.example.motion.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MotionState {
    private final UUID characterId;
    private final Position position;
    private final Rotation rotation;
    private final float speed;
    private final long timestamp;

    public MotionState(UUID characterId, Position position, Rotation rotation, float speed) {
        this(characterId, position, rotation, speed, System.currentTimeMillis());
    }

    public MotionState(UUID characterId, Position position, Rotation rotation, float speed, long timestamp) {
        this.characterId = characterId;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.timestamp = timestamp;
    }
}