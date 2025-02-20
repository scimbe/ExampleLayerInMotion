package com.example.motion.sys.model;

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
}
