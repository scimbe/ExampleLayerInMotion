package com.example.motion.model;

import lombok.Getter;

@Getter
public class Position {
    private final float x;
    private final float y;
    private final float z;

    public Position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}