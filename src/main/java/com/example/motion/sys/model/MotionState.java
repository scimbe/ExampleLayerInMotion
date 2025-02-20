package com.example.motion.sys.model;

import java.util.UUID;

/**
 * Repräsentiert den Bewegungszustand eines Charakters.
 */
public class MotionState {
    private final UUID characterId;
    private final Position position;
    private final Rotation rotation;
    private final float speed;
    private final long timestamp;

    /**
     * Konstruktor für einen Bewegungszustand.
     */
    public MotionState(UUID characterId, Position position, Rotation rotation, float speed) {
        this(characterId, position, rotation, speed, System.currentTimeMillis());
    }

    /**
     * Konstruktor für einen Bewegungszustand mit spezifischem Timestamp.
     */
    public MotionState(UUID characterId, Position position, Rotation rotation, float speed, long timestamp) {
        this.characterId = characterId;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public UUID getCharacterId() { return characterId; }
    public Position getPosition() { return position; }
    public Rotation getRotation() { return rotation; }
    public float getSpeed() { return speed; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("MotionState(id=%s, pos=%s, rot=%s, speed=%.2f, time=%d)",
            characterId, position, rotation, speed, timestamp);
    }
}
