package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;

import java.util.UUID;

/**
 * Implementiert Basis-Gehbewegungen für Charaktere.
 */
public class BasicWalkingLayer implements IMotionLayer {

    private static final float WALKING_SPEED = 1.0f;

    private static final float MAX_SLOPE = 30.0f;

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

    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Prüfe Geschwindigkeitsgrenzen und Hangneigung
        return motionState.getSpeed() <= WALKING_SPEED &&
                Math.abs(Math.toDegrees(Math.atan2(
                        motionState.getPosition().getY(),
                        Math.sqrt(
                                Math.pow(motionState.getPosition().getX(), 2) +
                                        Math.pow(motionState.getPosition().getZ(),
                                                2))))) <= MAX_SLOPE;
    }

    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
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
