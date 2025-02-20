package com.example.motion.api.dto;

import com.example.motion.model.MotionState;
import lombok.Data;

import java.util.UUID;

@Data
public class CharacterResponse {
    private final UUID characterId;
    private final float x;
    private final float y;
    private final float z;
    private final float speed;
    private final float rotationX;
    private final float rotationY;
    private final float rotationZ;

    public CharacterResponse(UUID characterId, MotionState state) {
        this.characterId = characterId;
        this.x = state.getPosition().getX();
        this.y = state.getPosition().getY();
        this.z = state.getPosition().getZ();
        this.speed = state.getSpeed();
        this.rotationX = state.getRotation().getPitch();
        this.rotationY = state.getRotation().getYaw();
        this.rotationZ = state.getRotation().getRoll();
    }
}