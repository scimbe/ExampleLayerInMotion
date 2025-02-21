package com.example.motion.sys.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Repräsentiert den Bewegungszustand eines Charakters.
 */
public class MotionState {
    private final UUID characterId;
    private final Position position;
    private final Rotation rotation;
    private final float speed;
    private final Instant timestamp;

    public MotionState(UUID characterId, Position position, Rotation rotation, float speed) {
        this(characterId, position, rotation, speed, Instant.now());
    }

    public MotionState(UUID characterId, Position position, Rotation rotation, float speed, Instant timestamp) {
        this.characterId = characterId;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public float getSpeed() {
        return speed;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("MotionState(characterId=%s, position=%s, rotation=%s, speed=%.2f, timestamp=%s)",
            characterId, position, rotation, speed, timestamp);
    }
}
