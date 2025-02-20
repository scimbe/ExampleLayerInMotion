package com.example.motion.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimationRequest {
    private String animationId;
    private float speed;
}