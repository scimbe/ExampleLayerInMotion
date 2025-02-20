package com.example.motion.sys.model;

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

    public static Direction fromRotation(float pitch, float yaw) {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        float x = (float) (Math.cos(pitchRad) * Math.cos(yawRad));
        float y = (float) Math.sin(pitchRad);
        float z = (float) (Math.cos(pitchRad) * Math.sin(yawRad));

        return new Direction(new Vector3D(x, y, z));
    }

    public static Direction fromRotation(Rotation rotation) {
        return fromRotation(rotation.getPitch(), rotation.getYaw());
    }
}
