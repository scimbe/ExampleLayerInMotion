package com.example.motion.sys.model;

/**
 * Repräsentiert die Rotation eines Charakters im 3D-Raum.
 */
public class Rotation {
    private final float pitch;
    private final float yaw;
    private final float roll;

    public Rotation(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public float getPitch() { return pitch; }
    public float getYaw() { return yaw; }
    public float getRoll() { return roll; }
}
