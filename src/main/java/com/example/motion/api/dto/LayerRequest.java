package com.example.motion.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LayerRequest {
    private String className;
    private int priority;
}