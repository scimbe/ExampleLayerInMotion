package com.example.motion.sys.model;

/**
 * Repräsentiert eine Rotation im 3D-Raum (Euler-Winkel).
 */
public class Rotation {
    private final float x; // Pitch
    private final float y; // Yaw
    private final float z; // Roll

    public Rotation(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getPitch() {
        return x;
    }

    public float getYaw() {
        return y;
    }

    public float getRoll() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("Rotation(pitch=%.2f, yaw=%.2f, roll=%.2f)", x, y, z);
    }
}
