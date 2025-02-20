package com.example.motion.model;

/**
 * Repräsentiert die Position eines Charakters im 3D-Raum.
 */
public class Position {
    private final float x;
    private final float y;
    private final float z;

    public Position(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    @Override
    public String toString() {
        return String.format("Position(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }
}