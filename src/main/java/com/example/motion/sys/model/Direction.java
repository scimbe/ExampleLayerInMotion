package com.example.motion.sys.model;

/**
 * Repräsentiert eine Richtung im 3D-Raum.
 */
public class Direction {
    private final float x;
    private final float y;
    private final float z;

    public Direction(float x, float y, float z) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        // Normalisiere den Vektor, falls er nicht Null ist
        if (length > 0.0001f) {
            this.x = x / length;
            this.y = y / length;
            this.z = z / length;
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 1; // Standardrichtung nach vorne
        }
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

    /**
     * Konvertiert die Richtung in eine Rotation.
     * Berechnet nur die Y-Rotation (Yaw), da dies für die meisten Bewegungen ausreicht.
     */
    public Rotation toRotation() {
        float yaw = (float) Math.toDegrees(Math.atan2(x, z));
        return new Rotation(0, yaw, 0);
    }

    @Override
    public String toString() {
        return String.format("Direction(x=%.2f, y=%.2f, z=%.2f)", x, y, z);
    }
}
