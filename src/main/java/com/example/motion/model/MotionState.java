package com.example.motion.model;

import java.util.UUID;

/**
 * Repräsentiert den Bewegungszustand eines Charakters.
 */
public class MotionState {
    private final UUID characterId;
    private final Position position;
    private final Rotation rotation;
    private final float speed;

    public MotionState(UUID characterId, Position position, Rotation rotation, float speed) {
        this.characterId = characterId;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
    }

    public UUID getCharacterId() { return characterId; }
    public Position getPosition() { return position; }
    public Rotation getRotation() { return rotation; }
    public float getSpeed() { return speed; }

    @Override
    public String toString() {
        return String.format("MotionState(id=%s, pos=%s, rot=%s, speed=%.2f)",
            characterId, position, rotation, speed);
    }
}