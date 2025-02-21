package com.example.motion.sys.model;

/**
 * Repräsentiert einen 3D-Vektor.
 */
public class Vector3D {
    private final float x;
    private final float y;
    private final float z;

    public Vector3D(float x, float y, float z) {
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

    public Vector3D normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length > 0.0001f) {
            return new Vector3D(x / length, y / length, z / length);
        }
        return new Vector3D(0, 0, 1);
    }

    public Direction toDirection() {
        return new Direction(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vector3D(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }
}
