package com.example.motion.model;

/**
 * Enthält physikalische Daten für Bewegungsberechnungen.
 */
public class PhysicsData {
    private final Position position;
    private final Rotation rotation;
    private final float speed;
    private final float deltaTime;
    private final Vector3D velocity;
    private final Vector3D acceleration;

    public PhysicsData(Position position, 
                      Rotation rotation, 
                      float speed, 
                      float deltaTime, 
                      Vector3D velocity, 
                      Vector3D acceleration) {
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.deltaTime = deltaTime;
        this.velocity = velocity;
        this.acceleration = acceleration;
    }

    public Position getPosition() { return position; }
    public Rotation getRotation() { return rotation; }
    public float getSpeed() { return speed; }
    public float getDeltaTime() { return deltaTime; }
    public Vector3D getVelocity() { return velocity; }
    public Vector3D getAcceleration() { return acceleration; }

    @Override
    public String toString() {
        return String.format(
            "PhysicsData(pos=%s, rot=%s, speed=%.2f, dt=%.3f, vel=%s, acc=%s)",
            position, rotation, speed, deltaTime, velocity, acceleration
        );
    }
}