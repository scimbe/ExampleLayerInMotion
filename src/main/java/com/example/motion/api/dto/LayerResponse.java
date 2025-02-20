package com.example.motion.api.dto;

import lombok.Getter;

@Getter
public class LayerResponse {
    private final String name;
    private final String className;

    public LayerResponse(String name, String className) {
        this.name = name;
        this.className = className;
    }
}