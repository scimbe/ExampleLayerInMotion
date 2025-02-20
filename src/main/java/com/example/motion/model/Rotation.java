package com.example.motion.model;

import lombok.Getter;

@Getter
public class Rotation {
    private final float pitch;
    private final float yaw;
    private final float roll;

    public Rotation(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }
}