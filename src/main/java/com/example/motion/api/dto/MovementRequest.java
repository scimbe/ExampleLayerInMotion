package com.example.motion.api.dto;

import lombok.Data;

@Data
public class MovementRequest {
    private float directionX;
    private float directionY;
    private float directionZ;
    private float speed;
}