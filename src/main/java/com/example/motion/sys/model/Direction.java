package com.example.motion.sys.model;

/**
 * Repräsentiert eine Bewegungsrichtung im 3D-Raum.
 */
public class Direction {
    private final Vector3D vector;

    public Direction(Vector3D vector) {
        this.vector = vector.normalize();
    }

    public Vector3D getVector() {
        return vector;
    }

    /**
     * Konvertiert die Richtung in eine Rotation.
     */
    public Rotation toRotation() {
        // Berechne Yaw (horizontale Rotation)
        float yaw = (float) Math.toDegrees(Math.atan2(vector.getZ(), vector.getX()));

        // Berechne Pitch (vertikale Rotation)
        float pitch = (float) Math.toDegrees(Math.asin(vector.getY()));

        // Roll bleibt 0, da wir keine seitliche Neigung haben
        return new Rotation(pitch, yaw, 0.0f);
    }

    @Override
    public String toString() {
        return String.format("Direction(%s)", vector);
    }

    /**
     * Erstellt eine Richtung aus Euler-Winkeln.
     */
    public static Direction fromRotation(float pitch, float yaw) {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        float x = (float) (Math.cos(pitchRad) * Math.cos(yawRad));
        float y = (float) Math.sin(pitchRad);
        float z = (float) (Math.cos(pitchRad) * Math.sin(yawRad));

        return new Direction(new Vector3D(x, y, z));
    }

    /**
     * Erstellt eine Richtung aus einem Rotationsobjekt.
     */
    public static Direction fromRotation(Rotation rotation) {
        return fromRotation(rotation.getPitch(), rotation.getYaw());
    }
}
