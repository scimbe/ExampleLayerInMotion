package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;

import java.util.UUID;

/**
 * Implementiert Basis-Gehbewegungen für Charaktere.
 */
public class BasicWalkingLayer implements IMotionLayer {

    private static final float WALKING_SPEED = 1.0f;

    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        if (currentState.getSpeed() <= 0) {
            return currentState;
        }

        // Berechne neue Position basierend auf Geschwindigkeit und Richtung
        Position currentPos = currentState.getPosition();
        Rotation rotation = currentState.getRotation();

        float distance = currentState.getSpeed() * WALKING_SPEED * deltaTime;
        float newX = currentPos.getX()
                + distance * (float) Math.cos(Math.toRadians(rotation.getYaw()));
        float newZ = currentPos.getZ()
                + distance * (float) Math.sin(Math.toRadians(rotation.getYaw()));

        Position newPosition = new Position(newX, currentPos.getY(), newZ);

        return new MotionState(
                characterId,
                newPosition,
                rotation,
                currentState.getSpeed());
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState motionState) {
        // Dummy-Implementierung: Keine Kollisionserkennung
        return null;
    }

    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Grundlegende Gravitationsberechnung
        Position adjustedPosition = new Position(
                physicsData.getPosition().getX(),
                Math.max(0, physicsData.getPosition().getY() - 9.81f * physicsData.getDeltaTime()),
                physicsData.getPosition().getZ());

        return new MotionState(
                characterId,
                adjustedPosition,
                physicsData.getRotation(),
                physicsData.getSpeed());
    }
}
