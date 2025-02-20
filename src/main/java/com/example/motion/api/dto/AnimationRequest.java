package com.example.motion.api.dto;

import lombok.Data;

@Data
public class AnimationRequest {
    private String animationId;
    private float speed;
}