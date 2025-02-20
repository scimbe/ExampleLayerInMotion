package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;

import java.util.UUID;

/**
 * Implementiert Laufbewegungen mit höherer Geschwindigkeit und Dynamik.
 */
public class RunningLayer implements IMotionLayer {

    private static final float RUNNING_SPEED = 3.0f;

    private static final float ACCELERATION = 2.0f;

    private static final float MAX_STAMINA = 100.0f;

    private float currentStamina = MAX_STAMINA;

    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        float speed = currentState.getSpeed();

        // Stamina-basierte Geschwindigkeitsanpassung
        if (speed > 0) {
            currentStamina = Math.max(0, currentStamina - deltaTime * 10);
            if (currentStamina <= 0) {
                speed = Math.max(1.0f, speed - ACCELERATION * deltaTime);
            }
        } else {
            currentStamina = Math.min(MAX_STAMINA, currentStamina + deltaTime * 5);
        }

        // Berechne neue Position mit Laufgeschwindigkeit
        Position currentPos = currentState.getPosition();
        Rotation rotation = currentState.getRotation();

        float distance = speed * RUNNING_SPEED * deltaTime;
        float newX = currentPos.getX()
                + distance * (float) Math.cos(Math.toRadians(rotation.getYaw()));
        float newZ = currentPos.getZ()
                + distance * (float) Math.sin(Math.toRadians(rotation.getYaw()));

        // Füge vertikale Oszillation für Laufbewegung hinzu
        float bobbing = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.1f;
        float newY = currentPos.getY() + bobbing;

        return new MotionState(
                characterId,
                new Position(newX, newY, newZ),
                rotation,
                speed);
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState motionState) {
        // Erweiterte Kollisionsprüfung für schnellere Bewegungen
        return null; // Dummy-Implementierung
    }

    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Physik mit Trägheit und Beschleunigung
        float speed = physicsData.getSpeed();
        if (speed > 0) {
            speed = Math.min(RUNNING_SPEED, speed + ACCELERATION * physicsData.getDeltaTime());
        }

        return new MotionState(
                characterId,
                physicsData.getPosition(),
                physicsData.getRotation(),
                speed);
    }

    @Override
    public boolean validateMotionState(MotionState motionState) {
        return motionState.getSpeed() <= RUNNING_SPEED && currentStamina > 0;
    }

    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        // Ähnlich wie BasicWalkingLayer, aber mit Berücksichtigung der höheren Geschwindigkeit
        Position interpolatedPos = interpolatePosition(start.getPosition(), end.getPosition(),
                factor);
        Rotation interpolatedRot = interpolateRotation(start.getRotation(), end.getRotation(),
                factor);
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * factor;

        return new MotionState(
                start.getCharacterId(),
                interpolatedPos,
                interpolatedRot,
                interpolatedSpeed);
    }

    private Position interpolatePosition(Position start, Position end, float factor) {
        return new Position(
                start.getX() + (end.getX() - start.getX()) * factor,
                start.getY() + (end.getY() - start.getY()) * factor,
                start.getZ() + (end.getZ() - start.getZ()) * factor);
    }

    private Rotation interpolateRotation(Rotation start, Rotation end, float factor) {
        return new Rotation(
                start.getPitch() + (end.getPitch() - start.getPitch()) * factor,
                start.getYaw() + (end.getYaw() - start.getYaw()) * factor,
                start.getRoll() + (end.getRoll() - start.getRoll()) * factor);
    }
}
