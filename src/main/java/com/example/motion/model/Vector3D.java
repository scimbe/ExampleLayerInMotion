package com.example.motion.model;

import lombok.Getter;

@Getter
public class Vector3D {
    private final float x;
    private final float y;
    private final float z;

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length > 0) {
            return new Vector3D(x / length, y / length, z / length);
        }
        return this;
    }
}