package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;
import java.util.UUID;

/**
 * Basis-Implementation eines Motion Layers für Gehbewegungen.
 */
public class BasicWalkingLayer implements IMotionLayer {
    
    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        float speed = currentState.getSpeed();
        Rotation rotation = currentState.getRotation();
        Position currentPos = currentState.getPosition();
        
        // Berechne Bewegungsvektor basierend auf Rotation und Geschwindigkeit
        float dx = (float) Math.sin(Math.toRadians(rotation.getYaw())) * speed * deltaTime;
        float dz = (float) Math.cos(Math.toRadians(rotation.getYaw())) * speed * deltaTime;
        
        // Simuliere leichte vertikale Oszillation für natürlicheren Gang
        float bobbing = (float) Math.sin(System.currentTimeMillis() / 500.0) * 0.05f * speed;
        
        // Aktualisiere Position
        Position newPos = new Position(
            currentPos.getX() + dx,
            currentPos.getY() + bobbing,
            currentPos.getZ() + dz
        );
        
        return new MotionState(
            characterId,
            newPos,
            rotation,
            speed
        );
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState proposedState) {
        // Basic Walking Layer implementiert keine Kollisionserkennung
        return null;
    }

    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Einfache Physik-Verarbeitung mit Gravitationseinfluss
        Position position = physicsData.getPosition();
        float groundY = 0.0f; // Annahme: Boden ist auf Y=0
        
        // Stelle sicher, dass der Charakter nicht durch den Boden fällt
        Position adjustedPosition = new Position(
            position.getX(),
            Math.max(groundY, position.getY() - 9.81f * physicsData.getDeltaTime() * 0.1f),
            position.getZ()
        );
        
        return new MotionState(
            characterId,
            adjustedPosition,
            physicsData.getRotation(),
            physicsData.getSpeed()
        );
    }
    
    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Validierung für Walking Layer
        // Geschwindigkeit sollte im Bereich für Gehen sein
        return motionState.getSpeed() >= 0 && motionState.getSpeed() <= 2.0f; 
    }
    
    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        // Lineare Interpolation zwischen Start- und Endzustand
        Position startPos = start.getPosition();
        Position endPos = end.getPosition();
        Position interpolatedPos = new Position(
            startPos.getX() + (endPos.getX() - startPos.getX()) * factor,
            startPos.getY() + (endPos.getY() - startPos.getY()) * factor,
            startPos.getZ() + (endPos.getZ() - startPos.getZ()) * factor
        );
        
        Rotation startRot = start.getRotation();
        Rotation endRot = end.getRotation();
        Rotation interpolatedRot = new Rotation(
            startRot.getPitch() + (endRot.getPitch() - startRot.getPitch()) * factor,
            startRot.getYaw() + (endRot.getYaw() - startRot.getYaw()) * factor,
            startRot.getRoll() + (endRot.getRoll() - startRot.getRoll()) * factor
        );
        
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * factor;
        
        return new MotionState(
            start.getCharacterId(),
            interpolatedPos,
            interpolatedRot,
            interpolatedSpeed
        );
    }

    @Override
    public void reset(UUID characterId) {
        // Basic Walking Layer benötigt kein Reset
    }
}