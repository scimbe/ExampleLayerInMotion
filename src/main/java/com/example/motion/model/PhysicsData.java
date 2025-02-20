package com.example.motion.model;

import lombok.Getter;

@Getter
public class PhysicsData {
    private final Position position;
    private final Rotation rotation;
    private final float speed;
    private final float deltaTime;
    private final Vector3D velocity;
    private final Vector3D acceleration;

    public PhysicsData(Position position, Rotation rotation, float speed, float deltaTime,
                      Vector3D velocity, Vector3D acceleration) {
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.deltaTime = deltaTime;
        this.velocity = velocity;
        this.acceleration = acceleration;
    }
}