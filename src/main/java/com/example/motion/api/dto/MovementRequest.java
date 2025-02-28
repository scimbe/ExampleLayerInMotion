package com.example.motion.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovementRequest {
    private float directionX;
    private float directionY;
    private float directionZ;
    private float speed;

    public MovementRequest(float directionX, float directionY, float directionZ, float speed) {
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
        this.speed = speed;
    }
}
