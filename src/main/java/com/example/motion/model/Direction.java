package com.example.motion.model;

import lombok.Getter;

@Getter
public class Direction {
    private final Vector3D vector;

    public Direction(Vector3D vector) {
        this.vector = vector.normalize();
    }

    public Rotation toRotation() {
        float yaw = (float) Math.toDegrees(Math.atan2(vector.getZ(), vector.getX()));
        float pitch = (float) Math.toDegrees(Math.asin(vector.getY()));
        return new Rotation(pitch, yaw, 0.0f);
    }
}